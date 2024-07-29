package de.cristelknight.cristellib.data;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.builtinpacks.BuiltInDataPacks;
import de.cristelknight.cristellib.config.ConfigType;
import de.cristelknight.cristellib.config.ConfigUtil;
import de.cristelknight.cristellib.util.JanksonUtil;
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

    public static void getBuiltInPacks(String modId){
        for(Path path : getPathsInDir(modId, "data_packs")){
            InputStream stream;
            try {
                stream = Files.newInputStream(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));
            if(element instanceof JsonObject object){
                if(object.has("packs")){
                    JsonArray array = object.getAsJsonArray("packs");
                    for(int i = array.size() - 1; i >= 0; i--){
                        JsonElement jsonElement = array.get(i);
                        loadPack(jsonElement, modId);
                    }
                }
                else loadPack(object, modId);

            }
        }
        checkedConfigFiles = false;
    }


    public static void copyFile(String modId){
        for(Path path : getPathsInDir(modId, "copy_file")){
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
                    copyFileFromJar(location, destination, modId);
                }
            }
        }
        checkedConfigFiles = false;
    }
    
    public static void loadPack(JsonElement element, String modId){
        if(element instanceof JsonObject object){
            String location = object.get("location").getAsString();
            String name = object.get("display_name").getAsString();

            ResourceLocation rl = ResourceLocation.tryParse(location);
            CristelLib.LOGGER.error(rl);

            Component component = Component.literal(name);

                /*
                LocalPlayer access = Minecraft.getInstance().player;
                if(access != null){
                    try {
                        component = ComponentArgument.textComponent(CommandBuildContext.simple(access.registryAccess(), FeatureFlags.VANILLA_SET)).parse(new StringReader(name));
                    } catch (CommandSyntaxException e) {
                        CristelLib.LOGGER.debug("Couldn't parse: \"" + name + "\" to a component", e);
                    }
                }
                 */


            boolean b = Conditions.readConditions(object);
            BuiltInDataPacks.registerPack(rl, modId, component, () -> b);
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





    public static void copyFileFromJar(String from, String to, String frommodId){
        List<Path> inputUrl = CristelLibExpectPlatform.getRootPaths(frommodId);
        for(Path p : inputUrl){
            Path fromFile = p.resolve(from);
            File toFile = ConfigUtil.CONFIG_DIR.resolve(to).toFile();
            if(fromFile == null || toFile == null || toFile.exists()) continue;
            try {
                FileUtils.copyURLToFile(fromFile.toUri().toURL(), toFile);
            } catch (IOException e) {
                CristelLib.LOGGER.error("Couldn't copy file from: " + fromFile + " to: " + toFile, e);
            }
        }
    }


    public static void getStructureConfigs(String modId, Map<String, Set<StructureConfig>> modIdAndConfigs, CristelLibRegistry registry){
        Set<StructureConfig> configs = new HashSet<>();
        for(Path path : getPathsInDir(modId, "structure_configs")){
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
                    if(e instanceof JsonObject o && o.has("structure_set") && o.has("modId")){
                        String mS = o.get("modId").getAsString();
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
                    Map<String, JsonElement> commentMap = comments.asMap();
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
        modIdAndConfigs.put(modId, configs);
    }

    public static List<Path> getPathsInDir(String modId, String subPath){
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
     *
     * Find all files in data/modId/subPath
     * @param modId the modId
     * @param subPath the subPath in data/modId/ where the returned files are
     *
     */
    public static void findFiles(List<Path> rootPaths, String modId, String subPath, Predicate<Path> rootFilter, BiFunction<Path, Path, Boolean> processor, boolean visitAllFiles, int maxDepth) {
        if (modId.equals("minecraft")) return;
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
