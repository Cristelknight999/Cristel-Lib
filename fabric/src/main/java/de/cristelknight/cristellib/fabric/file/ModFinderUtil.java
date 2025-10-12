package de.cristelknight.cristellib.fabric.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class ModFinderUtil {

    public static Set<String> find(String modID) {
        Set<String> structureSets = new HashSet<>();
        PathFinderUtil.getRootPaths(modID).forEach(rootPath -> structureSets.addAll(findSets(rootPath)));
        return structureSets;
    }

    private static Set<String> findSets(Path rootPath) {
        Set<String> structureSets = new HashSet<>();
        Path dataDir = rootPath.resolve("data");
        if(!Files.exists(dataDir)) return structureSets;

        try (Stream<Path> namespaces = Files.list(dataDir)) {
            namespaces.filter(Files::isDirectory).forEach(namespace -> {
                Path structureSetDir = namespace.resolve("worldgen").resolve("structure_set");
                try {
                    PathFinderUtil.walk(structureSetDir, Files::isDirectory, (path, file) -> {
                        if (Files.isRegularFile(file) && file.getFileName().toString().endsWith(".json")) {
                            structureSets.add(file.toString());
                        }
                        return true;
                    }, true, Integer.MAX_VALUE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return structureSets;
    }

}
