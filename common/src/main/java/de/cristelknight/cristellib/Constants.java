package de.cristelknight.cristellib;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Constants {
    public static final String MC_ID = Identifier.DEFAULT_NAMESPACE;

    public static final String MOD_ID = "cristellib";
    public static final String MOD_NAME = "Cristel Lib";
    public static final Component MOD_COMPONENT = Component.literal(MOD_NAME).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.UNDERLINE);

    public static final Logger LOG = LogManager.getLogger(MOD_NAME);

    public static String getWithPrefix(String message) {
        return String.format("[%s] %s", MOD_ID, message);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static final Identifier CRISTEL_LIB_PACK_ID = id("runtime_pack");

}