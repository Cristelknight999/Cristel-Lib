package de.cristelknight.cristellib.config.client.simple.custom;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;

@FunctionalInterface
public interface ConfigFieldFactory<E extends AbstractFieldBuilder<?, ?, ?>> {
    E create(ConfigEntryBuilder entryBuilder, String name, Object value, Object defaultValue);
}