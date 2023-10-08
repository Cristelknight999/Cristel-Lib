package net.cristellib;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.cristellib.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.Nullable;

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
    public static @Nullable Path getResourceDirectory(String modid, String subPath) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static PackResources registerBuiltinResourcePack(ResourceLocation id, Component displayName, String modid) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static List<Path> getRootPaths(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Map<String, Set<StructureConfig>> getConfigs(CristelLibRegistry registry) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Platform getPlatform(){
        throw new AssertionError();
    }


}
