package de.cristelknight.cristellib.util;


import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.CristelLib;

import java.util.List;

public class JanksonUtil {
    public static void addToObject(JsonObject jsonObject, String path, List<Pair<String, String>> toAdd) {
        String[] pathSegments = path.split("/");
        JsonElement currentElement = jsonObject;

        for (String segment : pathSegments) {
            if (currentElement instanceof JsonObject object) {
                currentElement = object.get(segment);

                if (currentElement == null) {
                    CristelLib.LOGGER.error("Path segment not found: " + segment);
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
