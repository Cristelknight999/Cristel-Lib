package de.cristelknight.cristellib.neoforge.extrapackutil;

import de.cristelknight.cristellib.builtinpacks.BuiltInPackLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record RepositorySourceMaker(PackType type) implements RepositorySource {

    @Override
    public void loadPacks(@NotNull Consumer<Pack> consumer) {
        BuiltInPackLoader.getPacks(consumer, type);
    }
}