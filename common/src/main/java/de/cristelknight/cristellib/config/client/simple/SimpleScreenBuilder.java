package de.cristelknight.cristellib.config.client.simple;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.config.simple.ConfigHolder;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.config.simple.ConfigSettings;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.lang.reflect.RecordComponent;
import java.util.*;

@Environment(EnvType.CLIENT)
public class SimpleScreenBuilder {

    public static void saveConfigs(String modID) {
        Map<String, Set<SimpleConfigScreen>> simpleConfigsWithScreen = ClientConfigRegistry.getAllConfigsWithScreen();
        if(!simpleConfigsWithScreen.containsKey(modID)) return;

        for(SimpleConfigScreen simpleConfig : simpleConfigsWithScreen.get(modID))  {
            SimpleScreenBuilder.saveConfig(simpleConfig.simpleConfig(), modID);
        }
    }

    public static <T> void saveConfig(Class<T> config, String modID) {
        T configInstance = ConfigRegistry.get(config);
        try {
            // Rebuild and save updated config instance
            T updated = rebuildConfigInstance(config, configInstance);

            ConfigHolder<T> holder = ConfigRegistry.holder(config);
            holder.update(updated);
            Objects.requireNonNull(ClientConfigRegistry.getScreen(modID, config)).onScreenSave().run();
            holder.save();
        } catch (Exception e) {
            CristelLib.LOGGER.error("Couldn't save config screen: {}; ErrorMsg: {}", config.getSimpleName(), e.getMessage());
        }
    }


    public static <T> void addConfigToCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder, SimpleConfigScreen simpleScreen) {
        Class<T> config = (Class<T>) simpleScreen.simpleConfig();
        T configInstance = ConfigRegistry.get(config);
        ConfigSettings<T> settings = ConfigRegistry.getSettings(config);
        HashMap<String, String> comments = ConfigHolder.getSafeComments(settings.getComments());

        String categoryName = simpleScreen.screenName();
        ConfigCategory category = builder.getOrCreateCategory(Component.literal(categoryName.isEmpty() ? config.getSimpleName() : categoryName));
        String header = settings.getHeader();
        if(header != null && !header.isEmpty())
            category.addEntry(entryBuilder.startTextDescription(Component.literal(header.trim())).build());

        for (RecordComponent component : config.getRecordComponents()) {
            String name = component.getName();
            Class<?> type = component.getType();

            Object value;
            Object defaultValue;
            try {
                value = component.getAccessor().invoke(configInstance);
                defaultValue = component.getAccessor().invoke(settings.getDefault());
            } catch (Exception e) {
                CristelLib.LOGGER.error("Couldn't read config value for config: {}; ErrorMsg: {}", config.getSimpleName(), e.getMessage());
                continue;
            }

            // Handle supported types (String, int, boolean, List<String>) just like before
            if (type == String.class) {
                category.addEntry(entryBuilder.startStrField(Component.literal(name), (String) value)
                        .setDefaultValue((String) defaultValue)
                        .setTooltipSupplier(() -> tooltip(name, comments))
                        .setSaveConsumer(val -> updateFieldValue(config, name, val))
                        .build());
            } else if (type == int.class || type == Integer.class) {
                category.addEntry(entryBuilder.startIntField(Component.literal(name), (Integer) value)
                        .setDefaultValue((Integer) defaultValue)
                        .setTooltipSupplier(() -> tooltip(name, comments))
                        .setSaveConsumer(val -> updateFieldValue(config, name, val))
                        .build());
            } else if (type == boolean.class || type == Boolean.class) {
                category.addEntry(entryBuilder.startBooleanToggle(Component.literal(name), (Boolean) value)
                        .setDefaultValue((Boolean) defaultValue)
                        .setTooltipSupplier(() -> tooltip(name, comments))
                        .setSaveConsumer(val -> updateFieldValue(config, name, val))
                        .build());
            } else if (type == List.class && value instanceof List<?> list && list.stream().allMatch(it -> it instanceof String)) {
                category.addEntry(entryBuilder.startStrList(Component.literal(name), new ArrayList<>((List<String>) value))
                        .setDefaultValue((List<String>) defaultValue)
                        .setTooltipSupplier(() -> tooltip(name, comments))
                        .setSaveConsumer(val -> updateFieldValue(config, name, val))
                        .build());
            } else {
                category.addEntry(entryBuilder.startTextDescription(Component.literal("Unsupported type: " + name)).build());
            }

        }
    }

    public static Optional<Component[]> tooltip(String name, Map<String, String> comments) {
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

    private static <T> T rebuildConfigInstance(Class<T> clazz, Object oldInstance) throws Exception {
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
