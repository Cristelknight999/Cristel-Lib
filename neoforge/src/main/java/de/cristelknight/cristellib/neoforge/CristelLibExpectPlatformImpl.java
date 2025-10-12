package de.cristelknight.cristellib.neoforge;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.api.CristelLibAPI;
import de.cristelknight.cristellib.builtinpacks.BuiltinResourcePackSource;
import de.cristelknight.cristellib.data.PathFinder;
import de.cristelknight.cristellib.neoforge.extraapiutil.APIFinder;
import de.cristelknight.cristellib.util.Platform;
import de.cristelknight.cristellib.util.Util;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.resource.JarContentsPackResources;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public class CristelLibExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static PathFinder.PathFinderData findInModFiles(String modId, Set<String> modsWithConfig) {
        return PathFinderUtil.getSubPathsInMod(modId, modsWithConfig);
    }

    public static PackResources registerBuiltinResourcePack(ResourceLocation id, Component displayName) {
        String modID = id.getNamespace();
        String path = id.getPath();

        CristelLib.LOGGER.error("trying to load pack: " + id);

        IModFile file = PathFinderUtil.getModFile(modID);
        if(file == null) return null;

        PackLocationInfo metadata = new PackLocationInfo(
                id.toString(),
                displayName,
                new BuiltinResourcePackSource(),
                Optional.of(new KnownPack(CristelLib.MOD_ID, id.toString(), ModList.get().getModFileById(modID).versionString()))
        );

        CristelLib.LOGGER.error("load pack??: " + id);
        return new JarContentsPackResources(metadata, file.getContents(), path);
    }

    public static InputStream getResourceStream(String modId, String subPath) {
        IModFile file = PathFinderUtil.getModFile(modId);
        if(file == null) return null;

        InputStream inputStream;
        try {
            inputStream = file.getContents().openFile(subPath);
        } catch (IOException e) {
            CristelLib.LOGGER.warn("Couldn't create Input Stream for sub path: {} in mod: {}", subPath, modId, e);
            return null;
        }

        return inputStream;
    }

    public static Map<String, Set<StructureConfig>> getConfigs(CristelLibRegistry registry) {
        Map<String, Set<StructureConfig>> configs = new HashMap<>();
        List<Pair<List<String>, CristelLibAPI>> apis = APIFinder.scanForAPIs();

        for (Pair<List<String>, CristelLibAPI> apiPair : apis) {
            CristelLibAPI api = apiPair.getSecond();
            String modID = apiPair.getFirst().getFirst(); // just get main mod hopefully
            CristelLib.readAPI(registry, modID, api, configs);
        }
        Util.readData(configs, registry);
        return configs;
    }

    public static List<String> getModIds() {
        ModList modList = ModList.get();
        List<String> modIds = new ArrayList<>();
        if (modList != null) {
            for (IModInfo modInfo : modList.getMods()) {
                modIds.add(modInfo.getModId());
            }
        } else {
            for (IModInfo modInfo : FMLLoader.getCurrent().getLoadingModList().getMods()) {
                modIds.add(modInfo.getModId());
            }
        }
        return modIds;
    }

    @SuppressWarnings("SameReturnValue")
    public static Platform getPlatform() {
        return Platform.FORGE;
    }

    public static String getModDisplayName(String modId) {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(modId); // fallback to modid if nothing is found
    }

    public static boolean isClient() {
        return FMLEnvironment.getDist().isClient();
    }


}
