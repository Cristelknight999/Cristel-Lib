package de.cristelknight.cristellib.data.condition.conditions;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.data.condition.Conditions;
import de.cristelknight.cristellib.data.condition.ICondition;

import java.util.List;

public record NotCondition(List<JsonElement> conditions) implements ICondition {

    public static final Codec<NotCondition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Conditions.CODEC.fieldOf("all").forGetter(NotCondition::conditions)
            ).apply(instance, NotCondition::new));

    @Override
    public boolean test() {
        return !Conditions.readConditions(conditions);
    }
}
