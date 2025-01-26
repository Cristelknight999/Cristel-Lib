package de.cristelknight.cristellib;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.Optional;

public class ModLoadingUtil {
    @ExpectPlatform
    public static boolean isModLoaded(String modid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<Integer> compare(String modid, String version){
        throw new AssertionError();
    }
}
