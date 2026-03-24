package de.cristelknight.cristellib.util.runtimepack;

import com.google.gson.JsonObject;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.builtinpacks.RuntimePack;
import net.minecraft.resources.Identifier;
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


    public static Identifier getLocationForStructureSet(Identifier location) {
        return createJsonLocation("worldgen/structure_set", location);
    }

    public static Identifier createJsonLocation(String prefix, Identifier identifier) {
        return createIdentifier(prefix, "json", identifier);
    }

    public static Identifier createIdentifier(String prefix, String end, Identifier identifier) {
        return Identifier.fromNamespaceAndPath(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + end);
    }

}