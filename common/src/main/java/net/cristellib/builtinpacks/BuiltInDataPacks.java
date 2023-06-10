package net.cristellib.builtinpacks;

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
		for (Tuple<Tuple<Component, PackResources>, Supplier<Boolean>> entry : list) {
			if(entry.getB().get()){
				PackResources packResources = entry.getA().getB();
				Component displayName = entry.getA().getA();
				if(packResources == null){
					CristelLib.LOGGER.error("Pack for " + displayName.getString() + " is null");
					continue;
				}
				if (!packResources.getNamespaces(PackType.SERVER_DATA).isEmpty()) {
					Pack pack = Pack.readMetaAndCreate(
							packResources.packId(),
							displayName,
							true,
							ignored -> packResources,
							PackType.SERVER_DATA,
							Pack.Position.TOP,
							new BuiltinResourcePackSource()
					);
					if (pack != null) {
						consumer.accept(pack);
					}
					else CristelLib.LOGGER.error(packResources.packId() + " couldn't be created");
				}
				else CristelLib.LOGGER.debug(packResources.packId() + " has no data");
			}
		}
	}
}
