package de.cristelknight.cristellib;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.List;
import java.util.Optional;

public class ModLoadingUtil {

    @ExpectPlatform
    public static List<String> getModIds() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isModLoaded(String modId){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Optional<Integer> compare(String modId, String version){
        throw new AssertionError();
    }
}
