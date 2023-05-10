package net.cristellib.builtinpacks;

import net.cristellib.CristelLib;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import java.io.IOException;

public class PackWithoutConstructor extends Pack {

    public static PackWithoutConstructor of(PackResources resources){
        PackMetadataSection section = null;
        try {
            section = resources.getMetadataSection(PackMetadataSection.SERIALIZER);
        } catch (IOException e) {
            CristelLib.LOGGER.error("Couldn't get metadata for Pack: " + resources.getName());
        }

        return new PackWithoutConstructor(resources, section);
    }
    public PackWithoutConstructor(PackResources resources, PackMetadataSection section) {
        super(resources.getName(), Component.nullToEmpty(resources.getName()), true, () -> resources, section,
                PackType.SERVER_DATA, Position.TOP, PackSource.BUILT_IN);
    }
}
