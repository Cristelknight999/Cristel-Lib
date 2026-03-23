package de.cristelknight.cristellib.builtinpacks;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static de.cristelknight.cristellib.Constants.getWithPrefix;

public class BuiltInPackLoader {

    public static void registerAlwaysOnPack(Identifier path, Component displayName) {
        registerPack(path, displayName, () -> true);
    }

    public static void registerPack(Identifier path, Component displayName, Supplier<Boolean> supplier) {
        Pair<PackResources, PackResources> packs = CristelLibExpectPlatform.registerBuiltinResourcePack(path, displayName);
        if (packs == null)
            return;

        PackResources server = packs.getFirst();
        PackResources client = packs.getSecond();

        // TODO: maybe not allow both
        if(server != null)
            registerPack(server, displayName, supplier);
        if(client != null)
            registerPack(client, displayName, supplier);
    }

    public static void registerPack(PackResources packResource, Component displayName, Supplier<Boolean> supplier) {
        if (frozen)
            throw new RuntimeException(getWithPrefix(String.format("BuiltInPack Registry is already frozen. Cannot add Pack with id: %s", packResource.packId())));
        PACK_LIST.add(new BuiltInPack(packResource, displayName, supplier));
    }

    public static List<String> getCustomIDs() {
        return PACK_LIST.stream().map(pack -> pack.packResource().packId()).filter(id -> !id.equals(Constants.CRISTEL_LIB_PACK_ID.toString())).toList();
    }

    private static final List<BuiltInPack> PACK_LIST = new ArrayList<>();

    public static void getPacks(Consumer<Pack> consumer) {
        if (!frozen) throw new RuntimeException(getWithPrefix("Tried to load Packs before the Registry phase is over!"));
        if (PACK_LIST.isEmpty()) return;
        BuiltInPackConfig config = ConfigRegistry.get(BuiltInPackConfig.class);

        for (BuiltInPack entry : PACK_LIST) {
            PackResources pack = entry.packResource();

            // Check conditions
            if (!entry.supplier().get() ||
                    config.disabledPacks().contains(pack.packId()) ||
                    (pack.getNamespaces(PackType.SERVER_DATA).isEmpty() && pack.getNamespaces(PackType.CLIENT_RESOURCES).isEmpty()))
                continue;

            Component displayName = entry.displayName();
            PackLocationInfo metadata = new PackLocationInfo(
                    pack.packId(),
                    displayName,
                    new BuiltinResourcePackSource(),
                    pack.knownPackInfo()
            );
            PackSelectionConfig info2 = new PackSelectionConfig(
                    true,
                    Pack.Position.TOP,
                    false
            );

            Pack profile = Pack.readMetaAndCreate(metadata, new Pack.ResourcesSupplier() {
                @Override
                public @NotNull PackResources openPrimary(@NonNull PackLocationInfo var1) {
                    return pack;
                }

                @Override
                public @NotNull PackResources openFull(@NonNull PackLocationInfo var1, Pack.@NonNull Metadata metadata) {
                    // Don't support overlays in builtin packs.
                    return pack;
                }
            }, PackType.SERVER_DATA, info2);

            if (profile == null) {
                Constants.LOGGER.error("Pack Profile with display name: {} is null", displayName);
                continue;
            }
            consumer.accept(profile);
        }
    }

    private static boolean frozen = false;

    public static void freeze() {
        frozen = true;
    }
}
