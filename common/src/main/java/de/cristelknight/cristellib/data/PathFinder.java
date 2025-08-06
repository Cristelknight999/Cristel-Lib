package de.cristelknight.cristellib.data;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.config.ConfigManager;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class PathFinder {

    public static List<Path> getPathsInDir(String modId, String subPath) {
        List<Path> paths = new ArrayList<>();

        findInFiles(CristelLibExpectPlatform.getRootPaths(modId), modId, subPath, Files::exists, (path, file) -> {
            if (Files.isRegularFile(file) && file.getFileName().toString().endsWith(".json")) {
                paths.add(file);
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
            CristelLib.LOGGER.error("Mod with id {} only has an old path for subPath {}. New Path for Cristel Lib >=2.0.1 is missing! Maybe contact the mod author to let them know.", modId, subPath);

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



}
