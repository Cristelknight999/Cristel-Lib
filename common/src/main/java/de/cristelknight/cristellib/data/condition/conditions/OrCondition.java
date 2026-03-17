package de.cristelknight.cristellib.data.condition.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.data.condition.ConditionNode;
import de.cristelknight.cristellib.data.condition.ICondition;

import java.util.List;

public record OrCondition(List<ConditionNode> conditionNodes) implements ICondition {

    public static final Codec<OrCondition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(ConditionNode.CODEC).fieldOf("any").forGetter(OrCondition::conditionNodes)
            ).apply(instance, OrCondition::new));

    @Override
    public boolean test() {
        for (ConditionNode conditionNode: conditionNodes) {
            if(conditionNode.test())
                return true;
        }
        return false;
    }
}
