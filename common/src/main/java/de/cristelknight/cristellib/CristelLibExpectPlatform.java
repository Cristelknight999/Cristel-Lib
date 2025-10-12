package de.cristelknight.cristellib;

import de.cristelknight.cristellib.data.PathFinder;
import de.cristelknight.cristellib.util.Platform;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public static PackResources registerBuiltinResourcePack(ResourceLocation id, Component displayName) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Map<String, Set<StructureConfig>> getConfigs(CristelLibRegistry registry) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static String getModDisplayName(String modID) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Platform getPlatform(){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isClient(){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static List<String> getModIds() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static PathFinder.PathFinderData findInModFiles(String modId, Set<String> modsWithConfig) {
        throw new AssertionError();
    }
}
