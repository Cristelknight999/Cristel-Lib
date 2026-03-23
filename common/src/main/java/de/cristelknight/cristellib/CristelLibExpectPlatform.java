package de.cristelknight.cristellib;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.api.CristelLibAPI;
import de.cristelknight.cristellib.util.Platform;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CristelLibExpectPlatform {

    @ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static InputStream getResourceStream(String modId, String subPath) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Pair<PackResources, PackResources> registerBuiltinResourcePack(Identifier id, Component displayName) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static String getModDisplayName(String modId) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Platform getPlatform() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isClient() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void findInModFiles(String modId, String startingFolder, Predicate<Path> fileFilter, Consumer<String> consumer) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Map<String, CristelLibAPI> getApis() {
        throw new AssertionError();
    }
}