package de.cristelknight.cristellib.config.client.simple.custom;

import de.cristelknight.cristellib.config.simple.custom.AlphaColorField;
import de.cristelknight.cristellib.config.simple.custom.ColorField;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public class SimpleScreenTypes {

    private static final List<EntryPredicate> FIELD_ENTRIES = new CopyOnWriteArrayList<>();

    /* ----------------------------
       Registration helpers
       ---------------------------- */

    // Register one factory for many classes (no converter)
    public static void addEntry(ConfigFieldFactory<?> factory, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            FIELD_ENTRIES.add(new EntryPredicate(
                    c -> c == clazz,                   // exact class matcher
                    value -> true,                     // accept any value (value check optional)
                    new ScreenFieldEntry(factory, Optional.empty())
            ));
        }
    }

    // Register with converter for many classes
    public static void addEntry(ConfigFieldFactory<?> factory, Function<Object, ?> toOriginal, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            FIELD_ENTRIES.add(new EntryPredicate(
                    c -> c == clazz,
                    value -> true,
                    new ScreenFieldEntry(factory, Optional.of(toOriginal))
            ));
        }
    }

    // Register an advanced entry using class predicate and value predicate (no converter)
    public static void addAdvancedEntry(Predicate<Class<?>> classMatcher,
                                        Predicate<Object> valueMatcher,
                                        ConfigFieldFactory<?> factory) {
        FIELD_ENTRIES.add(new EntryPredicate(
                Objects.requireNonNull(classMatcher),
                valueMatcher == null ? (v -> true) : valueMatcher,
                new ScreenFieldEntry(factory, Optional.empty())
        ));
    }

    // Register advanced entry with converter
    public static <V> void addAdvancedEntry(Predicate<Class<?>> classMatcher,
                                            Predicate<Object> valueMatcher,
                                            ConfigFieldFactory<?> factory,
                                            Function<Object, V> toOriginal) {
        FIELD_ENTRIES.add(new EntryPredicate(
                Objects.requireNonNull(classMatcher),
                valueMatcher == null ? (v -> true) : valueMatcher,
                new ScreenFieldEntry(factory, Optional.of(toOriginal))
        ));
    }

    /* ----------------------------
       Lookup helpers
       ---------------------------- */

    public static Optional<ScreenFieldEntry> getEntry(Class<?> clazz, Object value) {
        return FIELD_ENTRIES.stream()
                .filter(e -> e.classMatcher().test(clazz) && e.valueMatcher().test(value))
                .map(EntryPredicate::entry)
                .findFirst();
    }

    /* ----------------------------
       Static registrations (examples)
       ---------------------------- */
    static {
        // String
        addEntry((entryBuilder, name, value, defaultValue) ->
                        entryBuilder.startStrField(Component.literal(name), (String) value)
                                .setDefaultValue((String) defaultValue),
                String.class
        );

        // int and Integer together (correct order: factory first, then classes)
        addEntry((entryBuilder, name, value, defaultValue) ->
                        entryBuilder.startIntField(Component.literal(name), ((Number) value).intValue())
                                .setDefaultValue(((Number) defaultValue).intValue()),
                Integer.class, int.class
        );

        // double and Double
        addEntry((entryBuilder, name, value, defaultValue) ->
                        entryBuilder.startDoubleField(Component.literal(name), ((Number) value).doubleValue())
                                .setDefaultValue(((Number) defaultValue).doubleValue()),
                Double.class, double.class
        );

        // boolean and Boolean
        addEntry((entryBuilder, name, value, defaultValue) ->
                        entryBuilder.startBooleanToggle(Component.literal(name), (boolean) value)
                                .setDefaultValue((boolean) defaultValue),
                Boolean.class, boolean.class
        );

        addEntry(
                (entryBuilder, name, value, defaultValue) ->
                        entryBuilder.startColorField(Component.literal(name), ((ColorField) value).toInt())
                                .setDefaultValue(((ColorField) defaultValue).toInt()),
                intColor -> ColorField.fromInt((int) intColor),
                ColorField.class
        );
        addEntry(
                (entryBuilder, name, value, defaultValue) ->
                        entryBuilder.startAlphaColorField(Component.literal(name), ((AlphaColorField) value).toInt())
                                .setDefaultValue(((AlphaColorField) defaultValue).toInt()),
                intColor -> AlphaColorField.fromInt((int) intColor),
                AlphaColorField.class
        );

        // Advanced
        addAdvancedEntry(
                List.class::isAssignableFrom,
                value -> value instanceof List<?> list && list.stream().allMatch(it -> it instanceof String),
                (entryBuilder, name, value, defaultValue) ->
                        entryBuilder.startStrList(Component.literal(name), (List<String>) value)
                                .setDefaultValue((List<String>) defaultValue)
        );
    }

    /* ----------------------------
       Helper records
       ---------------------------- */

    public record ScreenFieldEntry(ConfigFieldFactory<?> fieldFactory, Optional<Function<Object, ?>> toOriginal) {
        public boolean hasConverter() {
            return toOriginal.isPresent();
        }
    }

    private record EntryPredicate(Predicate<Class<?>> classMatcher,
                                  Predicate<Object> valueMatcher,
                                  ScreenFieldEntry entry) {}
}
