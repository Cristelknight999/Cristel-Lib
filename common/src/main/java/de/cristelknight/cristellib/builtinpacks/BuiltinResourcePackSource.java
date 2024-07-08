package de.cristelknight.cristellib.builtinpacks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.NotNull;

public class BuiltinResourcePackSource implements PackSource {

    public BuiltinResourcePackSource() {
    }

    @Override
    public boolean shouldAddAutomatically() {
        return true;
    }

    @Override
    public @NotNull Component decorate(@NotNull Component packName) {
        return Component.translatable("cristellib.nameAndSource", packName, Component.translatable("cristellib.builtinPack")).withStyle(ChatFormatting.GRAY);
    }
}
