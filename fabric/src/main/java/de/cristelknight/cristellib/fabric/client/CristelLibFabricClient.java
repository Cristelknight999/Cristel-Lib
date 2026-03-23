package de.cristelknight.cristellib.fabric.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.cristelknight.cristellib.ModLoadingUtil;
import de.cristelknight.cristellib.config.client.ScreenBuilder;
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
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class CristelLibFabricClient implements ClientModInitializer {

    private static boolean shouldOpenScreen = false;

    private static Screen pending = null;

    @Override
    public void onInitializeClient() {
        registerClientCommands();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (shouldOpenScreen) {
                shouldOpenScreen = false;
                if (pending != null) client.setScreen(pending);
                pending = null;
            }
        });
    }

    private static void registerClientCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerOpenScreenCmd(dispatcher);
        });
    }

    private static void registerOpenScreenCmd(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("cristellib_screen")
                .then(ClientCommandManager.argument("mod_id", StringArgumentType.string()).suggests(new ScreenSuggestionProvider())
                        .executes(ctx ->
                                showScreen(ctx, StringArgumentType.getString(ctx, "mod_id")))
                )
        );
    }

    private static class ScreenSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
            Set<String> allScreens = ScreenBuilder.allModsWithScreen();

            for (String screen : allScreens) {
                builder.suggest(screen);
            }

            return builder.buildFuture();
        }
    }

    private static int showScreen(CommandContext<FabricClientCommandSource> ctx, String modId) {
        FabricClientCommandSource source = ctx.getSource();

        if (!Util.isClothConfigLoaded()) {
            source.sendError(Component.literal("Cloth Config is not installed!"));
            return 0;
        }
        if (!ModLoadingUtil.isModLoaded(modId)) {
            source.sendError(Component.literal("Mod: " + modId + " is not installed!"));
            return 0;
        }

        Minecraft.getInstance().execute(() -> {
            pending = new ScreenBuilder(modId).create(null);
            if (pending != null) {
                shouldOpenScreen = true;
            }
        });

        if (!shouldOpenScreen) {
            source.sendError(Component.literal("Mod: " + modId + " has no (enabled) screen!"));
            return 0;
        }

        source.sendFeedback(Component.literal("Opened screen for " + modId));
        return 1;
    }

}