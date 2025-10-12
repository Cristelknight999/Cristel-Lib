package de.cristelknight.cristellib.fabric.file;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.autoconfig.ModFinder;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.data.PathFinder;
import de.cristelknight.cristellib.data.ReadData;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class PathFinderUtil {

    public static PathFinder.PathFinderData getSubPathsInMod(String modId, Set<String> modsWithConfig) {
        Set<String> autoConfig = getPathsInDir(modId, "auto_config");
        Set<String> structureConfig = getPathsInDir(modId, "structure_config");
        Set<String> dataPack = getPathsInDir(modId, "data_pack");
        Set<String> copyFile = getPathsInDir(modId, "copy_file");
        ReadData.getAutoConfigSettings(modId, autoConfig);

        Set<String> structureSets = ModFinder.shouldSkipModForAC(modId, modsWithConfig) ? Set.of() : ModFinderUtil.find(modId);

        return new PathFinder.PathFinderData(autoConfig, structureConfig, dataPack, copyFile, structureSets);
    }


    public static Set<String> getPathsInDir(String modId, String subPath) {
        Set<String> paths = new HashSet<>();

        findInFiles(getRootPaths(modId), modId, subPath, Files::exists, (path, file) -> {
            if (Files.isRegularFile(file) && file.getFileName().toString().endsWith(".json")) {
                paths.add(file.toString());
            }
            return true;
        }, true, Integer.MAX_VALUE);
        return paths;
    }

    private static void findInFiles(List<Path> rootPaths, String modId, String subPath, Predicate<Path> rootFilter, BiFunction<Path, Path, Boolean> processor, boolean visitAllFiles, int maxDepth) {
        try {
            if (modId.equals("minecraft")) {
                walk(ConfigManager.CONFIG_LIB.resolve(subPath), rootFilter, processor, visitAllFiles, maxDepth);
                return;
            }

            findInModFiles(rootPaths, modId, String.format("data/cristellib/%s", subPath), rootFilter, processor, visitAllFiles, maxDepth);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private static void findInModFiles(List<Path> rootPaths, String modId, String subPath, Predicate<Path> rootFilter, BiFunction<Path, Path, Boolean> processor, boolean visitAllFiles, int maxDepth) throws IOException {
        boolean hasOldPath = false;
        boolean hasNewPath = false;
        for (var root : rootPaths) {
            Path newPath = root.resolve(subPath);

            if (!hasOldPath) hasOldPath = Files.exists(root.resolve(subPath + "s"));
            if (!hasNewPath) hasNewPath = Files.exists(newPath);

            walk(newPath, rootFilter, processor, visitAllFiles, maxDepth);
        }
        if (hasOldPath && !hasNewPath)
            CristelLib.LOGGER.warn("Mod with id: {} only has an old path for subPath: {}. New Path for Cristel Lib >=2.0.1 is missing! Maybe contact the mod author to let them know.", modId, subPath);

    }

    public static void walk(Path root, Predicate<Path> rootFilter, BiFunction<Path, Path, Boolean> processor, boolean visitAllFiles, int maxDepth) throws IOException {
        if (root == null || !Files.exists(root) || !rootFilter.test(root)) {
            return;
        }
        if (processor == null) return;
        try (var stream = Files.walk(root, maxDepth)) {
            Iterator<Path> itr = stream.iterator();

            while (itr.hasNext()) {
                boolean keepGoing = processor.apply(root, itr.next());
                if (!visitAllFiles && !keepGoing) {
                    return;
                }
            }
        }
    }


    // Internal
    public static List<Path> getRootPaths(String modId) {
        ModContainer container = FabricLoader.getInstance().getModContainer(modId).orElse(null);
        List<Path> paths = new ArrayList<>();
        if(container != null){
            paths = container.getRootPaths();
        }
        return paths;
    }

}
