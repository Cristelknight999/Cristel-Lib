package de.cristelknight.cristellib.config;

import blue.endless.jankson.*;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.config.serialize.ed.EDConfig;
import de.cristelknight.cristellib.config.serialize.ed.EDConfigTransformer;
import de.cristelknight.cristellib.config.serialize.ed.NestedEDConfig;
import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.config.simple.datafixer.DataFixer;
import de.cristelknight.cristellib.util.JanksonHelper;
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

import static de.cristelknight.cristellib.CristelLib.getWithPrefix;

public class ConfigManager {

    public static final Path CONFIG_DIR = CristelLibExpectPlatform.getConfigDirectory();

    public static final Path CONFIG_LIB = CONFIG_DIR.resolve("cristellib");

    public static final Jankson JANKSON = Jankson.builder().build();

    public static final Supplier<JsonGrammar.Builder> JSON_GRAMMAR_BUILDER = () -> new JsonGrammar.Builder().withComments(true).bareSpecialNumerics(true).printCommas(true);

    public static final JsonGrammar JSON_GRAMMAR = JSON_GRAMMAR_BUILDER.get().build();

    public static void createEDConfig(StructureConfig config, boolean override) {
        Map<String, NestedEDConfig> nestedStructureMap;
        if(config.enableDisableConfig == null) {
            Map<Identifier, List<Identifier>> sets = config.getDefaultStructures();
            nestedStructureMap = EDConfigTransformer.mapToNestedStructures(sets, config);
        } else nestedStructureMap = EDConfigTransformer.mapToNestedStructuresWithValues(config.enableDisableConfig, config);

        writeConfig(config, NestedEDConfig.ED_CODEC, nestedStructureMap, override);
    }

    public static Map<Identifier, EDConfig> readEDConfig(StructureConfig config) {
        Map<String, NestedEDConfig> configMap = readFromJanksonPath(config.getPath(), NestedEDConfig.ED_CODEC);

        Map<Identifier, EDConfig> map = new HashMap<>();
        for (String structureSet : configMap.keySet()) {
            map.put(config.toDefaultRL(structureSet), new EDConfig(EDConfigTransformer.stringBooleanMap(configMap.get(structureSet), "")));
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
        return sets.entrySet().stream().collect(Collectors.toMap(entry -> config.toDefaultRL(entry.getKey()), Map.Entry::getValue));
    }

    // File and Codec Util
    public static String createHeader(String header) {
        if(header == null || header.isEmpty()) return "";
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
            jsonElement = JanksonHelper.addCommentsAndAlphabeticallySortRecursively(comments, jsonObject, "", isSorted);
        }
        try {
            Files.createDirectories(path.getParent());
            String output = rawHeader + jsonElement.toJson(JSON_GRAMMAR);
            Files.write(path, output.getBytes());
        } catch (IOException e) {
            CristelLib.LOGGER.error(e.toString());
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
        if(gotFixed) writeAfterFix.accept(config);
        return config;
    }

    public static <T> T readFromSubPath(String modId, String subPath, Codec<T> codec, String errorMsg) {
        InputStream stream = CristelLibExpectPlatform.getResourceStream(modId, subPath);
        if(stream == null) {
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
}
