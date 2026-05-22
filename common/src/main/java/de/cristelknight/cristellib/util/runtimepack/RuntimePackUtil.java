package de.cristelknight.cristellib.util.runtimepack;

import com.google.gson.JsonObject;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.builtinpacks.RuntimePack;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class RuntimePackUtil {

    public static byte @Nullable [] extractImageBytes(InputStream imageStream) {
        try {
            byte[] bytes = imageStream.readAllBytes();
            imageStream.close();
            return bytes;
        } catch (IOException e) {
            Constants.LOG.warn("Couldn't get image for a RuntimePack");
            return null;
        }
    }

    public static byte[] serializeJson(JsonObject object) {
        UnsafeByteArrayOutputStream ubaos = new UnsafeByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(ubaos, StandardCharsets.UTF_8);
        RuntimePack.GSON.toJson(object, writer);
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(Constants.getWithPrefix("Failed to serialize JsonObject"), e);
        }
        return ubaos.getBytes();
    }


    public static ResourceLocation getLocationForStructureSet(ResourceLocation location) {
        return createJsonLocation("worldgen/structure_set", location);
    }

    public static ResourceLocation createJsonLocation(String prefix, ResourceLocation identifier) {
        return createIdentifier(prefix, "json", identifier);
    }

    public static ResourceLocation createIdentifier(String prefix, String end, ResourceLocation identifier) {
        return ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + end);
    }

}