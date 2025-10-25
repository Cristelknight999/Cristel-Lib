package de.cristelknight.cristellib.config.simple;

import com.mojang.serialization.Codec;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.config.client.simple.ClientConfigRegistry;
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
    public static <T> void registerWithScreen(Class<T> clazz, ConfigSettings<T> spec, String modIdForScreen, String screenName) {
        registerWithScreen(clazz, spec, modIdForScreen, screenName, () -> {});
    }

    /**
     * REQUIRES CLOTH CONFIG TO DISPLAY
     */
    public static <T> void registerWithScreen(Class<T> clazz, ConfigSettings<T> spec, String modIdForScreen, String screenName, Runnable onScreenSave) {
        register(clazz, spec);
        if(CristelLibExpectPlatform.isClient() && Util.isClothConfigLoaded())
            ClientConfigRegistry.registerScreen(modIdForScreen, screenName, onScreenSave, clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        ConfigHolder<T> holder = (ConfigHolder<T>) CONFIGS.get(clazz);
        if (holder == null) throw new IllegalStateException("No config registered for: " + clazz.getName());
        return holder.getInstance();
    }

    public static <T> ConfigSettings<T> getSettings(Class<T> clazz) {
        return holder(clazz).getSettings();
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigHolder<T> holder(Class<T> clazz) {
        return (ConfigHolder<T>) CONFIGS.get(clazz);
    }

    public static <T> void updateAndSave(T newInstance) {
        @SuppressWarnings("unchecked")
        ConfigHolder<T> holder = (ConfigHolder<T>) CONFIGS.get(newInstance.getClass());
        if (holder == null) {
            throw new IllegalStateException("No config registered for: " + newInstance.getClass().getName());
        }
        holder.updateAndSave(newInstance);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClazzFromCodec(Codec<T> codec) {
        for(Map.Entry<Class<?>, ConfigHolder<?>> entry : CONFIGS.entrySet()) {
            if(entry.getValue().getSettings().getCodec().equals(codec)) return (Class<T>) entry.getKey();
        }
        return null;
    }
}
