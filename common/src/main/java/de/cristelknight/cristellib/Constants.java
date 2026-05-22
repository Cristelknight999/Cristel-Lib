package de.cristelknight.cristellib;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Constants {
    public static final String MC_ID = ResourceLocation.DEFAULT_NAMESPACE;

    public static final String MOD_ID = "cristellib";
    public static final String MOD_NAME = "Cristel Lib";
    public static final Component MOD_COMPONENT = Component.literal(MOD_NAME).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.UNDERLINE);

    public static final Logger LOG = LogManager.getLogger(MOD_NAME);

    public static String getWithPrefix(String message) {
        return String.format("[%s] %s", MOD_ID, message);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static final ResourceLocation CRISTEL_LIB_PACK_ID = id("runtime_pack");

}