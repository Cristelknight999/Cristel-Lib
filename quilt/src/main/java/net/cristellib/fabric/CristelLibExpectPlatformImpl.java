package net.cristellib.fabric;

import net.cristellib.CristelLib;
import net.cristellib.CristelLibRegistry;
import net.cristellib.StructureConfig;
import net.cristellib.fabriclike.CristelLibFabricLikePlatform;
import net.cristellib.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;
import org.quiltmc.qsl.resource.loader.impl.ModNioResourcePack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CristelLibExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return QuiltLoader.getConfigDir();
    }

    public static PackResources registerBuiltinResourcePack(ResourceLocation id, Component displayName, String modid) {
        ModContainer container = QuiltLoader.getModContainer(modid).orElse(null);
        if(container != null){
            Path resourcePackPath = container.getPath(id.getPath()).toAbsolutePath().normalize();
            if (!Files.exists(resourcePackPath)) {
                CristelLib.LOGGER.warn("Couldn't find built-in pack for modid: " + modid + ", at: " + resourcePackPath);
                return null;
            }
            return new ModNioResourcePack(null, container.metadata(), displayName, ResourcePackActivationType.ALWAYS_ENABLED, resourcePackPath, PackType.SERVER_DATA, null);
        }
        else {
            CristelLib.LOGGER.warn("Couldn't get mod container for modid: " + modid);
            return null;
        }
    }


    public static @Nullable Path getResourceDirectory(String modid, String subPath) {
        return CristelLibFabricLikePlatform.getResourceDirectory(modid, subPath);
        /*
        ModContainer container = QuiltLoader.getModContainer(modid).orElse(null);
        if(container != null){
            Path path = container.getPath(subPath);
            if(path == null){
                CristelLib.LOGGER.warn("Path for subPath: " + subPath + " in modId: " + modid + " is null");
            }
            return path;
        }
        CristelLib.LOGGER.warn("Mod container for modId:" + modid + " is null");
        return null;

         */
    }


    public static Map<String, Set<StructureConfig>> getConfigs(CristelLibRegistry registry) {
        return CristelLibFabricLikePlatform.getConfigs(registry);
        /*
        Map<String, Set<StructureConfig>> configs = new HashMap<>();
        QuiltLoader.getEntrypointContainers("cristellib", CristelLibAPI.class).forEach(entrypoint -> {
            String modId = entrypoint.getProvider().metadata().id();
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
        Util.addAll(configs, CristelLibFabricPlatform.data(registry));
        return configs;

         */
    }

    public static List<Path> getRootPaths(String modId) {
        return CristelLibFabricLikePlatform.getRootPaths(modId);
        /*
        ModContainer container = QuiltLoader.getModContainer(modId).orElse(null);
        List<Path> paths = new ArrayList<>();
        if(container != null){
            paths = Collections.singletonList(container.rootPath());
        }
        return paths;

         */
    }

    public static boolean isModLoaded(String modId) {
        return QuiltLoader.isModLoaded(modId);
    }

    public static boolean isModLoadedWithVersion(String modid, String minVersion) {
        return CristelLibFabricLikePlatform.isModLoadedWithVersion(modid, minVersion);
    }

    public static Platform getPlatform() {
        return Platform.QUILT;
    }


}
