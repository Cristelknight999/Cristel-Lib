package de.cristelknight.cristellib.data;

import com.mojang.serialization.MapCodec;

public interface ICondition {

    boolean test();

    MapCodec<? extends ICondition> codec();
}
