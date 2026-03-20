package de.cristelknight.cristellib.neoforge;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibRegistry;
import de.cristelknight.cristellib.StructureConfig;
import de.cristelknight.cristellib.api.CristelLibAPI;
import de.cristelknight.cristellib.builtinpacks.BuiltinResourcePackSource;
import de.cristelknight.cristellib.neoforge.extraapiutil.APIFinder;
import de.cristelknight.cristellib.util.Platform;
import de.cristelknight.cristellib.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.jarcontents.JarResource;
import net.neoforged.fml.jarcontents.JarResourceVisitor;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforge.resource.JarContentsPackResources;
import net.neoforged.neoforgespi.locating.IModFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static de.cristelknight.cristellib.neoforge.ModLoadingUtilImpl.getPreLoadedModInfo;

@SuppressWarnings("unused")
public class CristelLibExpectPlatformImpl {

    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static void findInModFiles(String modId, String startingFolder, Predicate<Path> fileFilter, Consumer<String> consumer) {
        IModFile file = getModFile(modId);
        if(file == null){
            Constants.LOGGER.error("Couldn't get mod file for modId: {}", modId);
            return;
        }
        walk(file.getContents(), startingFolder, fileFilter, consumer);
    }

    public static PackResources registerBuiltinResourcePack(Identifier id, Component displayName) {
        String modId = id.getNamespace();
        String path = id.getPath();

        IModFile file = getModFile(modId);
        if(file == null) return null;

        PackLocationInfo metadata = new PackLocationInfo(
                id.toString(),
                displayName,
                new BuiltinResourcePackSource(),
                Optional.of(new KnownPack(Constants.MOD_ID, id.toString(), ModList.get().getModFileById(modId).versionString()))
        );

        return new JarContentsPackResources(metadata, file.getContents(), path);
    }

    public static InputStream getResourceStream(String modId, String subPath) {
        IModFile file = getModFile(modId);
        if(file == null) return null;

        InputStream inputStream;
        try {
            inputStream = file.getContents().openFile(subPath);
        } catch (IOException e) {
            Constants.LOGGER.warn("Couldn't create Input Stream for sub path: {} in mod: {}", subPath, modId, e);
            return null;
        }

        return inputStream;
    }

    public static Map<String, Set<StructureConfig>> getConfigs(CristelLibRegistry registry) {
        Map<String, Set<StructureConfig>> configs = new HashMap<>();
        List<Pair<List<String>, CristelLibAPI>> apis = APIFinder.scanForAPIs();

        for (Pair<List<String>, CristelLibAPI> apiPair : apis) {
            CristelLibAPI api = apiPair.getSecond();
            String modId = apiPair.getFirst().getFirst(); // just get main mod hopefully
            CristelLib.readAPI(registry, modId, api, configs);
        }
        Util.readData(configs, registry);
        return configs;
    }

    @SuppressWarnings("SameReturnValue")
    public static Platform getPlatform() {
        return Platform.FORGE;
    }

    public static String getModDisplayName(String modId) {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(modId); // fallback to modId if nothing is found
    }

    public static boolean isClient() {
        return FMLEnvironment.getDist().isClient();
    }

    // Internal
    public static IModFile getModFile(String modId) {
        ModList modList = ModList.get();
        IModFile file;
        if (modList == null) {
            ModInfo info = getPreLoadedModInfo(modId);
            if (info == null) {
                Constants.LOGGER.warn("Mod info for modId: {} is null", modId);
                return null;
            }
            file = info.getOwningFile().getFile();
        } else {
            ModContainer container = modList.getModContainerById(modId).orElse(null);
            if (container == null) {
                Constants.LOGGER.warn("Mod container for modId: {} is null", modId);
                return null;
            }
            file = container.getModInfo().getOwningFile().getFile();
        }
        return file;
    }

    // Internal
    private static void walk(JarContents contents, String startingFolder, Predicate<Path> fileFilter, Consumer<String> consumer) {
        var visitor = new JarResourceVisitor() {
            @Override
            public void visit(String relativePath, JarResource resource) {
                if(!fileFilter.test(Path.of(relativePath))) return;
                consumer.accept(relativePath);
            }
        };

        contents.visitContent(startingFolder, visitor);
    }
}
