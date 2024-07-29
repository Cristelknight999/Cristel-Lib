package de.cristelknight.cristellib.fabric;

import de.cristelknight.cristellib.data.ReadData;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.api.CristelLibAPI;
import de.cristelknight.cristellib.util.Platform;
import de.cristelknight.cristellib.util.Util;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

public class CristelLibExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static PackResources registerBuiltinResourcePack(ResourceLocation id, Component displayName, String modid) {
        ModContainer container = FabricLoader.getInstance().getModContainer(modid).orElse(null);
        if(container != null){
            return ModNioResourcePack.create(id.toString(), container, id.getPath(), PackType.SERVER_DATA, ResourcePackActivationType.ALWAYS_ENABLED, false);
        }
        else {
            CristelLib.LOGGER.warn("Couldn't get mod container for modid: {}", modid);
            return null;
        }
    }

    public static @Nullable Path getResourceDirectory(String modid, String subPath) {
        ModContainer container = FabricLoader.getInstance().getModContainer(modid).orElse(null);
        if(container != null){
            Path path = container.findPath(subPath).orElse(null);
            if(path == null) CristelLib.LOGGER.debug("Path for subPath: {} in modId: {} is null", subPath, modid);
            return path;
        }
        CristelLib.LOGGER.debug("Mod container for modId: {} is null", modid);
        return null;
    }



    public static Map<String, Set<StructureConfig>> getConfigs(CristelLibRegistry registry) {
        Map<String, Set<StructureConfig>> configs = new HashMap<>();
        FabricLoader.getInstance().getEntrypointContainers("cristellib", CristelLibAPI.class).forEach(entrypoint -> {
            String modId = entrypoint.getProvider().getMetadata().getId();
            try {
                CristelLibAPI api = entrypoint.getEntrypoint();
                Set<StructureConfig> set = new HashSet<>();
                api.registerConfigs(set);
                configs.put(modId, set);
                api.registerStructureSets(registry);
            } catch (Throwable e) {
                CristelLib.LOGGER.error("Mod {} provides a broken implementation of CristelLibRegistry", modId, e);
            }
        });
        Util.addAll(configs, data(registry));
        return configs;
    }


    public static Map<String, Set<StructureConfig>> data(CristelLibRegistry registry){
        Map<String, Set<StructureConfig>> modidAndConfigs = new HashMap<>();
        for(ModContainer container : FabricLoader.getInstance().getAllMods()){
            String modid = container.getMetadata().getId();

            ReadData.getBuiltInPacks(modid);
            ReadData.copyFile(modid);
            //ReadData.modifyJson5File(modid);
            ReadData.getStructureConfigs(modid, modidAndConfigs, registry);
        }
        return modidAndConfigs;
    }



    public static List<Path> getRootPaths(String modId) {
        ModContainer container = FabricLoader.getInstance().getModContainer(modId).orElse(null);
        List<Path> paths = new ArrayList<>();
        if(container != null){
            paths = container.getRootPaths();
        }
        return paths;
    }

    public static Platform getPlatform() {
        return Platform.FABRIC;
    }

}
