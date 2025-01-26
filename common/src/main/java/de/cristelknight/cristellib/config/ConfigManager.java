package de.cristelknight.cristellib.config;

import blue.endless.jankson.*;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.config.serialize.ed.EDConfigTransformer;
import de.cristelknight.cristellib.config.serialize.ed.NestedEDConfig;
import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import de.cristelknight.cristellib.util.JanksonUtil;
import de.cristelknight.cristellib.util.jankson.JanksonOps;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import static de.cristelknight.cristellib.CristelLib.getWithPrefix;

public class ConfigManager {

    public static final Path CONFIG_DIR = CristelLibExpectPlatform.getConfigDirectory();

    public static final Path CONFIG_LIB = CONFIG_DIR.resolve("cristellib");

    public static final Jankson JANKSON = Jankson.builder().build();

    public static final Supplier<JsonGrammar.Builder> JSON_GRAMMAR_BUILDER = () -> new JsonGrammar.Builder().withComments(true).bareSpecialNumerics(true).printCommas(true);

    public static final JsonGrammar JSON_GRAMMAR = JSON_GRAMMAR_BUILDER.get().build();

    public static void createEDConfig(StructureConfig config, boolean override) {
        Map<ResourceLocation, List<String>> sets = config.getStructures();
        Map<String, NestedEDConfig> nestedStructureMap = EDConfigTransformer.mapToNestedStructures(sets);

        writeConfig(config, NestedEDConfig.ED_CODEC, nestedStructureMap, override);
    }

    public static Map<String, Boolean> readEDConfig(StructureConfig config) {
        Map<String, NestedEDConfig> configMap = readConfig(config.getPath(), NestedEDConfig.ED_CODEC);

        Map<String, Boolean> map = new HashMap<>();
        for (NestedEDConfig edConfig : configMap.values()) {
            map.putAll(EDConfigTransformer.stringBooleanMap(edConfig, ""));
        }
        return map;
    }

    public static void createPlacementConfig(StructureConfig config, boolean override) {
        Map<String, PlacementConfig> sets = config.getStructurePlacement();
        writeConfig(config, PlacementConfig.PLACEMENT_CODEC, sets, override);
    }

    public static Map<String, PlacementConfig> readPlacementConfig(StructureConfig config) {
        return readConfig(config.getPath(), PlacementConfig.PLACEMENT_CODEC);
    }



    // File and Codec Util

    // Write
    public static <T> void writeConfig(StructureConfig config, Codec<T> codec, T from, boolean override) {
        Path path = config.getPath();
        if (!override && path.toFile().exists()) return;

        writeFile(config.getPath(), codec, config.getComments(), from, config.getHeader(), true);
    }

    public static <T> void writeFile(Path path, Codec<T> codec, Map<String, String> comments, T from, String header, boolean isSorted) {
        JsonElement jsonElement = createElement(path, codec, JanksonOps.INSTANCE, from);

        if (jsonElement instanceof JsonObject jsonObject) {
            jsonElement = JanksonUtil.addCommentsAndAlphabeticallySortRecursively(comments, jsonObject, "", isSorted);
        }
        try {
            Files.createDirectories(path.getParent());
            String output = header + jsonElement.toJson(JSON_GRAMMAR);
            Files.write(path, output.getBytes());
        } catch (IOException e) {
            CristelLib.LOGGER.error(e.toString());
        }
    }

    public static String createHeader(String header) {
        if(header == null || header.isEmpty()) return "";
        if (!header.endsWith("\n")) {
            header += "\n";
        }
        return "/*\n" + header + "*/\n";
    }

    public static <T, K> K createElement(Path path, Codec<T> codec, DynamicOps<K> ops, T from) {
        DataResult<K> dataResult = codec.encodeStart(ops, from);
        Optional<DataResult.Error<K>> error = dataResult.error();
        if (error.isPresent()) {
            throw new IllegalArgumentException(getWithPrefix(String.format("Jankson file creation for \"%s\" failed due to the following error(s):\n%s", path.toString(), error.get().message())));
        }

        return dataResult.result().orElseThrow();
    }

    // Read
    public static <T> T readConfig(Path path, Codec<T> codec) {
        JsonElement load;
        try {
            load = JANKSON.load(path.toFile());
        } catch (Exception errorMsg) {
            throw new IllegalArgumentException(getWithPrefix(String.format("Couldn't load %s, crashing instead. Maybe try to delete the config files!", path)));
        }
        return readElement(String.format("Couldn't read %s, crashing instead. Maybe try to delete the config files!", path), codec, JanksonOps.INSTANCE, load);
    }

    public static <T> T readFromJsonPath(String errorMsg, Path path, Codec<T> codec) {
        InputStream stream;
        try {
            stream = Files.newInputStream(path);
        } catch (IOException e) {
            throw new IllegalArgumentException(getWithPrefix(String.format("Couldn't load %s, crashing instead. Maybe try to delete the config files!", path)));
        }
        com.google.gson.JsonElement load = JsonParser.parseReader(new InputStreamReader(stream));
        return readElement(errorMsg, codec, JsonOps.INSTANCE, load);
    }

    public static <T, K> T readElement(String errorMsg, Codec<T> codec, DynamicOps<K> ops, K load) {
        DataResult<Pair<T, K>> decode = codec.decode(ops, load);
        Optional<DataResult.Error<Pair<T, K>>> error = decode.error();

        if (error.isPresent()) {
            throw new IllegalArgumentException(getWithPrefix(errorMsg));
        }
        return decode.result().orElseThrow().getFirst();
    }
}
