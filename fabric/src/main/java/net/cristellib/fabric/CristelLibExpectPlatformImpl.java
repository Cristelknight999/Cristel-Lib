package net.cristellib.fabric;

import net.cristellib.CristelLib;
import net.cristellib.CristelLibRegistry;
import net.cristellib.Util;
import net.cristellib.api.CristelLibAPI;
import net.cristellib.StructureConfig;
import net.cristellib.builtinpacks.BuiltinResourcePackSource;
import net.cristellib.data.ReadData;
import net.cristellib.fabriclike.CristelLibFabricPlatform;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.FabricModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;

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

    public static @Nullable Pack createPack(String name, String subPath, String modId, Pack.PackConstructor factory) {
        ModContainer container = FabricLoader.getInstance().getModContainer(modId).orElse(null);
        if(container != null){
            return Pack.create(name,
                    true, () -> ModNioResourcePack.create(name, container, subPath, PackType.SERVER_DATA, ResourcePackActivationType.ALWAYS_ENABLED), factory, Pack.Position.TOP,
                    new BuiltinResourcePackSource());
        }
        else {
            CristelLib.LOGGER.warn("Couldn't get mod container for modid: " + modId);
            return null;
        }
    }

}
