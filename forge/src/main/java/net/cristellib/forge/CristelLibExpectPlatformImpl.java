package net.cristellib.forge;

import com.mojang.datafixers.util.Pair;
import net.cristellib.CristelLib;
import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;
import net.cristellib.Util;
import net.cristellib.api.CristelLibAPI;
import net.cristellib.builtinpacks.PackWithoutConstructor;
import net.cristellib.data.ReadData;
import net.cristellib.forge.extraapiutil.APIFinder;
import net.cristellib.forge.extrapackutil.ModResourcePack;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;

public class CristelLibExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }


    public static @Nullable Path getResourceDirectory(String modid, String subPath) {
        IModFileInfo container = ModList.get().getModFileById(modid);
        if(container != null){
            Path path = container.getFile().findResource(subPath);
            if(path == null){
                CristelLib.LOGGER.warn("Path for subPath: " + subPath + " in modId: " + modid + " is null");
            }
            return path;
        }
        CristelLib.LOGGER.warn("Mod container for modId:" + modid + " is null");
        return null;
    }

    public static Map<String, Set<StructureConfig>> getConfigs(CristelLibRegistry registry) {
        Map<String, Set<StructureConfig>> configs = new HashMap<>();
        List<Pair<List<String>, CristelLibAPI>> apis = APIFinder.scanForAPIs();

        for(Pair<List<String>, CristelLibAPI> apiPair : apis){

            CristelLibAPI api = apiPair.getSecond();
            List<String> modIds = apiPair.getFirst();
            //modIds.forEach(modid -> CristelLib.LOGGER.error("Found API for modid: " + modid));

            Set<StructureConfig> set = new HashSet<>();
            api.registerConfigs(set);
            configs.put(modIds.get(0), set);
            api.registerStructureSets(registry);
        }
        Util.addAll(configs, data(registry));
        return configs;
    }

    public static Map<String, Set<StructureConfig>> data(CristelLibRegistry registry) {
        Map<String, Set<StructureConfig>> modidAndConfigs = new HashMap<>();
        for(IModInfo container : ModList.get().getMods()){
            String modid = container.getModId();
            //List<Path> dataPacks = getPathsInDir(modid, "data/cristellib/data_packs");
            ReadData.getBuiltInPacks(modid);
            ReadData.copyFile(modid);
            ReadData.getStructureConfigs(modid, modidAndConfigs, registry);
        }
        return modidAndConfigs;
    }

    public static List<Path> getRootPaths(String modId) {
        ModContainer container = ModList.get().getModContainerById(modId).orElse(null);
        List<Path> paths = new ArrayList<>();
        if(container != null){
            paths = Collections.singletonList(container.getModInfo().getOwningFile().getFile().getSecureJar().getRootPath());
        }
        return paths;
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static @Nullable AbstractPackResources createPack(String name, String subPath, String modId) {
        ModContainer container = ModList.get().getModContainerById(modId).orElse(null);
        if(container != null){
            ModResourcePack pack = ModResourcePack.create(name, container.getModInfo(), subPath);
            return pack;
            /*
            if(pack == null) return null;
            return PackWithoutConstructor.of(pack);
             */
        }
        else {
            CristelLib.LOGGER.warn("Couldn't get mod container for modid: " + modId);
            return null;
        }
    }


}
