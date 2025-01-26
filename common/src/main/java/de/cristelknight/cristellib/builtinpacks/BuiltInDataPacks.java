package de.cristelknight.cristellib.builtinpacks;

import de.cristelknight.cristellib.CristelLib;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BuiltInDataPacks {

	public static void registerAlwaysOnPack(ResourceLocation path, String modid, Component displayName){
		registerPack(path, modid, displayName, () -> true);
	}

	public static void registerPack(ResourceLocation path, String modid, Component displayName, Supplier<Boolean> supplier){
		registerPack(CristelLibExpectPlatform.registerBuiltinResourcePack(path, displayName, modid), displayName, supplier);
	}

	public static void registerPack(PackResources packResource, Component displayName, Supplier<Boolean> supplier){
		PACK_LIST.add(new Tuple<>(new Tuple<>(displayName, packResource), supplier));
	}

	private static final List<Tuple<Tuple<Component, PackResources>, Supplier<Boolean>>> PACK_LIST = new ArrayList<>();

	public static void getPacks(Consumer<Pack> consumer){
		if(PACK_LIST.isEmpty()) return;
		for (Tuple<Tuple<Component, PackResources>, Supplier<Boolean>> entry : PACK_LIST) {

			// Check conditions
			if(!entry.getB().get()) continue;

			PackResources pack = entry.getA().getB();
			if (pack.getNamespaces(PackType.SERVER_DATA).isEmpty()) continue;

			Component displayName = entry.getA().getA();
			PackLocationInfo metadata = new PackLocationInfo(
					pack.packId(),
					displayName,
					new BuiltinResourcePackSource(),
					pack.knownPackInfo()
			);
			PackSelectionConfig info2 = new PackSelectionConfig(
					true,
					Pack.Position.TOP,
					true
			);

			Pack profile = Pack.readMetaAndCreate(metadata, new Pack.ResourcesSupplier() {
				@Override
				public @NotNull PackResources openPrimary(PackLocationInfo var1) {
					return pack;
				}

				@Override
				public @NotNull PackResources openFull(PackLocationInfo var1, Pack.Metadata metadata) {
					// Don't support overlays in builtin packs.
					return pack;
				}
			}, PackType.SERVER_DATA, info2);

			if(profile == null){
				CristelLib.LOGGER.error("Pack Profile with display name: {} is null", displayName);
				continue;
			}
			consumer.accept(profile);
		}
	}
}
