package de.cristelknight.cristellib.config.simple;

import com.mojang.serialization.Codec;
import de.cristelknight.cristellib.config.client.simple.ClientConfigRegistry;
import de.cristelknight.cristellib.PlatformHelper;
import de.cristelknight.cristellib.util.Util;

import java.util.HashMap;
import java.util.Map;

public class ConfigRegistry {

    private static final Map<Class<?>, ConfigHolder<?>> CONFIGS = new HashMap<>();

    public static <T> void register(Class<T> clazz, ConfigSettings<T> spec) {
        CONFIGS.put(clazz, new ConfigHolder<>(spec));
        get(clazz); // read or create config
    }

    /**
     * REQUIRES CLOTH CONFIG TO DISPLAY
     */
    @SuppressWarnings("unused")
    public static <T> void registerWithScreen(Class<T> clazz, ConfigSettings<T> spec, String modIdForScreen, String screenName) {
        registerWithScreen(clazz, spec, modIdForScreen, screenName, () -> {});
    }

    /**
     * REQUIRES CLOTH CONFIG TO DISPLAY
     */
    public static <T> void registerWithScreen(Class<T> clazz, ConfigSettings<T> spec, String modIdForScreen, String screenName, Runnable onScreenSave) {
        register(clazz, spec);
        if (PlatformHelper.isClient() && Util.isClothConfigLoaded())
            ClientConfigRegistry.registerScreen(modIdForScreen, screenName, onScreenSave, clazz);
    }

    public static <T> T get(Class<T> clazz) {
        return holder(clazz).getInstance();
    }

    public static <T> ConfigSettings<T> getSettings(Class<T> clazz) {
        return holder(clazz).getSettings();
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigHolder<T> holder(Class<T> clazz) {
        ConfigHolder<T> holder = (ConfigHolder<T>) CONFIGS.get(clazz);
        if (holder == null)
            throw new IllegalStateException("No config registered for: " + clazz.getName());
        return holder;
    }

    @SuppressWarnings("unchecked")
    public static <T> void updateAndSave(T newInstance) {
        ConfigHolder<T> holder = (ConfigHolder<T>) holder(newInstance.getClass());
        holder.updateAndSave(newInstance);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClazzFromCodec(Codec<T> codec) {
        for (Map.Entry<Class<?>, ConfigHolder<?>> entry : CONFIGS.entrySet()) {
            if (entry.getValue().getSettings().getCodec().equals(codec))
                return (Class<T>) entry.getKey();
        }
        return null;
    }
}
