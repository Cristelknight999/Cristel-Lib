package de.cristelknight.cristellib.builtinpacks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.util.JsonHelper;
import de.cristelknight.cristellib.util.runtimepack.RuntimePackUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.io.*;
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
    private final Map<Identifier, Supplier<byte[]>> data = new ConcurrentHashMap<>();
    private final Map<Identifier, Supplier<byte[]>> assets = new ConcurrentHashMap<>();
    private final Map<List<String>, Supplier<byte[]>> root = new ConcurrentHashMap<>();
    public final int packVersion;
    private final String id;

    private final PackLocationInfo metadata;


    public RuntimePack(Identifier id, int version, String description, @Nullable InputStream imageStream) {
        packVersion = version;
        this.id = id.toString();

        metadata = new PackLocationInfo(
                this.id,
                Component.literal(description),
                new BuiltinResourcePackSource(),
                Optional.of(new KnownPack(Constants.MOD_ID, this.id, String.valueOf(version)))
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

    protected Map<Identifier, Supplier<byte[]>> getSys(PackType side) {
        return side == PackType.CLIENT_RESOURCES ? assets : data;
    }

    public byte[] addStructureSet(Identifier identifier, JsonObject set) {
        return addDataForJsonLocation("worldgen/structure_set", identifier, set);
    }

    public void removeStructureSet(Identifier identifier) {
        removeDataForJsonLocation("worldgen/structure_set", identifier);
    }

    public byte[] addBiome(Identifier identifier, JsonObject biome) {
        return addDataForJsonLocation("worldgen/biome", identifier, biome);
    }

    public byte[] addStructure(Identifier identifier, JsonObject structure) {
        return addDataForJsonLocation("worldgen/structure", identifier, structure);
    }

    public byte[] addLootTable(Identifier identifier, JsonObject table) {
        return addDataForJsonLocation("loot_tables", identifier, table);
    }

    public byte @Nullable [] addDataForJsonLocationFromPath(String prefix, Identifier identifier, String fromSubPath, String fromModID) {
        if (JsonHelper.getElement(fromModID, fromSubPath) instanceof JsonObject object) {
            return addDataForJsonLocation(prefix, identifier, object);
        }
        return null;
    }

    public byte[] addDataForJsonLocation(String prefix, Identifier identifier, JsonObject object) {
        return addAndSerializeDataForLocation(prefix, "json", identifier, object);
    }

    public void removeDataForJsonLocation(String prefix, Identifier identifier) {
        removeDataForLocation(prefix, "json", identifier);
    }

    public byte[] addAndSerializeDataForLocation(String prefix, String end, Identifier identifier, JsonObject object) {
        return addData(Identifier.fromNamespaceAndPath(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + end), RuntimePackUtil.serializeJson(object));
    }
    public void removeDataForLocation(String prefix, String end, Identifier identifier) {
        removeData(Identifier.fromNamespaceAndPath(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + end));
    }


    public byte[] addData(Identifier path, byte[] data) {
        lock();
        try {
            this.data.put(path, () -> data);
            return data;
        } finally {
            waiting.unlock();
        }
    }

    public byte[] addImageAsset(Identifier path, String modId, String subPath) {
        InputStream stream = CristelLibExpectPlatform.getResourceStream(modId, subPath);
        if(stream == null)
            return null;
        byte[] asset = RuntimePackUtil.extractImageBytes(stream);
        return addAsset(path, asset);
    }

    public byte[] addAsset(Identifier path, byte[] asset) {
        lock();
        try {
            assets.put(path, () -> asset);
            return asset;
        } finally {
            waiting.unlock();
        }
    }

    public void removeData(Identifier path) {
        lock();
        try {
            data.remove(path);
        } finally {
            waiting.unlock();
        }
    }

    public void removeAsset(Identifier path) {
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
    public IoSupplier<InputStream> getResource(@NotNull PackType packType, @NotNull Identifier id) {
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

    public @Nullable JsonObject getResourceAsJson(PackType packType, Identifier location) {
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

    public boolean hasData(Identifier location) {
        lock();
        try {
            return data.containsKey(location);
        } finally {
            waiting.unlock();
        }
    }

    public boolean hasAsset(Identifier location) {
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
            for (Identifier identifier : getSys(packType).keySet()) {
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
            for (Identifier identifier : getSys(packType).keySet()) {
                namespaces.add(identifier.getNamespace());
            }
            return namespaces;
        } finally {
            waiting.unlock();
        }
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(@NonNull MetadataSectionType<T> metadataSectionType) {
        InputStream stream = null;
        try {
            IoSupplier<InputStream> supplier = getRootResource("pack.mcmeta");
            if (supplier != null) {
                stream = supplier.get();
            }
        } catch (IOException e) {
            throw new RuntimeException(Constants.getWithPrefix("Error reading pack.mcmeta from: " + packId()), e);
        }
        if (stream == null) {
            throw new RuntimeException(Constants.getWithPrefix("Couldn't find pack.mcmeta of Runtime Pack: " + packId()));
        }
        return FilePackResources.getMetadataFromStream(metadataSectionType, stream, metadata);
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
            for (Map.Entry<Identifier, Supplier<byte[]>> entry : data.entrySet()) {
                Identifier rl = entry.getKey();
                Path filePath = output.resolve(Paths.get("data", rl.getNamespace(), rl.getPath()));
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, entry.getValue().get(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            for (Map.Entry<Identifier, Supplier<byte[]>> entry : assets.entrySet()) {
                Identifier rl = entry.getKey();
                Path filePath = output.resolve(Paths.get("assets", rl.getNamespace(), rl.getPath()));
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, entry.getValue().get(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } finally {
            waiting.unlock();
        }
    }
}
