package net.cristellib.fabric;

import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;
import net.cristellib.fabriclike.CristelLibFabricPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CristelLibExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return CristelLibFabricPlatform.getConfigDirectory();
    }

    public static PackResources registerBuiltinResourcePack(ResourceLocation id, Component displayName, String modid) {
        return CristelLibFabricPlatform.registerBuiltinResourcePack(id, displayName, modid);
    }

    public static @Nullable Path getResourceDirectory(String modid, String subPath) {
        return CristelLibFabricPlatform.getResourceDirectory(modid, subPath);
    }



    public static Map<String, Set<StructureConfig>> getConfigs(CristelLibRegistry registry) {
        return CristelLibFabricPlatform.getConfigs(registry);
    }

    public static List<Path> getRootPaths(String modId) {
        return CristelLibFabricPlatform.getRootPaths(modId);
    }

    public static boolean isModLoaded(String modId) {
        return CristelLibFabricPlatform.isModLoaded(modId);
    }

}
