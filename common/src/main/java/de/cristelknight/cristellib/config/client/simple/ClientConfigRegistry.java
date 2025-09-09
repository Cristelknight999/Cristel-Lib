package de.cristelknight.cristellib.config.client.simple;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ClientConfigRegistry {

    private static final Map<String, Set<SimpleConfigScreen>> CONFIGS_WITH_SCREEN = new HashMap<>();

    public static Map<String, Set<SimpleConfigScreen>> getAllConfigsWithScreen() {
        return CONFIGS_WITH_SCREEN;
    }

    public static boolean hasScreens(String modID) {
        return CONFIGS_WITH_SCREEN.containsKey(modID);
    }

    public static Set<SimpleConfigScreen> getScreens(String modID){
        if(hasScreens(modID)) return CONFIGS_WITH_SCREEN.get(modID);
        return Set.of();
    }

    public static <T> void registerScreen(String modIdForScreen, String screenName, Runnable onScreenSave, Class<T> simpleConfig) {
        CONFIGS_WITH_SCREEN.computeIfAbsent(modIdForScreen, k -> new HashSet<>()).add(new SimpleConfigScreen(simpleConfig, screenName, onScreenSave));
    }

    public static @Nullable SimpleConfigScreen getScreen(String modID, Class<?> simpleConfig) {
        for(SimpleConfigScreen screen : getScreens(modID)) {
            if(screen.simpleConfig() == simpleConfig) return screen;
        }
        return null;
    }

}
