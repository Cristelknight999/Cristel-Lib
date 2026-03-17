package de.cristelknight.cristellib.data.condition.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.data.condition.ConditionNode;
import de.cristelknight.cristellib.data.condition.ICondition;

public record NotCondition(ConditionNode conditionNode) implements ICondition {

    public static final Codec<NotCondition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ConditionNode.CODEC.fieldOf("all").forGetter(NotCondition::conditionNode)
            ).apply(instance, NotCondition::new));

    @Override
    public boolean test() {
        return !conditionNode.test();
    }
}
