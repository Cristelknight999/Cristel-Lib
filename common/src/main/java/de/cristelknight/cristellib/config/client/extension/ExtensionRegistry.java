package de.cristelknight.cristellib.config.client.extension;

import de.cristelknight.cristellib.config.client.extension.extensions.SimpleConfigExtension;
import de.cristelknight.cristellib.config.client.extension.extensions.StructureConfigExtension;
import de.cristelknight.cristellib.config.client.simple.ClientConfigRegistry;
import de.cristelknight.cristellib.util.Util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ExtensionRegistry {

    private static final ConcurrentMap<ExtensionFactory<?>, LoadPredicate> EXTENSIONS = new ConcurrentHashMap<>();

    public static Map<ExtensionFactory<?>, LoadPredicate> getExtensions() {
        return Map.copyOf(EXTENSIONS);
    }

    @SuppressWarnings("unused")
    public static void registerConfigScreenExtension(ExtensionFactory<?> extension) {
        registerConfigScreenExtension(extension, (modId) -> true);
    }

    public static void registerConfigScreenExtension(ExtensionFactory<?> extensionFactory, LoadPredicate predicate) {
        EXTENSIONS.put(extensionFactory, predicate);
    }

    static {
        if(Util.isClothConfigLoaded()) {
            registerConfigScreenExtension(StructureConfigExtension::new, StructureConfigExtension.SHOULD_LOAD);
            registerConfigScreenExtension(SimpleConfigExtension::new, ClientConfigRegistry::hasScreens);
        }
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
