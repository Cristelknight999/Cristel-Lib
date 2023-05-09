package net.cristellib.forge.extrapackutil;

import com.google.common.base.Charsets;
import net.cristellib.CristelLibExpectPlatform;
import net.minecraft.ResourceLocationException;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ModResourcePack extends AbstractPackResources {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModResourcePack.class);
    private static final Pattern RESOURCE_PACK_PATH = Pattern.compile("[a-z0-9-_.]+");
    private static final FileSystem DEFAULT_FS = FileSystems.getDefault();

    private final String name;
    private final IModInfo modInfo;
    private final List<Path> basePaths;
    private final AutoCloseable closer;
    private final Map<PackType, Set<String>> namespaces;

    public static ModResourcePack create(String name, IModInfo mod, String subPath) {
        List<Path> rootPaths = CristelLibExpectPlatform.getRootPaths(mod.getModId());
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

        if (paths.isEmpty()) return null;

        ModResourcePack ret = new ModResourcePack(name, mod, paths, null);

        return ret.getNamespaces(PackType.SERVER_DATA).isEmpty() ? null : ret;
    }

    private ModResourcePack(String name, IModInfo modInfo, List<Path> paths, AutoCloseable closer) {
        super(null);
        this.name = name;
        this.modInfo = modInfo;
        this.basePaths = paths;
        this.closer = closer;
        this.namespaces = readNamespaces(paths, modInfo.getModId());
    }

    private static Map<PackType, Set<String>> readNamespaces(List<Path> paths, String modId) {
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

    private static final String resPrefix = PackType.CLIENT_RESOURCES.getDirectory()+"/";
    private static final String dataPrefix = PackType.SERVER_DATA.getDirectory()+"/";

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

    @Override
    protected InputStream getResource(String filename) throws IOException {
        InputStream stream;

        Path path = getPath(filename);

        if (path != null && Files.isRegularFile(path)) {
            return Files.newInputStream(path);
        }

        stream = openDefault(this.modInfo,  filename);

        if (stream != null) {
            return stream;
        }

        // ReloadableResourceManagerImpl gets away with FileNotFoundException.
        throw new FileNotFoundException("\"" + filename + "\" in Fabric mod \"" + modInfo.getModId() + "\"");
    }

    @Override
    protected boolean hasResource(String filename) {
        if ("pack.mcmeta".equals(filename)) {
            return true;
        }

        Path path = getPath(filename);
        return path != null && Files.isRegularFile(path);
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType type, String namespace, String path, int depth, Predicate<String> predicate) {
        if (type.equals(PackType.CLIENT_RESOURCES) || !namespaces.getOrDefault(type, Collections.emptySet()).contains(namespace)) {
            return Collections.emptyList();
        }

        List<ResourceLocation> ids = new ArrayList<>();

        for (Path basePath : basePaths) {
            String separator = basePath.getFileSystem().getSeparator();
            Path nsPath = basePath.resolve(type.getDirectory()).resolve(namespace);
            Path searchPath = nsPath.resolve(path.replace("/", separator)).normalize();
            if (!exists(searchPath)) continue;

            try {
                Files.walkFileTree(searchPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        String fileName = file.getFileName().toString();

                        if (!fileName.endsWith(".mcmeta")
                                && predicate.test(fileName)) {
                            try {
                                ids.add(new ResourceLocation(namespace, nsPath.relativize(file).toString().replace(separator, "/")));
                            } catch (ResourceLocationException e) {
                                LOGGER.error(e.getMessage());
                            }
                        }

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                LOGGER.warn("findResources at " + path + " in namespace " + namespace + ", mod " + modInfo.getModId() + " failed!", e);
            }
        }

        return ids;
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return namespaces.getOrDefault(type, Collections.emptySet());
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
    public String getName() {
        return name;
    }

    private static boolean exists(Path path) {
        // NIO Files.exists is notoriously slow when checking the file system
        return path.getFileSystem() == DEFAULT_FS ? path.toFile().exists() : Files.exists(path);
    }

    public static InputStream openDefault(IModInfo info, String filename) {
        switch (filename) {
            case "pack.mcmeta":
                String description = info.getDisplayName();

                if (description == null) {
                    description = "";
                } else {
                    description = description.replaceAll("\"", "\\\"");
                }

                String pack = String.format("{\"pack\":{\"pack_format\":" + PackType.SERVER_DATA.getVersion(SharedConstants.getCurrentVersion()) + ",\"description\":\"%s\"}}", description);
                return IOUtils.toInputStream(pack, Charsets.UTF_8);
            default:
                return null;
        }
    }
}
