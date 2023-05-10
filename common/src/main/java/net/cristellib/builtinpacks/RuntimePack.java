package net.cristellib.builtinpacks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.cristellib.CristelLib;
import net.cristellib.config.ConfigUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RuntimePack implements PackResources {

    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();

    public final int packVersion;
    private final String name;
    private final Lock waiting = new ReentrantLock();
    private final Map<ResourceLocation, Supplier<byte[]>> data = new ConcurrentHashMap<>();
    private final Map<String, Supplier<byte[]>> root = new ConcurrentHashMap<>();


    public RuntimePack(String name, int version, @Nullable Path imageFile) {
        this.packVersion = version;
        this.name = name;
        if(imageFile != null){
            byte[] image = extractImageBytes(imageFile);
            if(image != null) this.addRootResource("pack.png", image);
        }
    }


    //Adding Stuff
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
    public byte[] addRootResource(String path, byte[] data) {
        this.root.put(path, () -> data);
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


    //Rest of the Stuff












    public void dumpDirect(Path output) {
        CristelLib.LOGGER.info("dumping " + this.name + "'s assets and data");
        // data dump time
        try {
            for(Map.Entry<String, Supplier<byte[]>> e : this.root.entrySet()) {
                Path root = output.resolve(e.getKey());
                Files.createDirectories(root.getParent());
                Files.write(root, e.getValue().get());
            }

            Path data = output.resolve("data");
            Files.createDirectories(data);
            for(Map.Entry<ResourceLocation, Supplier<byte[]>> entry : this.data.entrySet()) {
                this.write(data, entry.getKey(), entry.getValue().get());
            }
        } catch(IOException exception) {
            throw new RuntimeException(exception);
        }
    }



    @Nullable
    @Override
    public InputStream getRootResource(String fileName) {
        if(!fileName.contains("/") && !fileName.contains("\\")) {
            this.lock();
            Supplier<byte[]> supplier = this.root.get(fileName);
            if(supplier == null) {
                this.waiting.unlock();
                return null;
            }
            this.waiting.unlock();
            return new ByteArrayInputStream(supplier.get());
        } else {
            throw new IllegalArgumentException("File name can't be a path");
        }
    }

    @Override
    public InputStream getResource(PackType packType, @NotNull ResourceLocation id) {
        if(packType.equals(PackType.CLIENT_RESOURCES)) return null;
        this.lock();
        Supplier<byte[]> supplier = data.get(id);
        if(supplier == null) {
            this.waiting.unlock();
            return null;
        }
        this.waiting.unlock();
        return new ByteArrayInputStream(supplier.get());
    }


    public @Nullable JsonObject getResource(ResourceLocation location){
        InputStream stream = this.getResource(PackType.SERVER_DATA, location);
        return GsonHelper.parse(new BufferedReader(new InputStreamReader(stream)));
    }



    @Override
    public Collection<ResourceLocation> getResources(PackType packType, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
        Set<ResourceLocation> resourceLocations = new HashSet<>();
        if(packType.equals(PackType.CLIENT_RESOURCES)) return resourceLocations;
        this.lock();
        for(ResourceLocation ResourceLocation : data.keySet()) {
            if(ResourceLocation.getNamespace().equals(namespace) && ResourceLocation.getPath().startsWith(prefix) && pathFilter.test(ResourceLocation.getPath())) {
                resourceLocations.add(ResourceLocation);
            }
        }
        this.waiting.unlock();
        return resourceLocations;
    }

    @Override
    public boolean hasResource(PackType packType, ResourceLocation id) {
        if(packType.equals(PackType.CLIENT_RESOURCES)) return false;
        this.lock();
        boolean contains = data.containsKey(id);
        this.waiting.unlock();
        return contains;
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        Set<String> namespaces = new HashSet<>();
        if(packType.equals(PackType.CLIENT_RESOURCES)) return namespaces;
        this.lock();
        for(ResourceLocation ResourceLocation : this.data.keySet()) {
            namespaces.add(ResourceLocation.getNamespace());
        }
        this.waiting.unlock();
        return namespaces;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        if(metaReader.getMetadataSectionName().equals("pack")) {
            JsonObject object = new JsonObject();
            object.addProperty("pack_format", this.packVersion);
            object.addProperty("description", this.name);
            return metaReader.fromJson(object);
        }
        return metaReader.fromJson(new JsonObject());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void close() {
        CristelLib.LOGGER.debug("Closing RDP: " + this.name);
    }



    private void lock() {
        if(!this.waiting.tryLock()) {
            this.waiting.lock();
        }
    }

    private void write(Path dir, ResourceLocation ResourceLocation, byte[] data) {
        try {
            Path file = dir.resolve(ResourceLocation.getNamespace()).resolve(ResourceLocation.getPath());
            Files.createDirectories(file.getParent());
            try(OutputStream output = Files.newOutputStream(file)) {
                output.write(data);
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
