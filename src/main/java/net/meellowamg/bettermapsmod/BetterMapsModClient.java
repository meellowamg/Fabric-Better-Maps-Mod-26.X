package net.meellowamg.bettermapsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

        // Update position every tick regardless of map center
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!minimapEnabled) return;
            if (currentMapTexture == null) return;
            if (client.player == null || client.level == null) return;
            if (currentMapData == null) return;

            int scale = 1 << currentMapData.scale;
            float halfBlocks = 64.0f * scale;
            float px = (client.player.getBlockX() - currentMapData.centerX + halfBlocks) / (halfBlocks * 2.0f);
            float pz = (client.player.getBlockZ() - currentMapData.centerZ + halfBlocks) / (halfBlocks * 2.0f);

            // Always update position from player coords
            MinimapRenderer.targetMarkerX = px;
            MinimapRenderer.targetMarkerY = pz;
            MinimapRenderer.markerOffMap = px < 0 || px > 1 || pz < 0 || pz > 1;

            // Rotation from player yaw
            float yaw = client.player.getYRot() % 360;
            if (yaw < 0) yaw += 360;
            MinimapRenderer.markerRot = (byte) Math.round(yaw / 360.0f * 16.0f);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            minimapEnabled = false;
            currentMapTexture = null;
            currentMapData = null;
            MinimapRenderer.reset();
        });
    }
}