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
        for (Path path : getPathsInDir(modId, "structure_configs")) {
            StructureConfig config = ConfigManager.readFromJsonPath(String.format("Couldn't read %s, crashing instead. This file is corrupted!", path),
                    path, StructureConfig.CODEC);

            configs.add(config);
        }
        checkedConfigFiles = false;
        if (configs.isEmpty()) return;
        modIdAndConfigs.put(modId, configs);
    }

    public static void getBuiltInPacks(String modId) {
        for (Path path : getPathsInDir(modId, "data_packs")) {

            Either<BuiltInPackData, List<BuiltInPackData>> either = ConfigManager.readFromJsonPath(
                    String.format("Couldn't read %s, crashing instead. This file is corrupted!", path),
                    path, BuiltInPackData.PACKS_CODEC);

            either.left().ifPresent(ReadData::loadPack);
            either.right().ifPresent(packs -> packs.forEach(ReadData::loadPack));

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


    


    /*
    public static void modifyJson5File(String modId){
        for(Path path : getPathsInDir(modId, "modify_file")){
            InputStream stream;
            try {
                stream = Files.newInputStream(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));
            if(element instanceof JsonObject object && Conditions.readConditions(object)){
                String location = object.get("location").getAsString();
                String destination = object.get("path").getAsString();
                JsonObject objects = object.get("objects").getAsJsonObject();

                List<Pair<String, String>> strings = new ArrayList<>();
                for(String s : objects.keySet()){
                    JsonElement e = objects.get(s);
                    strings.add(new Pair<>(s, e.getAsString()));
                }
                modifyObject(destination, strings, location);
            }
        }
        checkedConfigFiles = false;
    }


    public static void modifyObject(String modifier, List<Pair<String, String>> strings, String at){
        Path toFile = ConfigUtil.CONFIG_DIR.resolve(at);
        if(!toFile.toFile().exists() || !toFile.endsWith("json5")) return;

        try{
            blue.endless.jankson.JsonObject load = ConfigUtil.JANKSON.load(toFile.toFile());
            JanksonUtil.addToObject(load, modifier, strings);

            Files.createDirectories(toFile.getParent());
            String output = load.toJson(ConfigUtil.JSON_GRAMMAR);
            Files.write(toFile, output.getBytes());

        } catch (Exception errorMsg) {
            CristelLib.LOGGER.error("Couldn't read " + toFile + "can't modify it");
        }
    }
     */


    public static List<Path> getPathsInDir(String modId, String subPath) {
        List<Path> paths = new ArrayList<>();
        findFiles(CristelLibExpectPlatform.getRootPaths(modId), modId, subPath, Files::exists, (path, file) -> {
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
            for (var root : rootPaths) {
                walk(root.resolve(String.format("data/cristellib/%s", subPath)), rootFilter, processor, visitAllFiles, maxDepth);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }


    public static void findInConfigFiles(String subPath, Predicate<Path> rootFilter, BiFunction<Path, Path, Boolean> processor, boolean visitAllFiles, int maxDepth) {
        try {
            walk(ConfigManager.CONFIG_LIB.resolve(String.format("data/%s", subPath)), rootFilter, processor, visitAllFiles, maxDepth);
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
