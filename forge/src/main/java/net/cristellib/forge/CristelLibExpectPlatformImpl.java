package net.cristellib.forge;

import com.mojang.datafixers.util.Pair;
import net.cristellib.CristelLib;
import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;
import net.cristellib.api.CristelLibAPI;
import net.cristellib.data.ReadData;
import net.cristellib.forge.extraapiutil.APIFinder;
import net.cristellib.util.Platform;
import net.cristellib.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PathPackResources;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;

public class CristelLibExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static PackResources registerBuiltinResourcePack(ResourceLocation id, Component displayName, String modid) {
        Path path = getResourceDirectory(modid, id.getPath());
        if(path != null){
            return new PathPackResources(displayName.getString(), path, true);
        }
        return null;
    }

    public static @Nullable Path getResourceDirectory(String modId, String subPath) {
        ModList modList = ModList.get();
        IModFile file;
        if(modList == null){
            ModInfo info = getPreLoadedModInfo(modId);
            if(info == null){
                CristelLib.LOGGER.warn("Mod info for modId:" + modId + " is null");
                return null;
            }
            file = info.getOwningFile().getFile();
        }
        else {
            ModContainer container = modList.getModContainerById(modId).orElse(null);
            if(container == null){
                CristelLib.LOGGER.warn("Mod container for modId:" + modId + " is null");
                return null;
            }
            file = container.getModInfo().getOwningFile().getFile();
        }

        Path path = file.findResource(subPath);
        if(path == null){
            CristelLib.LOGGER.warn("Path for subPath: " + subPath + " in modId: " + modId + " is null");
        }
        return path;
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
        for(String modid : getModIds()){

            ReadData.getBuiltInPacks(modid);
            //ReadData.modifyJson5File(modid);
            ReadData.copyFile(modid);
            ReadData.getStructureConfigs(modid, modidAndConfigs, registry);
        }
        return modidAndConfigs;
    }


    public static List<String> getModIds(){
        ModList modList = ModList.get();
        List<String> modIds = new ArrayList<>();
        if(modList != null){
            for(IModInfo modInfo : modList.getMods()){
                modIds.add(modInfo.getModId());
            }
        }
        else {
            for(IModInfo modInfo : LoadingModList.get().getMods()){
                modIds.add(modInfo.getModId());
            }
        }
        return modIds;
    }


    public static List<Path> getRootPaths(String modId) {
        ModList modList = ModList.get();
        List<Path> paths = new ArrayList<>();
        IModFile file;
        if(modList == null){
            ModInfo info = getPreLoadedModInfo(modId);
            if(info == null) return paths;
            file = info.getOwningFile().getFile();
        }
        else {
            ModContainer container = modList.getModContainerById(modId).orElse(null);
            if(container == null) return paths;
            file = container.getModInfo().getOwningFile().getFile();
        }
        return Collections.singletonList(file.getSecureJar().getRootPath());
    }

    public static boolean isModLoaded(String modid) {
        ModList modList = ModList.get();
        if(modList != null){
            return modList.isLoaded(modid);
        }
        return isModPreLoaded(modid);
    }

    public static boolean isModPreLoaded(String modid) {
        return getPreLoadedModInfo(modid) != null;
    }

    public static @Nullable ModInfo getPreLoadedModInfo(String modId){
        for(ModInfo info : LoadingModList.get().getMods()){
            if(info.getModId().equals(modId)) {
                return info;
            }
        }
        return null;
    }

    public static boolean isModLoadedWithVersion(String modid, String minVersion) {
        if(isModLoaded(modid)){
            ModList modList = ModList.get();
            ArtifactVersion version;
            if(modList != null) version = modList.getModContainerById(modid).get().getModInfo().getVersion();
            else version = getPreLoadedModVersion(modid);

            ArtifactVersion min;
            min = new DefaultArtifactVersion(minVersion);
            return version.compareTo(min) >= 0;
        }
        return false;
    }

    public static ArtifactVersion getPreLoadedModVersion(String modid) {
        ModInfo info = getPreLoadedModInfo(modid);
        if(info == null) throw new RuntimeException("Couldn't find mod: " + modid);
        return info.getVersion();
    }

    public static Platform getPlatform() {
        return Platform.FORGE;
    }


}
