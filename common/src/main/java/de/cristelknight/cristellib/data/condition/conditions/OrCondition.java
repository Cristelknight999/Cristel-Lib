package de.cristelknight.cristellib.data.condition.conditions;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.data.condition.Conditions;
import de.cristelknight.cristellib.data.condition.ICondition;

import java.util.List;

public record OrCondition(List<List<JsonElement>> orConditions) implements ICondition {

    public static final Codec<OrCondition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(Conditions.CODEC).fieldOf("any").forGetter(OrCondition::orConditions)
            ).apply(instance, OrCondition::new));

    @Override
    public boolean test() {
        for (List<JsonElement> condition: orConditions) {
            if(Conditions.readConditions(condition))
                return true;
        }
        return false;
    }
}
