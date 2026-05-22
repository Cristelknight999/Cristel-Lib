package de.cristelknight.cristellib.config.simple;


import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public interface ConfigSettings<T> {
    String getSubPath();
    Codec<T> getCodec();
    T getDefault();
    default @Nullable HashMap<String, String> getComments() {return null;}
    default String getHeader() {return "";}
    default boolean isSorted() {return false;}
}
