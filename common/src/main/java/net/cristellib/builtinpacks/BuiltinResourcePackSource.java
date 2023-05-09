package net.cristellib.builtinpacks;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.PackSource;
import org.jetbrains.annotations.NotNull;

public class BuiltinResourcePackSource implements PackSource {

    public BuiltinResourcePackSource() {
    }


    @Override
    public @NotNull Component decorate(@NotNull Component packName) {
        return new TranslatableComponent("cristellib.nameAndSource", packName, new TranslatableComponent("cristellib.builtinPack")).withStyle(ChatFormatting.GRAY);
    }
}
