package de.cristelknight.cristellib.data;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.autoconfig.ModFinder;
import de.cristelknight.cristellib.config.ConfigManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PathFinder {

    public static PathFinder.PathFinderData getSubPathsInMod(String modId, Set<String> modsWithConfig) {
        //long startTime = System.nanoTime(); // start profiling

        Set<String> autoConfig = new HashSet<>();
        Set<String> structureConfig = new HashSet<>();
        Set<String> dataPack = new HashSet<>();
        Set<String> structureSets = ModFinder.shouldSkipModForACPre(modId, modsWithConfig) ? null : new HashSet<>();

        try {
            if (modId.equals(CristelLib.MC_ID)) {
                // Keep this logic unchanged: walks real config folder
                walk(ConfigManager.CONFIG_LIB,
                        path -> Files.isRegularFile(path) && path.toString().endsWith(".json"),
                        p -> categorizePath(p, autoConfig, structureConfig, dataPack, null));
            } else {
                // Walk all files under data/ once

                CristelLibExpectPlatform.findInModFiles(
                        modId,
                        "data",
                        path -> path.toString().endsWith(".json"),
                        p -> categorizePath(p, autoConfig, structureConfig, dataPack, structureSets)
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*
        long endTime = System.nanoTime(); // end profiling
        double durationMs = (endTime - startTime) / 1_000_000.0;
        CristelLib.LOGGER.error("Scanned mod {} in {}ms", modId, durationMs);
         */

        return new PathFinderData(autoConfig, structureConfig, dataPack, structureSets == null ?
                Set.of() :
                structureSets
        );
    }

    private static void categorizePath(String path,
                                       Set<String> autoConfig,
                                       Set<String> structureConfig,
                                       Set<String> dataPack,
                                       Set<String> structureSets) {

        // Normalize slashes for consistency across OSes
        String normalized = path.replace('\\', '/');
        path = normalized;

        if(!normalized.startsWith("/")) normalized = "/" + normalized;

        if (!normalized.startsWith("/data/")) {
            return;
        }

        if (structureSets != null && normalized.matches("^/data/[^/]+/worldgen/structure_set/.*")) {
            structureSets.add(normalized);
            return;
        }

        if (!normalized.startsWith("/data/cristellib/")) {
            return;
        }

        if (normalized.startsWith("/data/cristellib/structure_config/")) {
            structureConfig.add(path);
        } else if (normalized.startsWith("/data/cristellib/data_pack/")) {
            dataPack.add(path);
        } else if (normalized.startsWith("/data/cristellib/auto_config/")) {
            autoConfig.add(path);
        }
    }


    public static void walk(Path root, Predicate<Path> fileFilter, Consumer<String> consumer) throws IOException {
        if (root == null || !Files.exists(root)) return;

        try (var stream = Files.walk(root, Integer.MAX_VALUE)) {
            for (Path subPath : (Iterable<Path>) stream::iterator) {
                if (fileFilter.test(subPath)) {
                    consumer.accept(subPath.toString());
                }
            }
        }
    }

    public record PathFinderData(Set<String> autoConfig,
                                 Set<String> structureConfig,
                                 Set<String> dataPack,
                                 Set<String> structureSets) {}

}
