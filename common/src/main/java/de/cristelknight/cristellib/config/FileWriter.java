package de.cristelknight.cristellib.config;

import blue.endless.jankson.*;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.util.jankson.CommentArray;
import de.cristelknight.cristellib.util.jankson.JanksonOps;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;

import static de.cristelknight.cristellib.Constants.getWithPrefix;

public class FileWriter {

    public static final Jankson JANKSON = Jankson.builder().build();

    public static final Supplier<JsonGrammar.Builder> JSON_GRAMMAR_BUILDER = () -> new JsonGrammar.Builder().withComments(true).bareSpecialNumerics(true).printCommas(true);

    public static final JsonGrammar JSON_GRAMMAR = JSON_GRAMMAR_BUILDER.get().build();

    // Write
    public static <T> void writeToFile(Path path, Codec<T> codec, Map<String, String> comments, T from, String rawHeader, boolean isSorted) {
        JsonElement jsonElement = writeToElement(
                String.format("Jankson file creation for \"%s\" failed due to the following error(s):", path.toString()),
                codec, JanksonOps.INSTANCE, from
        );

        if (jsonElement instanceof JsonObject jsonObject) {
            jsonElement = addCommentsAndAlphabeticallySortRecursively(comments, jsonObject, "", isSorted);
        }
        try {
            Files.createDirectories(path.getParent());
            String output = rawHeader + jsonElement.toJson(JSON_GRAMMAR);
            Files.write(path, output.getBytes());
        } catch (IOException e) {
            Constants.LOG.error("Failed to write file to \"%s\" due to the following error(s):", e);
        }
    }

    public static <T, K> K writeToElement(String errorMsg, Codec<T> codec, DynamicOps<K> ops, T from) {
        DataResult<K> dataResult = codec.encodeStart(ops, from);
        Optional<DataResult.Error<K>> error = dataResult.error();
        if (error.isPresent()) {
            throw new IllegalArgumentException(getWithPrefix(errorMsg + "\n" + error.get().message()));
        }

        return dataResult.result().orElseThrow();
    }

    // Read
    public static <T> T readFromJanksonPath(Path path, Codec<T> codec) {
        JsonElement load;
        try {
            load = JANKSON.load(path.toFile());
        } catch (Exception errorMsg) {
            throw new IllegalArgumentException(getWithPrefix(String.format("Couldn't load %s, crashing instead. Maybe try to delete the config files!", path)));
        }
        return loadFromElement(String.format("Couldn't read %s, crashing instead. Maybe try to delete the config files!", path), codec, JanksonOps.INSTANCE, load);
    }

    public static <T> T readFromModContainer(String modId, String subPath, Codec<T> codec, String errorMsg) {
        InputStream stream = CristelLibExpectPlatform.getResourceStream(modId, subPath);
        if (stream == null) {
            throw new IllegalArgumentException(getWithPrefix("Couldn't create InputStream for subPath: " + subPath + " in ModContainer with id: " + modId));
        }
        com.google.gson.JsonElement load = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return loadFromElement(errorMsg, codec, JsonOps.INSTANCE, load);
    }

    public static <T, K> T loadFromElement(String errorMsg, Codec<T> codec, DynamicOps<K> ops, K load) {
        DataResult<Pair<T, K>> decode = codec.decode(ops, load);
        Optional<DataResult.Error<Pair<T, K>>> error = decode.error();

        if (error.isPresent()) {
            throw new IllegalArgumentException(getWithPrefix(errorMsg) + "\n" + error.get().message());
        }
        return decode.result().orElseThrow().getFirst();
    }

    // Util
    public static JsonObject addCommentsAndAlphabeticallySortRecursively(Map<String, String> comments, JsonObject object, String parentKey, boolean alphabeticallySorted) {
        if (comments.isEmpty() && !alphabeticallySorted) return object;
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
                        sortedJsonElements.add(addCommentsAndAlphabeticallySortRecursively(comments, nestedObject, entry.getKey() + ".", alphabeticallySorted));
                    } else if (element instanceof JsonArray array1) {
                        CommentArray commentArray = new CommentArray();
                        commentArray.addAll(array1);
                        sortedJsonElements.add(commentArray);
                    }
                }
                if (!sortedJsonElements.isEmpty()) {
                    object.put(objectKey, sortedJsonElements, comment);
                }
            }

            if (value instanceof JsonObject nestedObject) {
                object.put(objectKey, addCommentsAndAlphabeticallySortRecursively(comments, nestedObject, entry.getKey() + ".", alphabeticallySorted), comment);
            }
        }

        if (alphabeticallySorted) {
            JsonObject alphabeticallySortedJsonObject = new JsonObject();
            TreeMap<String, JsonElement> map = new TreeMap<>(String::compareTo);
            map.putAll(object);
            alphabeticallySortedJsonObject.putAll(map);
            alphabeticallySortedJsonObject.forEach((key, entry) ->
                    alphabeticallySortedJsonObject.setComment(key, object.getComment(key))
            );

            return alphabeticallySortedJsonObject;
        }
        return object;
    }

}
