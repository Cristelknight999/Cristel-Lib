package de.cristelknight.cristellib.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.cristelknight.cristellib.CristelLib;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CristelLibFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CristelLib.init();
        if(FabricLoader.getInstance().isDevelopmentEnvironment()) register();
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            register(dispatcher);
        });
    }

    private static int dumpPack(CommandContext<CommandSourceStack> ctx, String outputPathStr) {
        CommandSourceStack source = ctx.getSource();
        try {
            Path path = Paths.get(outputPathStr);
            CristelLib.RUNTIME_PACK.dumpToFolder(path);
            source.sendSuccess(() -> Component.literal("RuntimePack dumped to: " + path.toAbsolutePath()), false);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to dump RuntimePack: " + e.getMessage()));
            return 0;
        }
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dump_runtime_pack")
                .then(Commands.argument("outputPath", StringArgumentType.string())
                        .executes(ctx -> dumpPack(ctx, StringArgumentType.getString(ctx, "outputPath"))))
        );
    }
}
