package de.cristelknight.cristellib.builtinpacks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.PlatformHelper;
import de.cristelknight.cristellib.util.JsonHelper;
import de.cristelknight.cristellib.util.runtimepack.RuntimePackUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.flag.FeatureFlags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class RuntimePack implements PackResources {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private final Lock waiting = new ReentrantLock();
    private final Map<ResourceLocation, Supplier<byte[]>> data = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, Supplier<byte[]>> assets = new ConcurrentHashMap<>();
    private final Map<List<String>, Supplier<byte[]>> root = new ConcurrentHashMap<>();
    public final int packVersion;
    private final String id;

    private final PackLocationInfo metadata;


    public RuntimePack(ResourceLocation id, int version, String description, @Nullable InputStream imageStream) {
        packVersion = version;
        this.id = id.toString();

        metadata = new PackLocationInfo(
                this.id,
                Component.literal(description),
                new BuiltinResourcePackSource(),
                Optional.empty()
        );

        if (imageStream != null) {
            byte[] image = RuntimePackUtil.extractImageBytes(imageStream);
            if (image != null) addRootResource("pack.png", image);
        }

        if (!hasRootResource("pack.mcmeta")) {
            JsonObject object = new JsonObject();
            JsonObject pack = new JsonObject();
            pack.addProperty("pack_format", packVersion);
            pack.addProperty("min_format", packVersion);
            pack.addProperty("max_format", packVersion);
            pack.addProperty("description", description);
            object.add("pack", pack);
            addRootResource("pack.mcmeta", RuntimePackUtil.serializeJson(object));
        }
    }

    protected Map<ResourceLocation, Supplier<byte[]>> getSys(PackType side) {
        return side == PackType.CLIENT_RESOURCES ? assets : data;
    }

    public byte[] addStructureSet(ResourceLocation identifier, JsonObject set) {
        return addDataForJsonLocation("worldgen/structure_set", identifier, set);
    }

    public boolean removeStructureSet(ResourceLocation identifier) {
        return removeDataForJsonLocation("worldgen/structure_set", identifier);
    }

    public byte[] addBiome(ResourceLocation identifier, JsonObject biome) {
        return addDataForJsonLocation("worldgen/biome", identifier, biome);
    }

    public byte[] addStructure(ResourceLocation identifier, JsonObject structure) {
        return addDataForJsonLocation("worldgen/structure", identifier, structure);
    }

    public byte[] addLootTable(ResourceLocation identifier, JsonObject table) {
        return addDataForJsonLocation("loot_tables", identifier, table);
    }

    public byte @Nullable [] addDataForJsonLocationFromPath(String prefix, ResourceLocation identifier, String fromSubPath, String fromModID) {
        if (JsonHelper.getElement(fromModID, fromSubPath) instanceof JsonObject object) {
            return addDataForJsonLocation(prefix, identifier, object);
        }
        return null;
    }

    public byte[] addDataForJsonLocation(String prefix, ResourceLocation identifier, JsonObject object) {
        return addAndSerializeDataForLocation(prefix, "json", identifier, object);
    }

    public boolean removeDataForJsonLocation(String prefix, ResourceLocation identifier) {
        return removeDataForLocation(prefix, "json", identifier);
    }

    public byte[] addAndSerializeDataForLocation(String prefix, String end, ResourceLocation identifier, JsonObject object) {
        return addData(ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + end), RuntimePackUtil.serializeJson(object));
    }
    public boolean removeDataForLocation(String prefix, String end, ResourceLocation identifier) {
        return removeData(ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + end));
    }


    public byte[] addData(ResourceLocation path, byte[] data) {
        lock();
        try {
            this.data.put(path, () -> data);
            return data;
        } finally {
            waiting.unlock();
        }
    }

    public byte[] addImageAsset(ResourceLocation path, String modId, String subPath) {
        InputStream stream = PlatformHelper.getResourceStream(modId, subPath);
        if(stream == null)
            return null;
        byte[] asset = RuntimePackUtil.extractImageBytes(stream);
        return addAsset(path, asset);
    }

    public byte[] addAsset(ResourceLocation path, byte[] asset) {
        lock();
        try {
            assets.put(path, () -> asset);
            return asset;
        } finally {
            waiting.unlock();
        }
    }

    public boolean removeData(ResourceLocation path) {
        lock();
        try {
            return data.remove(path) != null;
        } finally {
            waiting.unlock();
        }
    }

    public void removeAsset(ResourceLocation path) {
        lock();
        try {
            assets.remove(path);
        } finally {
            waiting.unlock();
        }
    }


    public byte[] addRootResource(String path, byte[] data) {
        lock();
        try {
            root.put(Arrays.asList(path.split("/")), () -> data);
            return data;
        } finally {
            waiting.unlock();
        }
    }


    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String @NotNull ... strings) {
        lock();
        try {
            Supplier<byte[]> supplier = root.get(Arrays.asList(strings));
            if (supplier == null) {
                return null;
            }
            return () -> new ByteArrayInputStream(supplier.get());
        } finally {
            waiting.unlock();
        }
    }

    public boolean hasRootResource(String @NotNull ... strings) {
        lock();
        try {
            return root.containsKey(Arrays.asList(strings));
        } finally {
            waiting.unlock();
        }
    }


    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(@NotNull PackType packType, @NotNull ResourceLocation id) {
        lock();
        try {
            Supplier<byte[]> supplier = getSys(packType).get(id);
            if (supplier == null) {
                return null;
            }
            return () -> new ByteArrayInputStream(supplier.get());
        } finally {
            waiting.unlock();
        }
    }

    public @Nullable JsonObject getResourceAsJson(PackType packType, ResourceLocation location) {
        IoSupplier<InputStream> stream = getResource(packType, location);
        JsonObject jsonObject;
        try {
            jsonObject = GsonHelper.parse(new InputStreamReader(stream.get(), StandardCharsets.UTF_8));
        } catch (IOException | NullPointerException ex) {
            Constants.LOG.error("Couldn't get JsonObject from location: {}", location, ex);
            return null;
        }
        return jsonObject;
    }

    public boolean hasData(ResourceLocation location) {
        lock();
        try {
            return data.containsKey(location);
        } finally {
            waiting.unlock();
        }
    }

    public boolean hasAsset(ResourceLocation location) {
        lock();
        try {
            return assets.containsKey(location);
        } finally {
            waiting.unlock();
        }
    }

    @Override
    public void listResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String prefix, @NotNull ResourceOutput resourceOutput) {
        lock();
        try {
            for (ResourceLocation identifier : getSys(packType).keySet()) {
                Supplier<byte[]> supplier = getSys(packType).get(identifier);
                if (supplier == null) {
                    continue;
                }

                if (identifier.getNamespace().equals(namespace) && identifier.getPath().contains(prefix + "/")) {
                    /*
                    List<String> identifierHere = Arrays.stream(identifier.getPath().split("/")).toList();
                    List<String> identifierThere = Arrays.stream(prefix.split("/")).toList();
                    if(new HashSet<>(identifierHere).containsAll(identifierThere)) {
                     */
                    IoSupplier<InputStream> inputSupplier = () -> new ByteArrayInputStream(supplier.get());
                    resourceOutput.accept(identifier, inputSupplier);

                }
            }
        } finally {
            waiting.unlock();
        }
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType packType) {
        lock();
        try {
            Set<String> namespaces = new HashSet<>();
            for (ResourceLocation identifier : getSys(packType).keySet()) {
                namespaces.add(identifier.getNamespace());
            }
            return namespaces;
        } finally {
            waiting.unlock();
        }
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(@NotNull MetadataSectionSerializer<T> metadataSectionSerializer) {
        InputStream stream = null;
        try {
            IoSupplier<InputStream> supplier = this.getRootResource("pack.mcmeta");
            if (supplier != null) {
                stream = supplier.get();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(stream != null) {
            return FilePackResources.getMetadataFromStream(metadataSectionSerializer, stream);
        } else {
            if(metadataSectionSerializer.getMetadataSectionName().equals("pack")) {
                JsonObject object = new JsonObject();
                object.addProperty("pack_format", this.packVersion);
                object.addProperty("description", this.id);
                return metadataSectionSerializer.fromJson(object);
            }
            else if(metadataSectionSerializer.getMetadataSectionName().equals("features")){
                return metadataSectionSerializer.fromJson(FeatureFlagsMetadataSection.TYPE.toJson(new FeatureFlagsMetadataSection(FeatureFlags.DEFAULT_FLAGS)));
            }
            Constants.LOG.debug("'{}' is an unsupported metadata key", metadataSectionSerializer.getMetadataSectionName());
            return null;
        }
    }

    @Override
    public @NotNull PackLocationInfo location() {
        return metadata;
    }


    @Override
    public @NotNull String packId() {
        return id;
    }

    private void lock() {
        waiting.lock();
    }

    public void clear(PackType packType) {
        lock();
        try {
            getSys(packType).clear();
        } finally {
            waiting.unlock();
        }
    }

    @Override
    public void close() {
        Constants.LOG.debug("Closing Runtime Pack: {}", id);
    }

    public void dumpToFolder(Path output) throws IOException {
        lock();
        try {
            // Dump root resources (e.g. pack.mcmeta, pack.png)
            for (Map.Entry<List<String>, Supplier<byte[]>> entry : root.entrySet()) {
                List<String> pathParts = entry.getKey();
                Path filePath = output.resolve(Paths.get("", pathParts.toArray(new String[0])));
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, entry.getValue().get(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            // Dump namespaced data (e.g. data/<namespace>/<resource>.json)
            for (Map.Entry<ResourceLocation, Supplier<byte[]>> entry : data.entrySet()) {
                ResourceLocation rl = entry.getKey();
                Path filePath = output.resolve(Paths.get("data", rl.getNamespace(), rl.getPath()));
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, entry.getValue().get(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            for (Map.Entry<ResourceLocation, Supplier<byte[]>> entry : assets.entrySet()) {
                ResourceLocation rl = entry.getKey();
                Path filePath = output.resolve(Paths.get("assets", rl.getNamespace(), rl.getPath()));
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, entry.getValue().get(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } finally {
            waiting.unlock();
        }
    }
}
