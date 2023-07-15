package net.cristellib.forge;

import com.mojang.datafixers.util.Pair;
import net.cristellib.*;
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
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
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

            ReadData.getBuiltInPacks(modid);
            //ReadData.modifyJson5File(modid);
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

    public static boolean isModLoaded(String modid) {
        ModList modList = ModList.get();
        if(modList != null){
            return modList.isLoaded(modid);
        }
        return isModPreLoaded(modid);
    }

    public static boolean isModPreLoaded(String modid) {
        for(ModInfo info : LoadingModList.get().getMods()){
            if(info.getModId().equals(modid)) return true;
        }
        return false;
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
        for(ModInfo info : LoadingModList.get().getMods()){
            if(info.getModId().equals(modid)) {
                return info.getVersion();
            }
        }
        throw new RuntimeException("Couldn't find mod: " + modid);
    }

    public static Platform getPlatform() {
        return Platform.FORGE;
    }


}
