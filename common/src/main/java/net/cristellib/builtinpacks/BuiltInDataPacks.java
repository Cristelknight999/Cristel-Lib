package net.cristellib.builtinpacks;

import net.cristellib.CristelLibExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.Tuple;

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
		list.add(new Tuple<>(new Tuple<>(displayName, packResource), supplier));
	}

	private static final List<Tuple<Tuple<Component, PackResources>, Supplier<Boolean>>> list = new ArrayList<>();

	public static void getPacks(Consumer<Pack> consumer){
		if(list.isEmpty()) return;
		for (Tuple<Tuple<Component, PackResources>, Supplier<Boolean>> entry : list) {
			if(entry.getB().get()){
				PackResources pack = entry.getA().getB();

				// Add the built-in pack only if namespaces for the specified resource type are present.
				if (!pack.getNamespaces(PackType.SERVER_DATA).isEmpty()) {
					// Make the resource pack profile for built-in pack, should never be always enabled.
					PackLocationInfo info = new PackLocationInfo(
							pack.packId(),
							entry.getA().getA(),
							new BuiltinResourcePackSource(),
							pack.knownPackInfo()
					);
					PackSelectionConfig info2 = new PackSelectionConfig(
							true,
							Pack.Position.TOP,
							false
					);

					Pack profile = Pack.readMetaAndCreate(info, new Pack.ResourcesSupplier() {
						@Override
						public PackResources openPrimary(PackLocationInfo var1) {
							return pack;
						}

						@Override
						public PackResources openFull(PackLocationInfo var1, Pack.Metadata metadata) {
							// Don't support overlays in builtin res packs.
							return pack;
						}
					}, PackType.SERVER_DATA, info2);
					consumer.accept(profile);
				}
			}
		}
	}
}
