package de.cristelknight.cristellib.fabric;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.api.CristelLibAPI;
import de.cristelknight.cristellib.data.PathFinder;
import de.cristelknight.cristellib.util.Platform;
import de.cristelknight.cristellib.util.Util;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CristelLibExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static PackResources registerBuiltinResourcePack(ResourceLocation id, Component displayName) {
        String modID = id.getNamespace();
        ModContainer container = FabricLoader.getInstance().getModContainer(modID).orElse(null);
        if(container != null){
            return ModNioResourcePack.create(id.toString(), container, id.getPath(), PackType.SERVER_DATA, ResourcePackActivationType.ALWAYS_ENABLED, false);
        }
        else {
            CristelLib.LOGGER.warn("Couldn't get mod container for modID: {}", modID);
            return null;
        }
    }

    public static void findInModFiles(String modId, String startingFolder, Predicate<Path> fileFilter, Consumer<String> consumer) {
        for (var root : getRootPaths(modId)) {
            try {
                PathFinder.walk(root.resolve(startingFolder), fileFilter, consumer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Map<String, Set<StructureConfig>> getConfigs(CristelLibRegistry registry) {
        Map<String, Set<StructureConfig>> configs = new HashMap<>();
        // Read Custom Code Configs
        FabricLoader.getInstance().getEntrypointContainers("cristellib", CristelLibAPI.class).forEach(entrypoint -> {
            String modId = entrypoint.getProvider().getMetadata().getId();
            CristelLibAPI api = entrypoint.getEntrypoint();
            CristelLib.readAPI(registry, modId, api, configs);
        });

        Util.readData(configs, registry); // Read Custom Data Configs
        return configs;
    }

    public static List<String> getModIds() {
        return FabricLoader.getInstance().getAllMods().stream().map(mod -> mod.getMetadata().getId()).toList();
    }

    @SuppressWarnings("SameReturnValue")
    public static Platform getPlatform() {
        return Platform.FABRIC;
    }

    public static String getModDisplayName(String modID) {
        return FabricLoader.getInstance()
                .getModContainer(modID)
                .map(container -> container.getMetadata().getName()) // human-readable name
                .orElse(modID); // fallback to ID if not found
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static InputStream getResourceStream(String modId, String subPath) {
        InputStream inputStream;
        Path pathC = getResourceDirectory(modId, subPath);

        if(pathC == null) return null;
        try {
            inputStream = Files.newInputStream(pathC);
        } catch (IOException e) {
            CristelLib.LOGGER.warn("Couldn't create Input Stream for Path {}", pathC, e);
            return null;
        }
        return inputStream;
    }


    // Internal
    private static @Nullable Path getResourceDirectory(String modId, String subPath) {
        ModContainer container = FabricLoader.getInstance().getModContainer(modId).orElse(null);
        if(container != null){
            Path path = container.findPath(subPath).orElse(null);
            if(path == null) CristelLib.LOGGER.debug("Path for subPath: {} in modId: {} is null", subPath, modId);
            return path;
        }
        CristelLib.LOGGER.debug("Mod container for modId: {} is null", modId);
        return null;
    }

    // Internal
    public static List<Path> getRootPaths(String modId) {
        ModContainer container = FabricLoader.getInstance().getModContainer(modId).orElse(null);
        List<Path> paths = new ArrayList<>();
        if(container != null){
            paths = container.getRootPaths();
        }
        return paths;
    }
}
