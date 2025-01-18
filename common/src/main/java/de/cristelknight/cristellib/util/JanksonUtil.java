package de.cristelknight.cristellib.util;


import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class JanksonUtil {
    public static @Nullable com.google.gson.JsonElement getSetElement(String getDataFromModId, ResourceLocation location) {
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













    public static void addToObject(JsonObject jsonObject, String path, List<Pair<String, String>> toAdd) {
        String[] pathSegments = path.split("/");
        JsonElement currentElement = jsonObject;

        for (String segment : pathSegments) {
            if (currentElement instanceof JsonObject object) {
                currentElement = object.get(segment);

                if (currentElement == null) {
                    CristelLib.LOGGER.error("Path segment not found: {}", segment);
                    return;
                }
            } else {
                CristelLib.LOGGER.error("Invalid path or element type.");
                return;
            }
        }

        if(currentElement instanceof JsonObject object){
            for(Pair<String, String> s : toAdd){
                if(object.containsKey(s.getFirst())) continue;
                object.put(s.getFirst(), JsonPrimitive.of(s.getSecond()));
            }
        }
    }
}
