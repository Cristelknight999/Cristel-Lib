package net.cristellib.fabric;

import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;
import net.cristellib.fabriclike.CristelLibFabricLikePlatform;
import net.cristellib.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CristelLibExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return CristelLibFabricLikePlatform.getConfigDirectory();
    }

    public static PackResources registerBuiltinResourcePack(ResourceLocation id, Component displayName, String modid) {
        return CristelLibFabricLikePlatform.registerBuiltinResourcePack(id, displayName, modid);
    }

    public static @Nullable Path getResourceDirectory(String modid, String subPath) {
        return CristelLibFabricLikePlatform.getResourceDirectory(modid, subPath);
    }

    public static Map<String, Set<StructureConfig>> getConfigs(CristelLibRegistry registry) {
        return CristelLibFabricLikePlatform.getConfigs(registry);
    }

    public static List<Path> getRootPaths(String modId) {
        return CristelLibFabricLikePlatform.getRootPaths(modId);
    }

    public static Platform getPlatform() {
        return Platform.FABRIC;
    }

}
