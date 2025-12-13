package de.cristelknight.cristellib.fabric.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.ModLoadingUtil;
import de.cristelknight.cristellib.autoconfig.ACConfig;
import de.cristelknight.cristellib.config.client.ScreenBuilder;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import de.cristelknight.cristellib.util.Util;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Set;

@Environment(EnvType.CLIENT)
public class CristelLibClient implements ClientModInitializer {

    public static boolean shouldOpenScreen = false;

    public static Screen pending = null;

    @Override
    public void onInitializeClient() {
        registerClient();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (shouldOpenScreen) {
                shouldOpenScreen = false;
                if(pending != null) client.setScreen(pending);
                pending = null;
            }
        });
    }

    public static void registerClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerScreen(dispatcher);
        });
    }

    private static void registerScreen(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("cristellib_screen")
                .then(ClientCommandManager.argument("mod_id", StringArgumentType.string())
                        .executes(ctx ->
                                showScreen(ctx, StringArgumentType.getString(ctx, "mod_id")))
                )
        );

    }

    private static int showScreen(CommandContext<FabricClientCommandSource> ctx, String modID) {
        FabricClientCommandSource source = ctx.getSource();

        if(!Util.isClothConfigLoaded()) {
            source.sendError(Component.literal("Cloth Config is not installed!"));
            return 0;
        }
        if(!ModLoadingUtil.isModLoaded(modID)) {
            source.sendError(Component.literal("Mod: " + modID + " is not installed!"));
            return 0;
        }
        ACConfig acConfig = ConfigRegistry.get(ACConfig.class);
        boolean structureEnabled = !acConfig.disableAutoConfigScreens();

        Set<String> allScreens = ScreenBuilder.allConfigMods(structureEnabled);
        if(!allScreens.contains(modID)) {
            source.sendError(Component.literal("Mod: " + modID + " has no (enabled) screen!"));
            return 0;
        }

        Pair<Boolean, Boolean> structureSimple = ScreenBuilder.shouldCreateScreen(modID, structureEnabled);
        boolean structure = structureSimple.getFirst();
        boolean simple = structureSimple.getSecond();
        if((!structure && !simple) && !modID.equals(CristelLib.MOD_ID)) {
            source.sendError(Component.literal("Mod: " + modID + " has no (enabled) screen!"));
            return 0;
        }

        shouldOpenScreen = true;
        Minecraft.getInstance().execute(() -> {
            pending = new ScreenBuilder(modID).create(null, structure, simple);
        });

        source.sendFeedback(Component.literal("Opened screen for " + modID));
        return 1;
    }

}
