package de.cristelknight.cristellib.platform;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.api.CristelLibAPI;
import de.cristelknight.cristellib.api.CristelPlugin;
import de.cristelknight.cristellib.builtinpacks.BuiltinResourcePackSource;
import de.cristelknight.cristellib.extraapiutil.APIFinder;
import de.cristelknight.cristellib.mixin.JarContentsPackResourcesAccessor;
import de.cristelknight.cristellib.platform.services.IPlatformHelper;
import de.cristelknight.cristellib.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.jarcontents.JarResource;
import net.neoforged.fml.jarcontents.JarResourceVisitor;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforge.resource.JarContentsPackResources;
import net.neoforged.neoforgespi.locating.IModFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public InputStream getResourceStream(String modId, String subPath) {
        IModFile file = getModFile(modId);
        if (file == null) return null;

        InputStream inputStream;
        try {
            inputStream = file.getContents().openFile(subPath);
        } catch (IOException e) {
            Constants.LOG.warn("Couldn't create Input Stream for sub path: {} in mod: {}", subPath, modId, e);
            return null;
        }

        return inputStream;
    }

    @Override
    public Pair<PackResources, PackResources> registerBuiltinResourcePack(Identifier id, Component displayName) {
        String modId = id.getNamespace();
        String path = id.getPath();

        IModFile file = getModFile(modId);
        if (file == null) return null;

        PackLocationInfo metadata = new PackLocationInfo(
                id.toString(),
                displayName,
                new BuiltinResourcePackSource(),
                Optional.empty()
        );

        PackResources server = new JarContentsPackResources(metadata, file.getContents(), path);
        PackResources client = new JarContentsPackResources(metadata, file.getContents(), path);

        return new Pair<>(
                server.getNamespaces(PackType.SERVER_DATA).isEmpty() ? null : server,
                client.getNamespaces(PackType.CLIENT_RESOURCES).isEmpty() ? null : client
        );
    }

    @Override
    public PackResources createOverlay(PackResources pack, String overlay) {
        if(!(pack instanceof JarContentsPackResourcesAccessor accessor)) {
            Constants.LOG.warn("Couldn't create overlay for Pack: {}, because it does not support overlays", pack.packId());
            return null;
        }
        return new JarContentsPackResources(
                pack.location(),
                accessor.cristellib$getJarContents(),
                accessor.cristellib$getPrefix() + "/" + overlay
        );
    }

    @Override
    public String getModDisplayName(String modId) {
        return ModList.get()
                .getModContainerById(modId)
                .map(container -> container.getModInfo().getDisplayName())
                .orElse(modId); // fallback to modId if nothing is found
    }

    @Override
    public Platform getPlatform() {
        return Platform.NEO_FORGE;
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.getDist().isClient();
    }

    @Override
    public void findInModFiles(String modId, String startingFolder, Predicate<Path> fileFilter, Consumer<String> consumer) {
        IModFile file = getModFile(modId);
        if (file == null) return;
        walk(file.getContents(), startingFolder, fileFilter, consumer);
    }

    @Override
    public Map<String, CristelLibAPI> getApis() {
        Map<String, CristelLibAPI> apiMap = new HashMap<>();
        List<Pair<List<String>, CristelLibAPI>> apis = APIFinder.scanForAPIs(CristelPlugin.class, CristelLibAPI.class);

        for (Pair<List<String>, CristelLibAPI> apiPair : apis) {
            String modId = apiPair.getFirst().getFirst(); // just get main mod hopefully
            apiMap.put(modId, apiPair.getSecond());
        }
        return apiMap;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

    // Internal
    public static IModFile getModFile(String modId) {
        ModList modList = ModList.get();
        IModFile file;
        if (modList == null) {
            ModInfo info = NeoForgeModLoadingUtil.getPreLoadedModInfo(modId);
            if (info == null) {
                Constants.LOG.warn("Mod info for modId: {} is null", modId);
                return null;
            }
            file = info.getOwningFile().getFile();
        } else {
            ModContainer container = modList.getModContainerById(modId).orElse(null);
            if (container == null) {
                Constants.LOG.warn("Mod container for modId: {} is null", modId);
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
                if (!fileFilter.test(Path.of(relativePath))) return;
                consumer.accept(relativePath);
            }
        };

        contents.visitContent(startingFolder, visitor);
    }
}