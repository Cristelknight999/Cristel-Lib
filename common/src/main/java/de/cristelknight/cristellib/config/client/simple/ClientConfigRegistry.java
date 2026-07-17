package de.cristelknight.cristellib.config.client.simple;

import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClientConfigRegistry {

    private static final ConcurrentMap<String, Set<SimpleConfigScreen>> CONFIGS_WITH_SCREEN = new ConcurrentHashMap<>();

    public static boolean hasScreens(String modId) {
        return CONFIGS_WITH_SCREEN.containsKey(modId);
    }

    public static Set<SimpleConfigScreen> getScreens(String modId) {
        if (hasScreens(modId)) return CONFIGS_WITH_SCREEN.get(modId);
        return Set.of();
    }

    public static void registerScreen(String modIdForScreen, String screenName, Runnable onScreenSave, Class<?> simpleConfig) {
        CONFIGS_WITH_SCREEN.computeIfAbsent(modIdForScreen, _ -> ConcurrentHashMap.newKeySet()).add(new SimpleConfigScreen(simpleConfig, screenName, onScreenSave));
    }

    public static @Nullable SimpleConfigScreen getScreen(String modId, Class<?> simpleConfig) {
        for (SimpleConfigScreen screen : getScreens(modId)) {
            if (screen.simpleConfig() == simpleConfig) return screen;
        }
        return null;
    }

}
