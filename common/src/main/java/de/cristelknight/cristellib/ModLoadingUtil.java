package de.cristelknight.cristellib;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class ModLoadingUtil {
    @ExpectPlatform
    public static boolean isModLoadedWithVersion(String modid, String minVersion){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isModLoaded(String modid){
        throw new AssertionError();
    }
}
