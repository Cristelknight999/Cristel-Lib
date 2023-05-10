package net.cristellib.data;

import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cristellib.CristelLib;
import net.cristellib.CristelLibExpectPlatform;
import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;
import net.cristellib.builtinpacks.BuiltInDataPacks;
import net.cristellib.config.ConfigType;
import net.cristellib.config.ConfigUtil;
import net.minecraft.commands.arguments.ComponentArgument;
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


    public static void getBuiltInPacks(String modid){
        for(Path path : getPathsInDir(modid, "data_packs")){
            InputStream stream;
            try {
                stream = Files.newInputStream(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));
            if(element instanceof JsonObject object){
                String subPath = object.get("sub_path").getAsString();
                String name = object.get("display_name").getAsString();

                boolean b = Conditions.readConditions(object);
                BuiltInDataPacks.registerPack(name, subPath, modid, () -> b);
            }
        }
        checkedConfigFiles = false;
    }

    public static void copyFile(String modid){
        for(Path path : getPathsInDir(modid, "copy_file")){
            InputStream stream;
            try {
                stream = Files.newInputStream(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));
            if(element instanceof JsonObject object){
                String location = object.get("location").getAsString();
                String destination = object.get("destination").getAsString();

                if(Conditions.readConditions(object)){
                    copyFileFromJar(location, destination, modid);
                }
            }
        }
        checkedConfigFiles = false;
    }



    public static void copyFileFromJar(String from, String to, String fromModId){
        List<Path> inputUrl = CristelLibExpectPlatform.getRootPaths(fromModId);
        for(Path p : inputUrl){
            Path fromFile = p.resolve(from);
            File toFile = ConfigUtil.CONFIG_DIR.resolve(to).toFile();
            if(fromFile == null || toFile == null || new File(to).exists()) continue;
            try {
                FileUtils.copyURLToFile(fromFile.toUri().toURL(), toFile);
            } catch (IOException e) {
                CristelLib.LOGGER.error("Couldn't copy file from: " + fromFile + " to: " + toFile, e);
            }
        }
    }


    public static void getStructureConfigs(String modid, Map<String, Set<StructureConfig>> modidAndConfigs, CristelLibRegistry registry){
        Set<StructureConfig> configs = new HashSet<>();
        for(Path path : getPathsInDir(modid, "structure_configs")){
            InputStream stream;
            try {
                stream = Files.newInputStream(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));
            if(element instanceof JsonObject object){
                String subPath = object.get("subPath").getAsString();
                String name = object.get("name").getAsString();


                StructureConfig config = StructureConfig.createWithDefaultConfigPath(subPath, name, ConfigType.valueOf(object.get("config_type").getAsString().toUpperCase()));
                JsonArray sets = object.get("structure_sets").getAsJsonArray();
                for(JsonElement e : sets){
                    if(e instanceof JsonObject o && o.has("structure_set") && o.has("modid")){
                        String mS = o.get("modid").getAsString();
                        JsonArray array = o.get("structure_set").getAsJsonArray();
                        for(JsonElement s : array){
                            if(s instanceof JsonPrimitive primitive){
                                registry.registerSetToConfig(mS, ResourceLocation.tryParse(primitive.getAsString()), config);
                            }
                        }
                    }
                }


                if(object.has("header")){
                    String header = object.get("header").getAsString();
                    if(!header.isEmpty()){
                        config.setHeader(header + "\n");
                    }
                }

                if(object.has("comments")){
                    JsonObject comments = object.get("comments").getAsJsonObject();
                    HashMap<String, String> commentFinalMap = new HashMap<>();
                    Map<String, JsonElement> commentMap = comments.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
                    for(String s : commentMap.keySet()){
                        if(commentMap.get(s) instanceof JsonPrimitive p){
                            commentFinalMap.put(s, p.getAsString());
                        }
                    }
                    if(!commentFinalMap.isEmpty()){
                        config.setComments(commentFinalMap);
                    }
                }

                configs.add(config);
            }
        }
        checkedConfigFiles = false;
        if(configs.isEmpty()) return;
        modidAndConfigs.put(modid, configs);
    }

    public static List<Path> getPathsInDir(String modid, String subPath){
        List<Path> paths = new ArrayList<>();
        findFiles(CristelLibExpectPlatform.getRootPaths(modid), modid, subPath, Files::exists, (path, file) -> {
            if (Files.isRegularFile(file) && file.getFileName().toString().endsWith(".json")) {
                paths.add(file);
            }
            return true;
        }, true, Integer.MAX_VALUE);
        return paths;
    }

    /**
     *
     * Find all files in data/modid/subPath
     * @param modid the modid
     * @param subPath the subPath in data/modid/ where the returned files are
     *
     */
    public static void findFiles(List<Path> rootPaths, String modid, String subPath, Predicate<Path> rootFilter, BiFunction<Path, Path, Boolean> processor, boolean visitAllFiles, int maxDepth) {
        if (modid.equals("minecraft")) return;
        if(!checkedConfigFiles){
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
            walk(ConfigUtil.CONFIG_LIB.resolve(String.format("data/%s", subPath)), rootFilter, processor, visitAllFiles, maxDepth);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void walk(Path root, Predicate<Path> rootFilter, BiFunction<Path, Path, Boolean> processor, boolean visitAllFiles, int maxDepth) throws IOException {
        if (root == null || !Files.exists(root) || !rootFilter.test(root)) {
            return;
        }
        if (processor != null) {
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

}
