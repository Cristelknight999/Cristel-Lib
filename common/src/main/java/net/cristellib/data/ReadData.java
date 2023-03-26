package net.cristellib.data;

import com.google.gson.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cristellib.CristelLib;
import net.cristellib.CristelLibExpectPlatform;
import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;
import net.cristellib.api.builtinpacks.BuiltInDataPacks;
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
                String location = object.get("location").getAsString();
                String name = object.get("display_name").getAsString();

                ResourceLocation rl = ResourceLocation.tryParse(location);

                Component component = Component.literal(name);
                try {
                    component = ComponentArgument.textComponent().parse(new StringReader(name));
                } catch (CommandSyntaxException e) {
                    CristelLib.LOGGER.debug("Couldn't parse: \"" + name + "\" to a component", e);
                }

                boolean b = Conditions.readCondition(object);
                BuiltInDataPacks.registerPack(rl, modid, component, () -> b);
            }
        }
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

                if(Conditions.readCondition(object)){
                    copyFileFromJar(location, destination, modid);
                }
            }
        }
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
                String header = object.get("header").getAsString();
                JsonObject comments = object.get("comments").getAsJsonObject();
                HashMap<String, String> commentFinalMap = new HashMap<>();
                Map<String, JsonElement> commentMap = comments.asMap();
                for(String s : commentMap.keySet()){
                    JsonElement e = commentMap.get(s);
                    if(e instanceof JsonPrimitive p){
                        commentFinalMap.put(s, p.getAsString());
                    }
                }

                StructureConfig config = StructureConfig.createWithDefaultConfigPath(subPath, name, ConfigType.valueOf(object.get("config_type").getAsString()));
                JsonArray sets = object.get("structure_sets").getAsJsonArray();
                for(JsonElement e : sets){
                    if(e instanceof JsonObject o){
                        if(o.has("structure_set")){
                            String mS = modid;
                            if(o.has("modid")){
                                mS = o.get("modid").getAsString();
                            }
                            registry.registerSetToConfig(mS, ResourceLocation.tryParse(o.get("structure_set").getAsString()), config);
                        }

                    }
                }


                if(!header.isEmpty()){
                    config.setHeader(header + "\n");
                }
                if(!commentFinalMap.isEmpty()){
                    config.setComments(commentFinalMap);
                }
                configs.add(config);
            }
        }
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
        try {
            for (var root : rootPaths) {
                walk(root.resolve(String.format("data/%s/%s", modid, subPath)), rootFilter, processor, visitAllFiles, maxDepth);
            }
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
