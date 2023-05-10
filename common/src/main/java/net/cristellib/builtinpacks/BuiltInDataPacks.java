package net.cristellib.builtinpacks;

import net.cristellib.CristelLib;
import net.cristellib.CristelLibExpectPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BuiltInDataPacks {

	public static void registerAlwaysOnPack(String name, String subPath, String modid){
		registerPack(name, subPath, modid, () -> true);
	}

	public static void registerPack(String name, String subPath, String modid, Supplier<Boolean> supplier){
		list.add(new Tuple<>(new Tuple<>(name, CristelLibExpectPlatform.createPack(name, subPath, modid)), supplier));
	}

	public static void registerPack(String name, PackResources packResource, Supplier<Boolean> supplier){
		list.add(new Tuple<>(new Tuple<>(name, packResource), supplier));
	}

	private static List<Tuple<Tuple<String, PackResources>, Supplier<Boolean>>> list = new ArrayList<>();

	public static void getPacks(Consumer<Pack> consumer){
		if(list.isEmpty()) return;

		for (Tuple<Tuple<String, PackResources>, Supplier<Boolean>> entry : list) {
			if(entry.getB().get()){
				PackResources pack = entry.getA().getB();
				String displayName = entry.getA().getA();
				if(pack == null){
					CristelLib.LOGGER.error("Pack for " + displayName + " is null");
					continue;
				}
				if (!pack.getNamespaces(PackType.SERVER_DATA).isEmpty()) {
					Pack resourcePackProfile = PackWithoutConstructor.of(pack);
					if (resourcePackProfile != null) {
						consumer.accept(resourcePackProfile);
					}
					else CristelLib.LOGGER.error(pack + " couldn't be created");
				}
				else CristelLib.LOGGER.debug(pack + " has no data");
			}
		}
	}
}
