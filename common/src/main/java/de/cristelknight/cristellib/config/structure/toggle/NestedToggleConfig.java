package de.cristelknight.cristellib.config.structure.toggle;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.Map;

// TODO: Codec recursive maybe?
public record NestedToggleConfig(Map<String, Entry> entries) {

    public record Entry(Boolean value, NestedToggleConfig nested) {

        public boolean isBoolean() {
            return value != null;
        }

        public static final Codec<Entry> CODEC = Codec.either(
                Codec.BOOL,
                Codec.lazyInitialized(() -> NestedToggleConfig.CODEC)
        ).xmap(
                either -> either.map(Entry::ofBoolean, Entry::ofNested), //read
                entry -> entry.isBoolean() ? Either.left(entry.value) : Either.right(entry.nested) //write
        );

        public static Entry ofBoolean(Boolean value) {
            return new Entry(value, null);
        }

        public static Entry ofNested(NestedToggleConfig nested) {
            return new Entry(null, nested);
        }
    }

    public static final Codec<NestedToggleConfig> CODEC = Codec.unboundedMap(
            Codec.STRING,
            Entry.CODEC
    ).xmap(NestedToggleConfig::new, NestedToggleConfig::entries);

    public static final Codec<Map<String, NestedToggleConfig>> TOGGLE_CODEC = Codec.unboundedMap(
            Codec.STRING, // String keys
            NestedToggleConfig.CODEC // NestedStructure values
    );
}
