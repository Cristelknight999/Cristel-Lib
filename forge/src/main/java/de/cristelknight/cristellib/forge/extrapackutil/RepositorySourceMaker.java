package de.cristelknight.cristellib.forge.extrapackutil;

import de.cristelknight.cristellib.builtinpacks.BuiltInDataPacks;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RepositorySourceMaker implements RepositorySource {
	public RepositorySourceMaker() {}
	@Override
	public void loadPacks(@NotNull Consumer<Pack> consumer) {
		BuiltInDataPacks.getPacks(consumer);
	}
}