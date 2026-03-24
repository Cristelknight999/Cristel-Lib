package de.cristelknight.cristellib.config;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ConfigType implements StringRepresentable {

    //BOTH,
    ENABLE_DISABLE("ENABLE_DISABLE"),
    PLACEMENT("PLACEMENT");

    private final String name;
    public static final Codec<ConfigType> CODEC = StringRepresentable.fromEnum(ConfigType::values);

    ConfigType(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}