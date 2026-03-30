package de.cristelknight.cristellib.data;

import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.autoconfig.ModFinder;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.platform.Services;
import de.cristelknight.cristellib.util.FileHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class PathFinder {

    // TODO: improve this for Fabric
    public static PathFinderData getSubPathsInMod(String modId, Set<String> modsWithConfig) {
        //long startTime = System.nanoTime(); // start profiling

        Set<String> autoConfig = new HashSet<>();
        Set<String> structureConfig = new HashSet<>();
        Set<String> dataPack = new HashSet<>();
        Set<String> structureSets = ModFinder.shouldSkipModForACPre(modId, modsWithConfig) ? null : new HashSet<>();

        try {
            if (modId.equals(Constants.MC_ID)) {
                Predicate<Path> filter = path -> Files.isRegularFile(path) && path.toString().endsWith(".json");
                walk(ConfigManager.CONFIG_LIB.resolve("structure_config"),
                        filter,
                        structureConfig::add);
                walk(ConfigManager.CONFIG_LIB.resolve("data_pack"),
                        filter,
                        dataPack::add);
            } else {
                Services.PLATFORM.findInModFiles(
                        modId,
                        "data",
                        path -> path.toString().endsWith(".json"),
                        p -> categorizePath(p, autoConfig, structureConfig, dataPack, structureSets)
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(Constants.getWithPrefix("Error while trying to walk through mod files"), e);
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

    private static final Pattern STRUCTURE_SET = Pattern.compile("data/[^/]+/worldgen/structure_set/.*");

    private static void categorizePath(String path,
                                       Set<String> autoConfig,
                                       Set<String> structureConfig,
                                       Set<String> dataPack,
                                       Set<String> structureSets) {

        // Normalize slashes for consistency across OSes
        path = FileHelper.normalizeResourcePath(path);

        if (!path.startsWith("data/")) {
            return;
        }

        if (structureSets != null && STRUCTURE_SET.matcher(path).matches()) {
            structureSets.add(path);
            return;
        }

        if (!path.startsWith("data/cristellib/")) {
            return;
        }

        if (path.startsWith("data/cristellib/structure_config/")) {
            structureConfig.add(path);
        } else if (path.startsWith("data/cristellib/data_pack/")) {
            dataPack.add(path);
        } else if (path.startsWith("data/cristellib/auto_config/")) {
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
