package net.meellowamg.bettermapsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class BetterMapsModClient implements ClientModInitializer {

    public static boolean minimapEnabled = false;
    public static Identifier currentMapTexture = null;
    public static MapItemSavedData currentMapData = null;

    @Override
    public void onInitializeClient() {
        BetterMapsMod.LOGGER.info("Better Maps Mod Client Loaded!");

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("bettermapsmod", "minimap"),
                (context, tickCounter) -> MinimapRenderer.render(context)
        );

        // No tick event needed - decoration data from MixinMapRenderer
        // handles position updates every frame while holding the map.
        // When not holding the map, the minimap freezes at last known position
        // which is correct vanilla behavior.

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            minimapEnabled = false;
            currentMapTexture = null;
            currentMapData = null;
            MinimapRenderer.reset();
        });
    }
}