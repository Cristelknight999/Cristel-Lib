package de.cristelknight.cristellib.config.client;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.builtinpacks.BuiltInPackConfig;
import de.cristelknight.cristellib.config.simple.ConfigHolder;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.config.simple.ConfigSettings;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.lang.reflect.RecordComponent;
import java.util.*;

public class AutoClothConfigScreen {

    public static void addForCristelLib(ConfigEntryBuilder entryBuilder, ConfigBuilder builder, String modID) {
        if(modID.equals(CristelLib.MOD_ID)) addConfigToCategory(entryBuilder, builder, BuiltInPackConfig.class);
    }

    public static void saveForCristelLib() {
        BuiltInPackConfig configInstance = ConfigRegistry.get(BuiltInPackConfig.class);
        try {
            // Rebuild and save updated config instance
            BuiltInPackConfig updated = rebuildConfigInstance(BuiltInPackConfig.class, configInstance);
            ConfigRegistry.updateAndSave(updated);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BuiltInPackConfig.updateConfig();
    }


    public static <T> void addConfigToCategory(ConfigEntryBuilder entryBuilder, ConfigBuilder builder, Class<T> clazz) {
        T configInstance = ConfigRegistry.get(clazz);
        ConfigSettings<T> settings = ConfigRegistry.getSettings(clazz);
        HashMap<String, String> comments = ConfigHolder.getSafeComments(settings.getComments());

        ConfigCategory category = builder.getOrCreateCategory(Component.literal(clazz.getSimpleName()));


        for (RecordComponent component : clazz.getRecordComponents()) {
            String name = component.getName();
            Class<?> type = component.getType();

            Object value;
            try {
                value = component.getAccessor().invoke(configInstance);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            // Handle supported types (String, int, boolean, List<String>) just like before
            if (type == String.class) {
                category.addEntry(entryBuilder.startStrField(Component.literal(name), (String) value)
                        .setTooltipSupplier(() -> tooltip(name, comments))
                        .setSaveConsumer(val -> updateFieldValue(clazz, name, val))
                        .build());
            } else if (type == int.class || type == Integer.class) {
                category.addEntry(entryBuilder.startIntField(Component.literal(name), (Integer) value)
                        .setTooltipSupplier(() -> tooltip(name, comments))
                        .setSaveConsumer(val -> updateFieldValue(clazz, name, val))
                        .build());
            } else if (type == boolean.class || type == Boolean.class) {
                category.addEntry(entryBuilder.startBooleanToggle(Component.literal(name), (Boolean) value)
                        .setTooltipSupplier(() -> tooltip(name, comments))
                        .setSaveConsumer(val -> updateFieldValue(clazz, name, val))
                        .build());
            } else if (type == List.class && value instanceof List<?> list && list.stream().allMatch(it -> it instanceof String)) {
                category.addEntry(entryBuilder.startStrList(Component.literal(name), new ArrayList<>((List<String>) value))
                        .setTooltipSupplier(() -> tooltip(name, comments))
                        .setSaveConsumer(val -> updateFieldValue(clazz, name, val))
                        .build());
            } else {
                category.addEntry(entryBuilder.startTextDescription(Component.literal("Unsupported type: " + name)).build());
            }

        }
    }

    private static Optional<Component[]> tooltip(String name, Map<String, String> comments) {
        String comment = comments.get(name);
        if (comment != null && !comment.isEmpty()) {
            return Optional.of(new MutableComponent[] { Component.literal(comment) });
        }
        return Optional.empty();
    }


    // Helper: store changes in a map for each config + field (you'll want a better data structure here)
    private static final Map<Class<?>, Map<String, Object>> pendingUpdates = new HashMap<>();

    private static  <T> void updateFieldValue(Class<T> clazz, String fieldName, Object value) {
        pendingUpdates.computeIfAbsent(clazz, k -> new HashMap<>()).put(fieldName, value);
    }

    private static <T extends Record> T rebuildConfigInstance(Class<T> clazz, Object oldInstance) throws Exception {
        RecordComponent[] components = clazz.getRecordComponents();
        Object[] args = new Object[components.length];
        Map<String, Object> updates = pendingUpdates.getOrDefault(clazz, Map.of());

        for (int i = 0; i < components.length; i++) {
            String name = components[i].getName();
            Object oldValue = components[i].getAccessor().invoke(oldInstance);
            args[i] = updates.getOrDefault(name, oldValue);
        }
        // Clear updates after building
        pendingUpdates.remove(clazz);

        // Construct new record instance
        return clazz.getDeclaredConstructor(
                        Arrays.stream(components).map(RecordComponent::getType).toArray(Class[]::new))
                .newInstance(args);
    }
}
