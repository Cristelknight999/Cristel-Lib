package de.cristelknight.cristellib.config.client.extension;

import de.cristelknight.cristellib.config.client.extension.extensions.ConfigScreenTest;
import de.cristelknight.cristellib.config.client.extension.extensions.SimpleConfigExtension;
import de.cristelknight.cristellib.config.client.simple.ClientConfigRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ExtensionRegistry {

    private static final Map<ExtensionFactory<?>, LoadPredicate> EXTENSIONS = new HashMap<>();

    public static Map<ExtensionFactory<?>, LoadPredicate> getExtensions() {
        return EXTENSIONS;
    }

    public static void registerConfigScreenExtension(ExtensionFactory<?> extension) {
        registerConfigScreenExtension(extension, (modId) -> true);
    }

    public static void registerConfigScreenExtension(ExtensionFactory<?> extension, LoadPredicate predicate) {
        EXTENSIONS.put(extension, predicate);
    }

    static {
        registerConfigScreenExtension(ConfigScreenTest::new, ConfigScreenTest.SHOULD_LOAD);
        registerConfigScreenExtension(SimpleConfigExtension::new, ClientConfigRegistry::hasScreens);
    }

    @FunctionalInterface
    public interface ExtensionFactory<T extends ConfigScreenExtension> {
        T create(String modId);
    }

    @FunctionalInterface
    public interface LoadPredicate {
        boolean test(String modId);
    }
}
