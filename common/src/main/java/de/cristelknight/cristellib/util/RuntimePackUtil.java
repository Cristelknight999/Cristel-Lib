package de.cristelknight.cristellib.util;

import com.google.gson.JsonObject;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.builtinpacks.RuntimePack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class RuntimePackUtil {

    public static byte @Nullable [] extractImageBytes(Path imageName) {
        InputStream stream;
        try {
            stream = Files.newInputStream(imageName.toAbsolutePath());
            byte[] bytes = stream.readAllBytes();
            stream.close();
            return bytes;
        } catch (IOException e) {
            CristelLib.LOGGER.warn("Couldn't get image for path: {}", imageName, e);
            return null;
        }
    }


    public static byte[] serializeJson(JsonObject object) {
        UnsafeByteArrayOutputStream ubaos = new UnsafeByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(ubaos, StandardCharsets.UTF_8);
        RuntimePack.GSON.toJson(object, writer);
        try {
            writer.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return ubaos.getBytes();
    }


    public static ResourceLocation getLocationForStructureSet(ResourceLocation location){
        return createJsonLocation("worldgen/structure_set", location);
    }

    public static ResourceLocation createJsonLocation(String prefix, ResourceLocation identifier) {
        return createResourceLocation(prefix, "json", identifier);
    }

    public static ResourceLocation createResourceLocation(String prefix, String end, ResourceLocation identifier) {
        return ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + end);
    }

}
