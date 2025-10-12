package de.cristelknight.cristellib.data;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.config.ConfigManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PathFinder {



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
                                 Set<String> copyFile,
                                 Set<String> structureSets) {}

}
