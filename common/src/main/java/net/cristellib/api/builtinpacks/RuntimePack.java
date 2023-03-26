package net.cristellib.api.builtinpacks;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.cristellib.CristelLib;
import net.cristellib.CristelLibExpectPlatform;
import net.cristellib.config.ConfigUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class RuntimePack implements PackResources {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();
    private final Lock waiting = new ReentrantLock();
    private final Map<ResourceLocation, Supplier<byte[]>> data = new ConcurrentHashMap<>();
    private final Map<List<String>, Supplier<byte[]>> root = new ConcurrentHashMap<>();
    public final int packVersion;
    private final String name;


    public RuntimePack(String name, int version, @Nullable Path imageFile) {
        this.packVersion = version;
        this.name = name;
        if(imageFile != null){
            byte[] image = extractImageBytes(imageFile);
            if(image != null) this.addRootResource("pack.png", image);
        }
    }


    public byte[] addStructureSet(ResourceLocation identifier, JsonObject set) {
        return this.addDataForJsonLocation("worldgen/structure_set", identifier, set);
    }
    public byte[] addBiome(ResourceLocation identifier, JsonObject biome) {
        return this.addDataForJsonLocation("worldgen/biome", identifier, biome);
    }
    public byte[] addStructure(ResourceLocation identifier, JsonObject structure) {
        return this.addDataForJsonLocation("worldgen/structure", identifier, structure);
    }
    public byte[] addLootTable(ResourceLocation identifier, JsonObject table) {
        return this.addDataForJsonLocation("loot_tables", identifier, table);
    }

    public byte @Nullable [] addDataForJsonLocationFromPath(String prefix, ResourceLocation identifier, String fromSubPath, String fromModID) {
        if(ConfigUtil.getElement(fromModID, fromSubPath) instanceof JsonObject object){
            return addDataForJsonLocation(prefix, identifier, object);
        }
        return null;
    }

    public byte[] addDataForJsonLocation(String prefix, ResourceLocation identifier, JsonObject object) {
        return this.addAndSerializeDataForLocation(prefix, "json", identifier, object);
    }
    public byte[] addAndSerializeDataForLocation(String prefix, String end, ResourceLocation identifier, JsonObject object) {
        return this.addData(new ResourceLocation(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + end), serializeJson(object));
    }
    public byte[] addData(ResourceLocation path, byte[] data) {
        this.data.put(path, () -> data);
        return data;
    }

    public static byte @Nullable [] extractImageBytes(Path imageName) {
        InputStream stream;
        BufferedImage bufferedImage;
        try {
            stream = Files.newInputStream(imageName.toAbsolutePath());
            bufferedImage = ImageIO.read(stream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            CristelLib.LOGGER.warn("Couldn't get image for path: " + imageName, e);
            return null;
        }
    }


    public static byte[] serializeJson(JsonObject object) {
        UnsafeByteArrayOutputStream ubaos = new UnsafeByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(ubaos, StandardCharsets.UTF_8);
        GSON.toJson(object, writer);
        try {
            writer.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return ubaos.getBytes();
    }

    public byte[] addRootResource(String path, byte[] data) {
        this.root.put(Arrays.asList(path.split("/")), () -> data);
        return data;
    }


    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String @NotNull ... strings) {
        this.lock();
        Supplier<byte[]> supplier = this.root.get(Arrays.asList(strings));
        if(supplier == null) {
            this.waiting.unlock();
            return null;
        }
        this.waiting.unlock();
        return () -> new ByteArrayInputStream(supplier.get());
    }

    private void lock() {
        if(!this.waiting.tryLock()) {
            this.waiting.lock();
        }
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(@NotNull PackType packType, @NotNull ResourceLocation id) {
        this.lock();
        Supplier<byte[]> supplier = this.data.get(id);
        if(supplier == null) {
            this.waiting.unlock();
            return null;
        }
        this.waiting.unlock();
        return () -> new ByteArrayInputStream(supplier.get());
    }


    @Override
    public void listResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String prefix, @NotNull ResourceOutput resourceOutput) {
        this.lock();
        for(ResourceLocation identifier : this.data.keySet()) {
            Supplier<byte[]> supplier = this.data.get(identifier);
            if(supplier == null) {
                this.waiting.unlock();
                continue;
            }

            if(identifier.getNamespace().equals(namespace) && identifier.getPath().contains(prefix + "/")) {
                /*
                List<String> identifierHere = Arrays.stream(identifier.getPath().split("/")).toList();
                List<String> identifierThere = Arrays.stream(prefix.split("/")).toList();
                if(new HashSet<>(identifierHere).containsAll(identifierThere)) {
                 */
                IoSupplier<InputStream> inputSupplier = () -> new ByteArrayInputStream(supplier.get());
                resourceOutput.accept(identifier, inputSupplier);

            }
        }
        this.waiting.unlock();
    }

    @Override
    public Set<String> getNamespaces(@NotNull PackType packType) {
        this.lock();
        Set<String> namespaces = new HashSet<>();
        for(ResourceLocation identifier : this.data.keySet()) {
            namespaces.add(identifier.getNamespace());
        }
        this.waiting.unlock();
        return namespaces;
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
                object.addProperty("description", this.name);
                return metadataSectionSerializer.fromJson(object);
            }
            else if(metadataSectionSerializer.getMetadataSectionName().equals("features")){
                return metadataSectionSerializer.fromJson(FeatureFlagsMetadataSection.TYPE.toJson(new FeatureFlagsMetadataSection(FeatureFlags.DEFAULT_FLAGS)));
            }
            CristelLib.LOGGER.debug("'" + metadataSectionSerializer.getMetadataSectionName() + "' is an unsupported metadata key");
            return null;
        }
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    @Override
    public String packId() {
        return this.name;
    }

    @Override
    public void close() {
        CristelLib.LOGGER.debug("Closing RDP: " + this.name);
    }
}
