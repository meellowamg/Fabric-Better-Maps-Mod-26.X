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
    public static int currentMapId = -1;

    @Override
    public void onInitializeClient() {
        BetterMapsMod.LOGGER.info("Better Maps Mod Client Loaded!");

        BetterMapsConfig.load();
        BetterMapsKeyBindings.register();

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("bettermapsmod", "minimap"),
                (context, tickCounter) -> MinimapRenderer.render(context)
        );

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            minimapEnabled = false;
            currentMapTexture = null;
            currentMapData = null;
            currentMapId = -1;
            MinimapRenderer.reset();
        });
    }
}