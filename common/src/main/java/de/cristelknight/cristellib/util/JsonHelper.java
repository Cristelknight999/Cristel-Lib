package de.cristelknight.cristellib.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonHelper {

    // Loading
    public static @Nullable JsonElement getSetElement(String getDataFromModId, Identifier location) {
        return getElement(getDataFromModId, "data/" + location.getNamespace() + "/worldgen/structure_set/" + location.getPath() + ".json");
    }

    public static @Nullable JsonElement getElement(String getDataFromModId, String location) {
        InputStream in = CristelLibExpectPlatform.getResourceStream(getDataFromModId, location);
        if (in == null) {
            Constants.LOGGER.warn("Couldn't create Input Stream for sub path {} in modId {}", location, getDataFromModId);
            return null;
        }

        try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader);
        } catch (IOException e) {
            Constants.LOGGER.warn("Couldn't read {} from mod: {}", location, getDataFromModId, e);
            return null;
        }
    }

    // finding by key
    public static List<JsonElement> findAll(String searchedKey, JsonObject object, String parentKey) {
        if (searchedKey.isEmpty() && parentKey.isEmpty())
            return List.of(object);

        List<JsonElement> results = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String fullKey = parentKey + entry.getKey();
            JsonElement value = entry.getValue();

            boolean shouldBreak = collectMatches(results, searchedKey, fullKey, value);
            if (shouldBreak) break;
        }

        return results;
    }

    public static void findAllInArray(List<JsonElement> results, String searchedKey, JsonArray array, String parentKey) {
        if (searchedKey.split("\\.").length <= parentKey.split("\\.").length)
            return;

        for (int i = 0; i < array.size(); i++) {
            JsonElement value = array.get(i);
            String fullKey = parentKey + "." + getIndex(i);

            boolean shouldBreak = collectMatches(results, searchedKey, fullKey, value);
            if (shouldBreak) break;
        }
    }

    private static boolean collectMatches(List<JsonElement> results, String searchedKey, String fullKey, JsonElement value) {
        Match match = matches(searchedKey, fullKey);
        switch (match) {
            case MATCH:
                results.add(value);
                return false;
            case MATCH_AND_BREAK:
                results.add(value);
                return true;
            case BREAK:
                return false;
        }

        if (value instanceof JsonArray array) {
            findAllInArray(results, searchedKey, array, fullKey);
        } else if (value instanceof JsonObject nested) {
            results.addAll(findAll(searchedKey, nested, fullKey + "."));
        }
        return false;
    }

    private static Match matches(String searchedKey, String fullKey) {
        String[] searched = searchedKey.split("\\.");
        String[] current = fullKey.split("\\.");
        if (searched.length < current.length)
            return Match.BREAK; // to long key, break search

        for (int i = 0; i < current.length; i++) {
            String s = searched[i];
            if (s.equals(ANY))
                continue;

            String c = current[i];
            if (!s.equals(c))
                return Match.BREAK;
        }
        if (searched.length > current.length)
            return Match.NO_MATCH; //right path, but not correct length

        if (searched[searched.length - 1].equals(ANY))
            return Match.MATCH;
        else
            return Match.MATCH_AND_BREAK;
    }


    private static String getIndex(int i) {
        return "[" + i + "]";
    }

    private static final String ANY = "[*]";

    private enum Match {
        MATCH,
        MATCH_AND_BREAK,
        NO_MATCH,
        BREAK
    }
}