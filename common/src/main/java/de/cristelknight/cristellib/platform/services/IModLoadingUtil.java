package de.cristelknight.cristellib.platform.services;

import java.util.List;
import java.util.Optional;

public interface IModLoadingUtil {

    List<String> getModIds();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    Optional<Integer> compare(String modId, String version);
}
