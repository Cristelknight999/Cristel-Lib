package de.cristelknight.cristellib.data.condition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import de.cristelknight.cristellib.config.ConfigManager;
import net.minecraft.util.GsonHelper;

import java.util.List;
import java.util.Optional;

public record ConditionNode(Either<JsonElement, List<JsonElement>> either) {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static boolean testConditionNode(Optional<ConditionNode> condition) {
        return condition.isEmpty() || condition.get().test();
    }

    private static final Codec<JsonElement> PASSTHROUGH_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            jsonObject -> new Dynamic<>(JsonOps.INSTANCE, jsonObject)
    );

    private static final Codec<Either<JsonElement, List<JsonElement>>> EITHER_CODEC = Codec.either(PASSTHROUGH_CODEC, Codec.list(PASSTHROUGH_CODEC));

    public static final Codec<ConditionNode> CODEC = EITHER_CODEC.xmap(
            ConditionNode::new,
            conditionNode -> conditionNode.either
    );

    public boolean test() {
        if(either.left().isPresent())
            return evaluateConditions(List.of(either.left().get()));
        if(either.right().isPresent())
            return evaluateConditions(either.right().get());
        return false;
    }

    private boolean evaluateConditions(List<JsonElement> jsonElements){
        for(JsonElement e : jsonElements){
            if(!(e instanceof JsonObject o)) continue;
            if(!testCondition(o)) return false;
        }
        return true;
    }

    private boolean testCondition(JsonObject object) {
        String type = GsonHelper.getAsString(object, "type");
        Codec<? extends ICondition> conditionCodec = ConditionRegistry.getCodec(type);
        object.remove("type");
        ICondition condition = ConfigManager.readElement("Couldn't read conditionNode of type: " + type, conditionCodec, JsonOps.INSTANCE, object);
        return condition.test();
    }
}
