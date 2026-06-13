package de.cristelknight.cristellib.builtinpacks;

import com.mojang.datafixers.util.Pair;
import de.cristelknight.cristellib.Constants;
import de.cristelknight.cristellib.CristelLibExpectPlatform;
import de.cristelknight.cristellib.config.simple.ConfigRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
            registerPack(server, displayName, supplier, PackType.SERVER_DATA);
        if(client != null)
            registerPack(client, displayName, supplier, PackType.CLIENT_RESOURCES);
    }

    public static void registerPack(PackResources packResource, Component displayName, Supplier<Boolean> supplier, PackType type) {
        if (frozen)
            throw new RuntimeException(getWithPrefix(String.format("BuiltInPack Registry is already frozen. Cannot add Pack with id: %s", packResource.packId())));
        PACK_LIST.add(new BuiltInPack(packResource, displayName, supplier, type));
    }

    public static List<String> getCustomIDs() {
        return PACK_LIST.stream().map(pack -> pack.packResource().packId()).filter(id -> !id.equals(Constants.CRISTEL_LIB_PACK_ID.toString())).toList();
    }

    private static final List<BuiltInPack> PACK_LIST = new ArrayList<>();

    public static void getPacks(Consumer<Pack> consumer, PackType type) {
        if (!frozen) throw new RuntimeException(getWithPrefix("Tried to load Packs before the Registry phase is over!"));
        if (PACK_LIST.isEmpty()) return;
        BuiltInPackConfig config = ConfigRegistry.get(BuiltInPackConfig.class);

        for (BuiltInPack entry : PACK_LIST) {
            PackResources pack = entry.packResource();

            // Check conditions
            if(!entry.type().equals(type)
                    || pack.getNamespaces(type).isEmpty())
                continue;

            if (!entry.supplier().get() ||
                    config.disabledPacks().contains(pack.packId()))
                continue;

            Pack profile = buildPack(entry, type);

            if (profile == null) continue;
            consumer.accept(profile);
        }
    }

    /**
     * Registers one {@link RepositorySource} per qualifying pack so that NeoForge's
     * per-source alphabetical sorting (via TreeMap in {@code PackRepository.discoverAvailable})
     * does not reorder packs relative to their {@link #PACK_LIST} insertion order.
     * Each single-pack source has only one entry in NeoForge's TreeMap, so sorting is a no-op,
     * and the outer LinkedHashMap preserves source-registration order = PACK_LIST order.
     */
    public static void registerEachPackAsSource(PackType type, Consumer<RepositorySource> sourceRegistrar) {
        if (!frozen) throw new RuntimeException(getWithPrefix("Tried to load Packs before the Registry phase is over!"));
        if (PACK_LIST.isEmpty()) return;

        for (BuiltInPack entry : PACK_LIST) {
            PackResources pack = entry.packResource();
            if (!entry.type().equals(type) || pack.getNamespaces(type).isEmpty()) continue;

            sourceRegistrar.accept(consumer -> {
                BuiltInPackConfig config = ConfigRegistry.get(BuiltInPackConfig.class);
                if (!entry.supplier().get() || config.disabledPacks().contains(pack.packId())) return;
                Pack profile = buildPack(entry, type);
                if (profile == null) return;
                consumer.accept(profile);
            });
        }
    }

    @Nullable
    private static Pack buildPack(BuiltInPack entry, PackType type) {
        PackResources pack = entry.packResource();
        Component displayName = entry.displayName();

        PackLocationInfo metadata = new PackLocationInfo(
                pack.packId(),
                displayName,
                new BuiltinResourcePackSource(),
                pack.knownPackInfo()
        );
        PackSelectionConfig selectionConfig = new PackSelectionConfig(
                true,
                Pack.Position.TOP,
                false
        );

        Pack profile = Pack.readMetaAndCreate(metadata, new Pack.ResourcesSupplier() {
            @Override
            public @NotNull PackResources openPrimary(PackLocationInfo var1) {
                return pack;
            }

            @Override
            public PackResources openFull(PackLocationInfo packLocationInfo, Pack.Metadata metadata) {
                if (metadata.overlays().isEmpty()) {
                    return pack;
                }

                List<PackResources> overlays = new ArrayList<>(metadata.overlays().size());

                for (String overlay : metadata.overlays()) {
                    PackResources overlayPack = pack instanceof OverlayPack packWithOverlays ?
                            packWithOverlays.createOverlay(overlay)
                            : CristelLibExpectPlatform.createOverlay(pack, overlay);
                    if (overlayPack != null)
                        overlays.add(overlayPack);
                }

                return new CompositePackResources(pack, overlays);
            }
        }, type, selectionConfig);

        if (profile == null) {
            Constants.LOG.error("Pack Profile with display name: {} is null", displayName);
        }
        return profile;
    }

    private static boolean frozen = false;

    public static void freeze() {
        frozen = true;
    }
}
