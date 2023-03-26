package net.cristellib.api.builtinpacks;

import net.cristellib.CristelLib;
import net.cristellib.CristelLibExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
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
		list.add(new Tuple<>(new Tuple<>(displayName, CristelLibExpectPlatform.registerBuiltinResourcePack(path, displayName, modid)), supplier));
	}

	public static void registerPack(PackResources packResource, Component displayName, Supplier<Boolean> supplier){
		list.add(new Tuple<>(new Tuple<>(displayName, packResource), supplier));
	}

	private static List<Tuple<Tuple<Component, PackResources>, Supplier<Boolean>>> list = new ArrayList<>();

	public static void getPacks(Consumer<Pack> consumer){
		if(list.isEmpty()) return;
		/*
		List<Tuple<Tuple<Component, PackResources>, Supplier<Boolean>>> copiedList = new ArrayList<>(list);
		copiedList.add(new Tuple<>(new Tuple<>(Component.literal(""), CristelLib.DATA_PACK), () -> true));
		 */
		for (Tuple<Tuple<Component, PackResources>, Supplier<Boolean>> entry : list) {
			if(entry.getB().get()){
				PackResources pack = entry.getA().getB();
				Component displayName = entry.getA().getA();
				if(pack == null){
					CristelLib.LOGGER.error("Pack for " + displayName.getString() + " is null");
					continue;
				}

				//writeStreamToFile("acacia" + list.indexOf(entry), new ResourceLocation("minecraft", "worldgen/structure_set/villages.json"), pack);

				if (!pack.getNamespaces(PackType.SERVER_DATA).isEmpty()) {
					Pack profile = Pack.readMetaAndCreate(
							pack.packId(),
							displayName,
							true,
							ignored -> pack,
							PackType.SERVER_DATA,
							Pack.Position.TOP,
							new BuiltinResourcePackSource()
					);
					//Pack profile = Pack.create(entry.getA(), true, () -> pack, factory, Pack.Position.TOP, new BuiltinModResourcePackSource(pack.getFabricModMetadata().getId()));
					if (profile != null) {
						consumer.accept(profile);
					}
					else CristelLib.LOGGER.error(pack + " couldn't be created");
				}
				else CristelLib.LOGGER.debug(pack + " has no data");
			}
		}
	}
}
