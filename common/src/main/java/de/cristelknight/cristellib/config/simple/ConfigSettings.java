package de.cristelknight.cristellib.config.simple;

import blue.endless.jankson.annotation.Nullable;
import com.mojang.serialization.Codec;

import java.util.HashMap;

public interface ConfigSettings<T> {
    String getSubPath();
    Codec<T> getCodec();
    T getDefault();
    default @Nullable HashMap<String, String> getComments() { return null; }
    default String getHeader() { return ""; }
    default boolean isSorted() { return false; }
}
