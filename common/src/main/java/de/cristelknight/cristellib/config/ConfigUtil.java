package de.cristelknight.cristellib.config;

import blue.endless.jankson.*;
import blue.endless.jankson.api.SyntaxError;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.config.serialize.ed.EDUtil;
import de.cristelknight.cristellib.config.serialize.ed.NestedEDConfig;
import de.cristelknight.cristellib.config.serialize.placement.PlacementConfig;
import de.cristelknight.cristellib.util.JanksonUtil;
import de.cristelknight.cristellib.util.jankson.JanksonOps;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class ConfigUtil {

    public static final Path CONFIG_DIR = CristelLibExpectPlatform.getConfigDirectory();

    public static final Path CONFIG_LIB = CONFIG_DIR.resolve("cristellib");

    public static final Jankson JANKSON = Jankson.builder().build();

    public static final Supplier<JsonGrammar.Builder> JSON_GRAMMAR_BUILDER = () -> new JsonGrammar.Builder().withComments(true).bareSpecialNumerics(true).printCommas(true);

    public static final JsonGrammar JSON_GRAMMAR = JSON_GRAMMAR_BUILDER.get().build();

    public static void createEDConfig(StructureConfig config, boolean override) {
        Map<ResourceLocation, List<String>> sets = config.getStructures();
        Map<String, NestedEDConfig> nestedStructureMap = EDUtil.mapToNestedStructures(sets);

        writeConfig(config, NestedEDConfig.ED_CODEC, nestedStructureMap, override);
    }

    public static Map<String, Boolean> readEDConfig(StructureConfig config) {
        Map<String, NestedEDConfig> configMap = readConfig(config, NestedEDConfig.ED_CODEC);

        Map<String, Boolean> map = new HashMap<>();
        for(NestedEDConfig edConfig : configMap.values()){
            map.putAll(EDUtil.stringBooleanMap(edConfig, ""));
        }
        return map;
    }


    public static void createPlacementConfig(StructureConfig config, boolean override) {
        Map<String, PlacementConfig> sets = config.getStructurePlacement();
        //CristelLib.LOGGER.error(sets.toString());
        writeConfig(config, PlacementConfig.PLACEMENT_CODEC, sets, override);
    }

    public static Map<String, PlacementConfig> readPlacementConfig(StructureConfig config) {
        return readConfig(config, PlacementConfig.PLACEMENT_CODEC);
    }


    public static <T>  void writeConfig(StructureConfig config, Codec<T> codec, T from, boolean override){
        Path path = config.getPath();
        if(!override && path.toFile().exists()) return;

        writeFile(config.getPath(), codec, config.getComments(), JanksonOps.INSTANCE, from, config.getHeader());
    }

    public static <T> void writeFile(Path path, Codec<T> codec, Map<String, String> comments, DynamicOps<JsonElement> ops, T from, String header) {
        JsonElement jsonElement = readElement(path, codec, ops, from);

        if (jsonElement instanceof JsonObject jsonObject) {
            jsonElement = JanksonUtil.addCommentsAndAlphabeticallySortRecursively(comments, jsonObject, "", true);
        }
        try {
            Files.createDirectories(path.getParent());
            String output = header + "\n" + jsonElement.toJson(JSON_GRAMMAR);
            Files.write(path, output.getBytes());
        } catch (IOException e) {
            CristelLib.LOGGER.error(e.toString());
        }
    }

    public static <T> JsonElement readElement(Path path, Codec<T> codec, DynamicOps<JsonElement> ops, T from){
        DataResult<JsonElement> dataResult = codec.encodeStart(ops, from);
        Optional<DataResult.Error<JsonElement>> error = dataResult.error();
        if (error.isPresent()) {
            throw new IllegalArgumentException(String.format("Jankson file creation for \"%s\" failed due to the following error(s):\n%s", path.toString(), error.get().message()));
        }

        return dataResult.result().orElseThrow();
    }


    public static <T> T readConfig(StructureConfig config, Codec<T> codec){
        JsonElement load;
        try {
            load = JANKSON.load(config.getPath().toFile());
        } catch (Exception errorMsg) {
            throw new IllegalArgumentException("["+CristelLib.MOD_ID+"] Couldn't read " + config.getPath() + ", crashing instead. Maybe try to delete the config files!");
        }
        return readElement(config.getPath().toString(), codec, JanksonOps.INSTANCE, load);
    }

    public static <T> T readElement(String path, Codec<T> codec, DynamicOps<JsonElement> ops, JsonElement load) {
        CristelLib.LOGGER.error(load.toJson());
        DataResult<Pair<T, JsonElement>> decode = codec.decode(ops, load);
        Optional<DataResult.Error<Pair<T, JsonElement>>> error = decode.error();

        if (error.isPresent()) {
            throw new IllegalArgumentException("["+CristelLib.MOD_ID+"] Couldn't read " + path + ", crashing instead. Maybe try to delete the config files! " + error.get().message());
        }
        return decode.result().orElseThrow().getFirst();
    }
}
