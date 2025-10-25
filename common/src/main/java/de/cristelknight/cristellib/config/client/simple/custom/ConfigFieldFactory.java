package de.cristelknight.cristellib.config.client.simple.custom;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface ConfigFieldFactory<E extends AbstractFieldBuilder<?, ?, ?>> {
    E create(ConfigEntryBuilder entryBuilder, String name, Object value, Object defaultValue);
}