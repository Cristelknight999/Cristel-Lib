package net.cristellib;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.repository.Pack;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CristelLibExpectPlatform {

    @ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static @Nullable AbstractPackResources createPack(String name, String subPath, String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static @Nullable Path getResourceDirectory(String modid, String subPath) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static List<Path> getRootPaths(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Map<String, Set<StructureConfig>> getConfigs(CristelLibRegistry registry) {
        throw new AssertionError();
    }

}
