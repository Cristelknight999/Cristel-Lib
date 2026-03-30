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
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.config.structure.ed.EDConfig;
import de.cristelknight.cristellib.config.structure.ed.EDConfigTransformer;
import de.cristelknight.cristellib.config.structure.ed.NestedEDConfig;
import de.cristelknight.cristellib.config.structure.placement.PlacementConfig;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.config.simple.datafixer.DataFixer;
import de.cristelknight.cristellib.util.jankson.CommentArray;
import de.cristelknight.cristellib.util.jankson.JanksonOps;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.cristelknight.cristellib.Constants.getWithPrefix;

public class ConfigManager {

    public static final Path CONFIG_DIR = CristelLibExpectPlatform.getConfigDirectory();

    public static final Path CONFIG_LIB = CONFIG_DIR.resolve("cristellib");

    public static final Jankson JANKSON = Jankson.builder().build();

    public static final Supplier<JsonGrammar.Builder> JSON_GRAMMAR_BUILDER = () -> new JsonGrammar.Builder().withComments(true).bareSpecialNumerics(true).printCommas(true);

    public static final JsonGrammar JSON_GRAMMAR = JSON_GRAMMAR_BUILDER.get().build();

    public static void createEDConfig(StructureConfig config, boolean override) {
        Map<String, NestedEDConfig> nestedStructureMap;
        if (config.enableDisableConfig == null) {
            Map<Identifier, List<Identifier>> sets = config.getDefaultStructures();
            nestedStructureMap = EDConfigTransformer.mapToNestedStructures(sets, config);
        } else
            nestedStructureMap = EDConfigTransformer.mapToNestedStructuresWithValues(config.enableDisableConfig, config);

        writeConfig(config, NestedEDConfig.ED_CODEC, nestedStructureMap, override);
    }

    public static Map<Identifier, EDConfig> readEDConfig(StructureConfig config) {
        Map<String, NestedEDConfig> configMap = readFromJanksonPath(config.getPath(), NestedEDConfig.ED_CODEC);

        Map<Identifier, EDConfig> map = new HashMap<>();
        for (String structureSet : configMap.keySet()) {
            map.put(config.toDefaultId(structureSet), new EDConfig(EDConfigTransformer.stringBooleanMap(configMap.get(structureSet), "")));
        }
        return map;
    }

    public static void createPlacementConfig(StructureConfig config, boolean override) {
        Map<Identifier, PlacementConfig> sets = config.placementConfig == null ? config.getDefaultStructurePlacement() : config.placementConfig;
        Map<String, PlacementConfig> sets2 = sets.entrySet().stream().collect(Collectors.toMap(entry -> config.toDefaultString(entry.getKey()), Map.Entry::getValue));
        writeConfig(config, PlacementConfig.PLACEMENT_CODEC, sets2, override);
    }

    public static Map<Identifier, PlacementConfig> readPlacementConfig(StructureConfig config) {
        Map<String, PlacementConfig> sets = readFromJanksonPath(config.getPath(), PlacementConfig.PLACEMENT_CODEC);
        return sets.entrySet().stream().collect(Collectors.toMap(entry -> config.toDefaultId(entry.getKey()), Map.Entry::getValue));
    }

    // File and Codec Util
    public static String createHeader(String header) {
        if (header == null || header.isEmpty()) return "";
        if (!header.endsWith("\n")) {
            header += "\n";
        }
        return "/*\n" + header + "*/\n";
    }

    // Write
    public static <T> void writeConfig(StructureConfig config, Codec<T> codec, T from, boolean override) {
        writeFile(config.getPath(), codec, config.getComments(), from, ConfigManager.createHeader(config.getHeader()), true);
    }

    public static <T> void writeFile(Path path, Codec<T> codec, Map<String, String> comments, T from, String rawHeader, boolean isSorted) {
        JsonElement jsonElement = createElement(String.format("Jankson file creation for \"%s\" failed due to the following error(s):", path.toString()), codec, JanksonOps.INSTANCE, from);

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

    public static <T, K> K createElement(String errorMsg, Codec<T> codec, DynamicOps<K> ops, T from) {
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
        return readElement(String.format("Couldn't read %s, crashing instead. Maybe try to delete the config files!", path), codec, JanksonOps.INSTANCE, load);
    }

    public static <T> T readFromJanksonPathWithFix(Path path, Codec<T> codec, Consumer<T> writeAfterFix) {
        JsonElement load;
        try {
            load = JANKSON.load(path.toFile());
        } catch (Exception errorMsg) {
            throw new IllegalArgumentException(getWithPrefix(String.format("Couldn't load %s, crashing instead. Maybe try to delete the config files!", path)));
        }
        boolean gotFixed = load instanceof JsonObject object && DataFixer.appliedFixer(ConfigRegistry.getClazzFromCodec(codec), object);
        T config = readElement(String.format("Couldn't read %s, crashing instead. Maybe try to delete the config files!", path), codec, JanksonOps.INSTANCE, load);
        if (gotFixed) writeAfterFix.accept(config);
        return config;
    }

    public static <T> T readFromSubPath(String modId, String subPath, Codec<T> codec, String errorMsg) {
        InputStream stream = CristelLibExpectPlatform.getResourceStream(modId, subPath);
        if (stream == null) {
            throw new IllegalArgumentException(getWithPrefix("Couldn't create ImputStream for subPath: " + subPath + " in ModContainer with id: " + modId));
        }
        com.google.gson.JsonElement load = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return readElement(errorMsg, codec, JsonOps.INSTANCE, load);
    }

    public static <T, K> T readElement(String errorMsg, Codec<T> codec, DynamicOps<K> ops, K load) {
        DataResult<Pair<T, K>> decode = codec.decode(ops, load);
        Optional<DataResult.Error<Pair<T, K>>> error = decode.error();

        if (error.isPresent()) {
            throw new IllegalArgumentException(getWithPrefix(errorMsg) + "\n" + error.get().message());
        }
        return decode.result().orElseThrow().getFirst();
    }

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
            alphabeticallySortedJsonObject.forEach((key, entry) -> {
                alphabeticallySortedJsonObject.setComment(key, object.getComment(key));
            });

            return alphabeticallySortedJsonObject;
        }
        return object;
    }
}
