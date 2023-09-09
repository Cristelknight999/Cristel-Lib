package net.cristellib.config;

import blue.endless.jankson.*;
import com.google.gson.JsonParser;
import net.cristellib.CristelLib;
import net.cristellib.CristelLibExpectPlatform;
import net.cristellib.StructureConfig;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigUtil {

    public static final Path CONFIG_DIR = CristelLibExpectPlatform.getConfigDirectory();

    public static final Path CONFIG_LIB = CONFIG_DIR.resolve("cristellib");

    public static final Jankson JANKSON = Jankson.builder().build();

    public static final Supplier<JsonGrammar.Builder> JSON_GRAMMAR_BUILDER = () -> new JsonGrammar.Builder().withComments(true).bareSpecialNumerics(true).printCommas(true);

    public static final JsonGrammar JSON_GRAMMAR = JSON_GRAMMAR_BUILDER.get().build();



    public static void createConfig(StructureConfig config) {
        Path path = config.getPath();
        if(path.toFile().exists()) return;

        Map<ResourceLocation, List<String>> sets = config.getStructures();
        JsonObject object = new JsonObject();
        JsonElement jsonBoolean = JsonPrimitive.of(true);
        for(ResourceLocation location : sets.keySet()){
            JsonObject inObjectObject = new JsonObject();
            Map<String, JsonObject> l = new HashMap<>();
            for(String s : sets.get(location)){
                String sWN = s.split(":")[1];
                if(sWN.contains("/")){
                    String key = sWN.split("/")[0];
                    String value = sWN.split("/")[1];
                    if(!l.containsKey(key)){
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.put(value, jsonBoolean);
                        l.put(key, jsonObject);
                    }
                    else l.get(key).put(value, jsonBoolean);
                } else inObjectObject.put(sWN, jsonBoolean);
            }
            for(String s : l.keySet()) inObjectObject.put(s, l.get(s));
            object.put(location.toString().split(":")[1], inObjectObject);
        }

        addComments(config.getComments(), object, "");
        try {
            Files.createDirectories(path.getParent());
            String output = config.getHeader() + "\n" + object.toJson(JSON_GRAMMAR);
            Files.write(path, output.getBytes());
        } catch (IOException e) {
            CristelLib.LOGGER.error("Couldn't create Enable Disable Config for Path {}", path, e);
        }
    }

    public static void createPlacementConfig(StructureConfig config) {
        Path path = config.getPath();
        if(path.toFile().exists()) return;

        Map<ResourceLocation, Placement> sets = config.getStructurePlacement();
        JsonObject object = new JsonObject();

        for(ResourceLocation location : sets.keySet()){
            JsonElement element = JANKSON.toJson(sets.get(location));
            if(element instanceof JsonObject object1){
                if(object1.getDouble("frequency", 0F) == 0F){
                    object1.remove("frequency");
                }
            }
            object.put(location.toString().split(":")[1], element);
        }
        addComments(config.getComments(), object, "");
        try {
            Files.createDirectories(path.getParent());
            String output = config.getHeader() + "\n" + object.toJson(JSON_GRAMMAR);
            Files.write(path, output.getBytes());
        } catch (IOException e) {
            CristelLib.LOGGER.error("Couldn't create Placement Config for Path {}", path, e);
        }
    }




    public static Map<String, Placement> readPlacementConfig(Path path) {
        try{
            JsonObject load = JANKSON.load(path.toFile());
            return stringPlacementMap(load);
        } catch (Exception errorMsg) {
            throw new IllegalArgumentException("Couldn't read " + path + ", crashing instead. Maybe try to delete the config files!");
        }
    }

    public static Map<String, Placement> stringPlacementMap(JsonObject object){
        Map<String, Placement> map = new HashMap<>();
        for(Map.Entry<String, JsonElement> entry : object.entrySet()){
            if(entry.getValue() instanceof JsonObject jsonObject){
                /*
                StructurePlacement placement = StructurePlacement.CODEC.decode(JsonOps.INSTANCE, object).result().get().getFirst();
                if(placement.type().equals(StructurePlacementType.RANDOM_SPREAD)){
                    RandomSpreadStructurePlacement placement1 = RandomSpreadStructurePlacement.CODEC.decode(JsonOps.INSTANCE, object).result().get().getFirst();
                }
                else if(placement.type().equals(StructurePlacementType.CONCENTRIC_RINGS)){
                    ConcentricRingsStructurePlacement placement1 = ConcentricRingsStructurePlacement.CODEC.decode(JsonOps.INSTANCE, object).result().get().getFirst();
                }
                 */
                map.put(entry.getKey(), JANKSON.fromJson(jsonObject, Placement.class));
            }

        }
        return map;
    }


    public static Map<String, Boolean> readConfig(Path path) {
        try{
            JsonObject load = JANKSON.load(path.toFile());
            return newStringBooleanMap(load);
        } catch (Exception errorMsg) {
            throw new IllegalArgumentException("Couldn't read " + path + ", crashing instead. Maybe try to delete the config files!");
        }
    }

    /*
    public static Map<String, Boolean> stringBooleanMap(JsonObject object){
        Map<String, Boolean> map = new HashMap<>();
        for(Map.Entry<String, JsonElement> entry : object.entrySet()){
            String key = entry.getKey();
            JsonElement e = entry.getValue();
            if(e instanceof JsonPrimitive primitive && primitive.getValue() instanceof Boolean bool){
                CristelLib.LOGGER.error("Normal: " + key);
                map.put(key, bool);
            }
            else if(e instanceof JsonObject jsonObject){
                map.putAll(stringBooleanMap(jsonObject));
            }
        }
        return map;
    }
     */

    public static Map<String, Boolean> stringBooleanMap(JsonObject object, String parent){
        Map<String, Boolean> map = new HashMap<>();
        for(Map.Entry<String, JsonElement> entry : object.entrySet()){
            String key = parent.equals("") ? entry.getKey() : parent + "/" + entry.getKey();
            JsonElement e = entry.getValue();
            if(e instanceof JsonPrimitive primitive && primitive.getValue() instanceof Boolean bool){
                map.put(key, bool);
            }
            else if(e instanceof JsonObject jsonObject){
                map.putAll(stringBooleanMap(jsonObject, entry.getKey()));
            }
        }
        return map;
    }

    public static Map<String, Boolean> newStringBooleanMap(JsonObject object){
        Map<String, Boolean> map = new HashMap<>();
        for(Map.Entry<String, JsonElement> entry : object.entrySet()){
            JsonElement e = entry.getValue();
            if(e instanceof JsonObject jsonObject){
                map.putAll(stringBooleanMap(jsonObject, ""));
            }
        }
        return map;
    }

    public static @Nullable  com.google.gson.JsonElement getSetElement(String getDataFromModId, ResourceLocation location) {
        return getElement(getDataFromModId, "data/" + location.getNamespace() + "/worldgen/structure_set/" + location.getPath() + ".json");
    }
    public static @Nullable com.google.gson.JsonElement getElement(String getDataFromModId, String location) {
        InputStream im;
        Path pathC = CristelLibExpectPlatform.getResourceDirectory(getDataFromModId, location);
        //CristelLib.LOGGER.warn("PathC: " + pathC + " Location Path: " + location);

        if(pathC == null) return null;
        try {
            im = Files.newInputStream(pathC);
        } catch (IOException e) {
            CristelLib.LOGGER.warn("Couldn't create Input Stream for Path " + pathC, e);
            return null;
        }
        try (InputStreamReader reader = new InputStreamReader(im)) {
            return JsonParser.parseReader(reader);
        } catch (IOException e) {
            CristelLib.LOGGER.warn("Couldn't read " + location + " from mod: " + getDataFromModId, e);
            return null;
        }
    }

    public static JsonObject addComments(Map<String, String> comments, JsonObject object, String parentKey) {
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String objectKey = entry.getKey();
            String commentsKey = parentKey + objectKey;
            String comment = object.getComment(entry.getKey());
            if (comments.containsKey(commentsKey) && comment == null) {
                String commentToAdd = comments.get(commentsKey);
                object.setComment(objectKey, commentToAdd);
                comment = commentToAdd;
            }

            JsonElement value = entry.getValue();
            if (value instanceof JsonArray array) {
                JsonArray sortedJsonElements = new JsonArray();
                for (JsonElement element : array) {
                    if (element instanceof JsonObject nestedObject) {
                        sortedJsonElements.add(addComments(comments, nestedObject, entry.getKey() + "."));
                    } else if (element instanceof JsonArray array1) {
                        JsonArray arrayOfArrays = new JsonArray();
                        arrayOfArrays.addAll(array1);
                        sortedJsonElements.add(arrayOfArrays);
                    }
                }
                if (!sortedJsonElements.isEmpty()) {
                    object.put(objectKey, sortedJsonElements, comment);
                }
            }

            if (value instanceof JsonObject nestedObject) {
                object.put(objectKey, addComments(comments, nestedObject, entry.getKey() + "."), comment);
            }
        }
        return object;
    }
}
