package de.cristelknight.cristellib.neoforge;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.autoconfig.ModFinder;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.data.PathFinder;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.jarcontents.JarResource;
import net.neoforged.fml.jarcontents.JarResourceVisitor;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static de.cristelknight.cristellib.neoforge.ModLoadingUtilImpl.getPreLoadedModInfo;

public class PathFinderUtil {

    public static PathFinder.PathFinderData getSubPathsInMod(String modId, Set<String> modsWithConfig) {
        long startTime = System.nanoTime(); // start profiling

        Set<String> autoConfig = new HashSet<>();
        Set<String> structureConfig = new HashSet<>();
        Set<String> dataPack = new HashSet<>();
        Set<String> copyFile = new HashSet<>();
        Set<String> structureSets = new HashSet<>();

        try {
            if (modId.equals("minecraft")) {
                // Keep this logic unchanged: walks real config folder
                PathFinder.walk(ConfigManager.CONFIG_LIB,
                        path -> Files.isRegularFile(path) && path.toString().endsWith(".json"),
                        p -> categorizePath(p, autoConfig, structureConfig, dataPack, copyFile, null));
            } else {
                // Walk all files under data/ once

                findInModFiles(
                        modId,
                        "data/",
                        path -> path.toString().endsWith(".json"),
                        p -> categorizePath(p, autoConfig, structureConfig, dataPack, copyFile, structureSets)
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long endTime = System.nanoTime(); // end profiling
        double durationMs = (endTime - startTime) / 1_000_000.0;
        CristelLib.LOGGER.error("Scanned mod {} in {}ms", modId, durationMs);


        PathFinder.PathFinderData data = new PathFinder.PathFinderData(autoConfig, structureConfig, dataPack, copyFile, structureSets == null ?
                Set.of() :
                structureSets
        );
        CristelLib.LOGGER.error(data.toString());
        return data;
    }

    private static void categorizePath(String path,
                                       Set<String> autoConfig,
                                       Set<String> structureConfig,
                                       Set<String> dataPack,
                                       Set<String> copyFile,
                                       Set<String> structureSets) {

        // Normalize slashes for consistency across OSes
        String normalized = path.replace('\\', '/');

        if (!normalized.startsWith("data/")) {
            return;
        }

        if (structureSets != null && normalized.matches("^data/[^/]+/worldgen/structure_set/.*")) {
            structureSets.add(normalized);
            return;
        }

        if (!normalized.startsWith("data/cristellib/")) {
            return;
        }

        if (normalized.startsWith("data/cristellib/structure_config/")) {
            structureConfig.add(normalized);
        } else if (normalized.startsWith("data/cristellib/data_pack/")) {
            dataPack.add(normalized);
        } else if (normalized.startsWith("data/cristellib/auto_config/")) {
            autoConfig.add(normalized);
        } else if (normalized.startsWith("data/cristellib/copy_file/")) {
            copyFile.add(normalized);
        }
    }

    public static void findInModFiles(String modId, String startingFolder, Predicate<Path> fileFilter, Consumer<String> consumer) {
        IModFile file =  getModFile(modId);
        if(file == null) return;
        JarContents contents = file.getContents();

        if(!contents.containsFile(startingFolder)) return;

        walk(contents, startingFolder, fileFilter, consumer);


        // old stuff
        for(String subPath : Set.of("data/cristellib/structure_config/", "data/cristellib/data_pack/", "data/cristellib/copy_file/")) {
            if (contents.containsFile(subPath + "s") && !contents.containsFile(subPath))
                CristelLib.LOGGER.warn("Mod with id: {} only has an old path for subPath: {}. New Path for Cristel Lib >=2.0.1 is missing! Maybe contact the mod author to let them know.", modId, startingFolder);
        }
    }

    // Internal
    public static IModFile getModFile(String modId) {
        ModList modList = ModList.get();
        IModFile file;
        if (modList == null) {
            ModInfo info = getPreLoadedModInfo(modId);
            if (info == null) {
                CristelLib.LOGGER.warn("Mod info for modId: {} is null", modId);
                return null;
            }
            file = info.getOwningFile().getFile();
        } else {
            ModContainer container = modList.getModContainerById(modId).orElse(null);
            if (container == null) {
                CristelLib.LOGGER.warn("Mod container for modId: {} is null", modId);
                return null;
            }
            file = container.getModInfo().getOwningFile().getFile();
        }
        return file;
    }

    private static void walk(JarContents contents, String startingFolder, Predicate<Path> fileFilter, Consumer<String> consumer) {
        var visitor = new JarResourceVisitor() {
            @Override
            public void visit(String relativePath, JarResource resource) {
                if(!fileFilter.test(Path.of(relativePath))) return;
                consumer.accept(relativePath);
            }
        };

        contents.visitContent(startingFolder, visitor);
    }
}
