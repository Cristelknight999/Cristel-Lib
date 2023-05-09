package net.cristellib.forge.extrapackutil;

import net.cristellib.builtinpacks.BuiltInDataPacks;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RepositorySourceMaker implements RepositorySource {
	public RepositorySourceMaker() {}

	@Override
	public void loadPacks(Consumer<Pack> consumer, Pack.PackConstructor arg) {
		BuiltInDataPacks.getPacks(consumer, arg);
	}
}