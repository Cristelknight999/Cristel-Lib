package de.cristelknight.cristellib.platform.services;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.api.CristelLibAPI;
import de.cristelknight.cristellib.util.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface IPlatformHelper {

    Path getConfigDirectory();

    InputStream getResourceStream(String modId, String subPath);

    Pair<PackResources, PackResources> registerBuiltinResourcePack(Identifier id, Component displayName);

    PackResources createOverlay(PackResources pack, String overlay);

    String getModDisplayName(String modId);

    Platform getPlatform();

    boolean isClient();

    void findInModFiles(String modId, String startingFolder, Predicate<Path> fileFilter, Consumer<String> consumer);

    Map<String, CristelLibAPI> getApis();

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();
}