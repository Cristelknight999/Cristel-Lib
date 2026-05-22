package de.cristelknight.cristellib.data.condition.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.cristelknight.cristellib.data.condition.ConditionNode;
import de.cristelknight.cristellib.data.condition.ICondition;

public record NotCondition(ConditionNode conditionNode) implements ICondition<NotCondition> {

    @SuppressWarnings("unused")
    public NotCondition(ICondition<?> condition) {
        this(new ConditionNode(condition));
    }

    public static final Codec<NotCondition> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ConditionNode.CODEC.fieldOf("condition").forGetter(NotCondition::conditionNode)
            ).apply(instance, NotCondition::new));

    @Override
    public boolean test() {
        return !conditionNode.test();
    }

    @Override
    public Codec<NotCondition> getCodec() {
        return CODEC;
    }

}
