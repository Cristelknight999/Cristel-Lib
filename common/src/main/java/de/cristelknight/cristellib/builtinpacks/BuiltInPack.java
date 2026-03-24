package de.cristelknight.cristellib.builtinpacks;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

import java.util.function.Supplier;

public record BuiltInPack(PackResources packResource, Component displayName, Supplier<Boolean> supplier, PackType type) {
}