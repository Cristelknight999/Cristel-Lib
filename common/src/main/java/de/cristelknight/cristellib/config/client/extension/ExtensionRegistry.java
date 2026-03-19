package de.cristelknight.cristellib.config.client.extension;

import de.cristelknight.cristellib.config.client.extension.extensions.ConfigScreenTest;
import de.cristelknight.cristellib.config.client.extension.extensions.SimpleConfigExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExtensionRegistry {

    private static final Map<ExtensionFactory<?>, Set<String>> EXTENSIONS = new HashMap<>();

    public static Map<ExtensionFactory<?>, Set<String>> getExtensions() {
        return EXTENSIONS;
    }

    public static void registerConfigScreenExtension(ExtensionFactory<?> extension) {
        registerConfigScreenExtension(extension, Set.of());
    }

    public static void registerConfigScreenExtension(ExtensionFactory<?> extension, Set<String> supportedMods) {
        EXTENSIONS.put(extension, supportedMods);
    }

    static {
        registerConfigScreenExtension(ConfigScreenTest::new);
        registerConfigScreenExtension(SimpleConfigExtension::new);
    }

    @FunctionalInterface
    public interface ExtensionFactory<T extends ConfigScreenExtension> {
        T create(String modId);
    }
}
