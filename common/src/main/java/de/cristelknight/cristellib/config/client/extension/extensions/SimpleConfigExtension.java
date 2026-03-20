package de.cristelknight.cristellib.config.client.extension.extensions;

import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.config.client.ScreenBuilder;
import de.cristelknight.cristellib.config.client.extension.ConfigScreenExtension;
import de.cristelknight.cristellib.config.client.simple.ClientConfigRegistry;
import de.cristelknight.cristellib.config.client.simple.SimpleConfigScreen;
import de.cristelknight.cristellib.config.client.simple.custom.SimpleScreenTypes;
import de.cristelknight.cristellib.config.simple.ConfigHolder;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.config.simple.ConfigSettings;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

import java.lang.reflect.RecordComponent;
import java.util.*;

@Environment(EnvType.CLIENT)
public class SimpleConfigExtension extends ConfigScreenExtension {

    // Helper: store changes in a map for each config + field
    // TODO: better data structure here
    private static final Map<Class<?>, Map<String, Object>> pendingUpdates = new HashMap<>();

    public SimpleConfigExtension(String modId) {
        super(modId);
    }

    @Override
    public void addToBuilder(ConfigBuilder builder, ConfigEntryBuilder entryBuilder) {
        for (SimpleConfigScreen simpleConfigScreen : ClientConfigRegistry.getScreens(modId)) {
            addConfigToCategory(builder, entryBuilder, simpleConfigScreen);
        }
    }

    @SuppressWarnings({"unchecked"})
    private <T> void addConfigToCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder, SimpleConfigScreen simpleScreen) {
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

            addField(entryBuilder, component, configInstance, settings, config, type, category, name, comments);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private <T> void addField(ConfigEntryBuilder entryBuilder, RecordComponent component, T configInstance, ConfigSettings<T> settings,
                              Class<T> config, Class<?> type, ConfigCategory category, String name, HashMap<String, String> comments
    ) {
        Object value;
        Object defaultValue;
        try {
            value = component.getAccessor().invoke(configInstance);
            defaultValue = component.getAccessor().invoke(settings.getDefault());
        } catch (Exception e) {
            Constants.LOGGER.error("Couldn't read config value for config: {}; ErrorMsg: {}", config.getSimpleName(), e.fillInStackTrace());
            return;
        }

        var optionalEntry = SimpleScreenTypes.getEntry(type, value);
        if(optionalEntry.isEmpty()) {
            category.addEntry(entryBuilder.startTextDescription(Component.literal("Unsupported type: " + name)).build());
            return;
        }

        SimpleScreenTypes.ScreenFieldEntry entry = optionalEntry.get();

        AbstractFieldBuilder<?,?,?> builder1 = entry.fieldFactory().create(entryBuilder, name, value, defaultValue);
        if(entry.hasConverter())
            builder1.setSaveConsumer(val -> updateFieldValue(config, name, entry.toOriginal().get().apply(val)));
        else
            builder1.setSaveConsumer(val -> updateFieldValue(config, name, val));

        builder1.setTooltipSupplier(() -> ScreenBuilder.tooltip(name, comments));
        category.addEntry(builder1.build());
    }

    private <T> void updateFieldValue(Class<T> config, String fieldName, Object value) {
        pendingUpdates.computeIfAbsent(config, k -> new HashMap<>()).put(fieldName, value);
    }

    // Saving
    @Override
    public void onSave() {
        Map<String, Set<SimpleConfigScreen>> simpleConfigsWithScreen = ClientConfigRegistry.getAllConfigsWithScreen();
        if(!simpleConfigsWithScreen.containsKey(modId)) return;

        for(SimpleConfigScreen simpleConfig : simpleConfigsWithScreen.get(modId))  {
            saveConfig(simpleConfig.simpleConfig(), modId);
        }
    }

    private <T> void saveConfig(Class<T> config, String modId) {
        T configInstance = ConfigRegistry.get(config);
        try {
            // Rebuild and save updated config instance
            T updated = rebuildConfigInstance(config, configInstance);

            ConfigHolder<T> holder = ConfigRegistry.holder(config);
            holder.update(updated);
            Objects.requireNonNull(ClientConfigRegistry.getScreen(modId, config)).onScreenSave().run();
            holder.save();
        } catch (Exception e) {
            Constants.LOGGER.error("Couldn't save config screen: {}; ErrorMsg: {}", config.getSimpleName(), e.fillInStackTrace());
        }
    }

    private <T> T rebuildConfigInstance(Class<T> clazz, Object oldInstance) throws Exception {
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
