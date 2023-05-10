package net.cristellib.fabric;

import net.cristellib.CristelLib;
import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;
import net.cristellib.builtinpacks.PackWithoutConstructor;
import net.cristellib.fabriclike.CristelLibFabricPlatform;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CristelLibExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return CristelLibFabricPlatform.getConfigDirectory();
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

    public static @Nullable AbstractPackResources createPack(String name, String subPath, String modId) {
        ModContainer container = FabricLoader.getInstance().getModContainer(modId).orElse(null);
        if(container != null){
            ModNioResourcePack pack = ModNioResourcePack.create(name, container, subPath, PackType.SERVER_DATA, ResourcePackActivationType.ALWAYS_ENABLED);
            /*
            if(pack == null) return null;
            return PackWithoutConstructor.of(pack);
             */
            return pack;
        }
        else {
            CristelLib.LOGGER.warn("Couldn't get mod container for modid: " + modId);
            return null;
        }
    }

}
