package de.cristelknight.cristellib.data;

import com.mojang.datafixers.util.Either;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.builtinpacks.BuiltInDataPackLoader;
import de.cristelknight.cristellib.config.ConfigManager;
import de.cristelknight.cristellib.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ReadData {

    private static boolean checkedConfigFiles = false;

    public static void getStructureConfigs(String modId, Map<String, Set<StructureConfig>> modIdAndConfigs) {
        Set<StructureConfig> configs = new HashSet<>();
        for (Path path : getPathsInDir(modId, "structure_config")) {
            StructureConfig config = ConfigManager.readFromJsonPath(String.format("Couldn't read %s, crashing instead. This file is corrupted!", path),
                    path, StructureConfig.CODEC);

            configs.add(config);
        }
        checkedConfigFiles = false;
        if (configs.isEmpty()) return;
        modIdAndConfigs.put(modId, configs);
    }

    public static void getBuiltInPacks(String modId) {
        for (Path path : getPathsInDir(modId, "data_pack")) {

            Either<BuiltInPackData, BuiltInPackDataWrapper> either = ConfigManager.readFromJsonPath(
                    String.format("Couldn't read %s, crashing instead. This file is corrupted!", path),
                    path, BuiltInPackData.PACKS_CODEC);

            either.left().ifPresent(ReadData::loadPack);
            either.right().ifPresent(wrapper -> {
                List<BuiltInPackData> packs = new ArrayList<>(wrapper.packs());
                Collections.reverse(packs);
                packs.forEach(ReadData::loadPack);
            });
        }
        checkedConfigFiles = false;
    }

    public static void loadPack(BuiltInPackData pack) {
        boolean bl = Conditions.readConditions(pack.conditions());
        BuiltInDataPackLoader.registerPack(pack.location(), Component.nullToEmpty(pack.displayName()), () -> bl);
    }


    public static void copyFile(String modId) {
        for (Path path : getPathsInDir(modId, "copy_file")) {

            CopyFileData copyFileData = ConfigManager.readFromJsonPath(String.format("Couldn't read %s, crashing instead. This file is corrupted!", path),
                    path, CopyFileData.CODEC);

            if (Conditions.readConditions(copyFileData.conditions())) {
                copyFileFromJar(copyFileData.location(), copyFileData.destination());
            }
        }
        checkedConfigFiles = false;
    }

    public static void copyFileFromJar(ResourceLocation from, String to) {
        String modID = from.getNamespace();
        String location = from.getPath();

        List<Path> inputUrl = CristelLibExpectPlatform.getRootPaths(modID);
        for (Path p : inputUrl) {
            Path fromFile = p.resolve(location);
            File toFile = Util.pathFromString(to).toFile();
            if (fromFile == null || toFile == null || toFile.exists()) continue;
            try {
                FileUtils.copyURLToFile(fromFile.toUri().toURL(), toFile);
            } catch (IOException e) {
                CristelLib.LOGGER.error("Couldn't copy file from: {} to: {}", fromFile, toFile, e);
            }
        }
    }

    public static List<Path> getPathsInDir(String modId, String subPath) {
        List<Path> paths = new ArrayList<>();


        findFiles(CristelLibExpectPlatform.getRootPaths(modId), modId, String.format("data/cristellib/%s", subPath), Files::exists, (path, file) -> {
            if (Files.isRegularFile(file) && file.getFileName().toString().endsWith(".json")) {
                paths.add(file);
            }
            return true;
        }, true, Integer.MAX_VALUE);
        return paths;
    }

    /**
     * @param modId   the modId
     * @param subPath the subPath where the requested files are
     */
    public static void findFiles(List<Path> rootPaths, String modId, String subPath, Predicate<Path> rootFilter, BiFunction<Path, Path, Boolean> processor, boolean visitAllFiles, int maxDepth) {
        if (modId.equals("minecraft")) return;
        if (!checkedConfigFiles) {
            findInConfigFiles(subPath, rootFilter, processor, visitAllFiles, maxDepth);
            checkedConfigFiles = true;
        }
        try {
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

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }


    public static void findInConfigFiles(String subPath, Predicate<Path> rootFilter, BiFunction<Path, Path, Boolean> processor, boolean visitAllFiles, int maxDepth) {
        try {
            walk(ConfigManager.CONFIG_LIB.resolve(subPath), rootFilter, processor, visitAllFiles, maxDepth);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void walk(Path root, Predicate<Path> rootFilter, BiFunction<Path, Path, Boolean> processor, boolean visitAllFiles, int maxDepth) throws IOException {
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
