package net.cristellib.forge.extrapackutil;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.cristellib.CristelLib;
import net.minecraft.FileUtil;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.fml.ModContainer;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

public class ModResourcePack implements PackResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModResourcePack.class);
    private static final Pattern RESOURCE_PACK_PATH = Pattern.compile("[a-z0-9-_.]+");
    private static final FileSystem DEFAULT_FS = FileSystems.getDefault();

    private final ResourceLocation id;
    private final Component name;
    private final String modInfo;
    private final List<Path> basePaths;
    private final AutoCloseable closer;
    private final Map<PackType, Set<String>> namespaces;

    public static ModResourcePack create(ResourceLocation id, Component name, ModContainer mod, String subPath) {
        List<Path> rootPaths = List.of();
        try {
            rootPaths = List.of(Path.of(ModResourcePack.class.getResource("/").toURI()));
        } catch (URISyntaxException ignored) {
            LOGGER.error("Couldn't translate the resource path");
        }
        List<Path> paths;

        if (subPath == null) {
            paths = rootPaths;
        } else {
            paths = new ArrayList<>(rootPaths.size());

            for (Path path : rootPaths) {
                path = path.toAbsolutePath().normalize();
                Path childPath = path.resolve(subPath.replace("/", path.getFileSystem().getSeparator())).normalize();
                if (!childPath.startsWith(path) || !exists(childPath)) {
                    continue;
                }

                paths.add(childPath);
            }
        }

        if (paths.isEmpty()){
            return null;
        }

        ModResourcePack ret = new ModResourcePack(id, name, mod.getModId(), paths, null);

        return ret.getNamespaces(PackType.SERVER_DATA).isEmpty() ? null : ret;
    }

    private ModResourcePack(ResourceLocation id, Component name, String modInfo, List<Path> paths, AutoCloseable closer) {
        this.id = id;
        this.name = name;
        this.modInfo = modInfo;
        this.basePaths = paths;
        this.closer = closer;
        this.namespaces = readNamespaces(paths, modInfo);
    }

    static Map<PackType, Set<String>> readNamespaces(List<Path> paths, String modId) {
        Map<PackType, Set<String>> ret = new EnumMap<>(PackType.class);

        for (PackType type : PackType.values()) {
            Set<String> namespaces = null;

            for (Path path : paths) {
                Path dir = path.resolve(type.getDirectory());
                if (!Files.isDirectory(dir)) continue;

                String separator = path.getFileSystem().getSeparator();

                try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                    for (Path p : ds) {
                        if (!Files.isDirectory(p)) continue;

                        String s = p.getFileName().toString();
                        // s may contain trailing slashes, remove them
                        s = s.replace(separator, "");

                        if (!RESOURCE_PACK_PATH.matcher(s).matches()) {
                            LOGGER.warn("Fabric NioResourcePack: ignored invalid namespace: {} in mod ID {}", s, modId);
                            continue;
                        }

                        if (namespaces == null) namespaces = new HashSet<>();

                        namespaces.add(s);
                    }
                } catch (IOException e) {
                    LOGGER.warn("getNamespaces in mod " + modId + " failed!", e);
                }
            }

            ret.put(type, namespaces != null ? namespaces : Collections.emptySet());
        }

        return ret;
    }

    private Path getPath(String filename) {
        if (hasAbsentNs(filename)) return null;

        for (Path basePath : basePaths) {
            Path childPath = basePath.resolve(filename.replace("/", basePath.getFileSystem().getSeparator())).toAbsolutePath().normalize();

            if (childPath.startsWith(basePath) && exists(childPath)) {
                return childPath;
            }
        }

        return null;
    }

    private static final String resPrefix = PackType.CLIENT_RESOURCES.getDirectory() + "/";
    private static final String dataPrefix = PackType.SERVER_DATA.getDirectory() + "/";

    private boolean hasAbsentNs(String filename) {
        int prefixLen;
        PackType type;

        if (filename.startsWith(resPrefix)) {
            prefixLen = resPrefix.length();
            type = PackType.CLIENT_RESOURCES;
        } else if (filename.startsWith(dataPrefix)) {
            prefixLen = dataPrefix.length();
            type = PackType.SERVER_DATA;
        } else {
            return false;
        }

        int nsEnd = filename.indexOf('/', prefixLen);
        if (nsEnd < 0) return false;

        return !namespaces.get(type).contains(filename.substring(prefixLen, nsEnd));
    }

    private IoSupplier<InputStream> openFile(String filename) {
        Path path = getPath(filename);

        if (path != null && Files.isRegularFile(path)) {
            return () -> Files.newInputStream(path);
        }

        if ("pack.mcmeta".equals(filename)) {
            return () -> openDefault(this.modInfo, filename);
        }

        return null;
    }

    public static InputStream openDefault(String info, String filename) {
        if (filename.equals("pack.mcmeta")) {
            String description = Objects.requireNonNullElse(info, "");
            String metadata = serializeMetadata(PackType.SERVER_DATA.getVersion(SharedConstants.getCurrentVersion()), description);
            return IOUtils.toInputStream(metadata, Charsets.UTF_8);
        }
        return null;
    }

    public static String serializeMetadata(int packVersion, String description) {
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", packVersion);
        pack.addProperty("description", description);
        JsonObject metadata = new JsonObject();
        metadata.add("pack", pack);
        return new Gson().toJson(metadata);
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String @NotNull ... pathSegments) {
        FileUtil.validatePath(pathSegments);

        return this.openFile(String.join("/", pathSegments));
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getResource(@NotNull PackType type, @NotNull ResourceLocation id) {
        final Path path = getPath(getFilename(type, id));
        return path == null ? null : IoSupplier.create(path);
    }

    @Override
    public void listResources(@NotNull PackType type, @NotNull String namespace, @NotNull String path, @NotNull ResourceOutput visitor) {
        if (!namespaces.getOrDefault(type, Collections.emptySet()).contains(namespace)) {
            return;
        }

        for (Path basePath : basePaths) {
            String separator = basePath.getFileSystem().getSeparator();
            Path nsPath = basePath.resolve(type.getDirectory()).resolve(namespace);
            Path searchPath = nsPath.resolve(path.replace("/", separator)).normalize();
            if (!exists(searchPath)) continue;


            try {
                Files.walkFileTree(searchPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        String filename = nsPath.relativize(file).toString().replace(separator, "/");
                        ResourceLocation identifier = ResourceLocation.tryBuild(namespace, filename);

                        if (identifier == null) {
                            LOGGER.error("Invalid path in mod resource-pack {}: {}:{}, ignoring", id, namespace, filename);
                        } else {
                            visitor.accept(identifier, IoSupplier.create(file));
                        }

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                LOGGER.warn("findResources at " + path + " in namespace " + namespace + ", mod " + modInfo + " failed!", e);
            }
        }
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType type) {
        return namespaces.getOrDefault(type, Collections.emptySet());
    }

    @Override
    public <T> T getMetadataSection(@NotNull MetadataSectionSerializer<T> metaReader) throws IOException {
        try (InputStream is = openFile("pack.mcmeta").get()) {
            return AbstractPackResources.getMetadataFromStream(metaReader, is);
        }
    }

    @Override
    public void close() {
        if (closer != null) {
            try {
                closer.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public @NotNull String packId() {
        return name.getString();
    }

    public ResourceLocation getId() {
        return id;
    }

    private static boolean exists(Path path) {
        // NIO Files.exists is notoriously slow when checking the file system
        return path.getFileSystem() == DEFAULT_FS ? path.toFile().exists() : Files.exists(path);
    }

    private static String getFilename(PackType type, ResourceLocation id) {
        return String.format(Locale.ROOT, "%s/%s/%s", type.getDirectory(), id.getNamespace(), id.getPath());
    }
}
