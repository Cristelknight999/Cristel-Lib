package de.cristelknight.cristellib.data.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.List;
import java.util.Optional;

public record ConditionNode(Either<List<ICondition<?>>, ICondition<?>> either) {

    public ConditionNode(ICondition<?> condition) {
        this(Either.right(condition));
    }

    @SuppressWarnings("unused")
    public ConditionNode(List<ICondition<?>> conditions) {
        this(Either.left(conditions));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static boolean testConditionNode(Optional<ConditionNode> condition) {
        return condition.isEmpty() || condition.get().test();
    }

    public static final Codec<Either<List<ICondition<?>>, ICondition<?>>> EITHER_CODEC = Codec.either(
            Codec.list(ICondition.FULL_CODEC), ICondition.FULL_CODEC
    );

    public static final Codec<ConditionNode> CODEC = EITHER_CODEC.xmap(
            ConditionNode::new,
            conditionNode -> conditionNode.either
    );

    public boolean test() {
        if (either.left().isPresent())
            return evaluateConditions(either.left().get());
        if (either.right().isPresent())
            return evaluateConditions(List.of(either.right().get()));
        return false;
    }

    private boolean evaluateConditions(List<ICondition<?>> jsonElements) {
        for (ICondition<?> e : jsonElements) {
            if (!e.test()) return false;
        }
        return true;
    }
}
