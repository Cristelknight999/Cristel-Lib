package de.cristelknight.cristellib;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.List;
import java.util.Optional;

public class ModLoadingUtil {

    @ExpectPlatform
    public static List<String> getModIds() {
        throw new AssertionError();
    }

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<Integer> compare(String modId, String version) {
        throw new AssertionError();
    }
}
