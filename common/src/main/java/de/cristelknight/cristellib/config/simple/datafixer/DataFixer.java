package de.cristelknight.cristellib.config.simple.datafixer;

import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import com.mojang.serialization.Codec;
import de.cristelknight.cristellib.builtinpacks.BuiltInPackConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class DataFixer {

    private static final Map<Codec<?>, Set<Function<JsonObject, Boolean>>> DATA_FIXERS = new HashMap<>();

    public static <T> boolean appliedFixer(Codec<T> codec, JsonObject object) {
        Set<Function<JsonObject, Boolean>> fixers = DATA_FIXERS.getOrDefault(codec, Set.of());
        boolean changed = false;
        for(Function<JsonObject, Boolean> fixer : fixers) {
            if (fixer != null && fixer.apply(object)) changed = true;
        }
        return changed;
    }

    public static void register(Codec<?> codec, Function<JsonObject, Boolean> fixer) {
        DATA_FIXERS.computeIfAbsent(codec, (l) -> new HashSet<>()).add(fixer);
    }


    static {
        register(BuiltInPackConfig.CODEC, jsonObject -> {
            if(jsonObject.containsKey("hideAllPacksInScreen")) return false;
            jsonObject.put("hideAllPacksInScreen", new JsonPrimitive(false));
            return true;
        });
    }

}
