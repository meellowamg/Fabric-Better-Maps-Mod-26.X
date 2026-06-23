package net.meellowamg.bettermapsmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.maps.MapId;
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

        // Feature 3: Update player position every tick even without holding map
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!minimapEnabled) return;
            if (currentMapTexture == null) return;
            if (client.player == null || client.level == null) return;

            // Try to get map data from texture path if we don't have it
            if (currentMapData == null && currentMapTexture != null) {
                String path = currentMapTexture.getPath();
                if (path.startsWith("map/")) {
                    try {
                        int mapId = Integer.parseInt(path.substring(4));
                        MapItemSavedData data = client.level.getMapData(new MapId(mapId));
                        if (data != null) {
                            currentMapData = data;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            // Update marker position from player coords every tick
            if (currentMapData != null) {
                int scale = 1 << currentMapData.scale;
                float halfBlocks = 64.0f * scale;
                float px = (client.player.getBlockX() - currentMapData.centerX + halfBlocks) / (halfBlocks * 2.0f);
                float pz = (client.player.getBlockZ() - currentMapData.centerZ + halfBlocks) / (halfBlocks * 2.0f);
                MinimapRenderer.targetMarkerX = px;
                MinimapRenderer.targetMarkerY = pz;
                // Update off-map status
                MinimapRenderer.markerOffMap = px < 0 || px > 1 || pz < 0 || pz > 1;

                // Update rotation from player yaw
                float yaw = client.player.getYRot();
                MinimapRenderer.markerRot = (byte)((yaw / 360.0f * 16.0f) % 16);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            minimapEnabled = false;
            currentMapTexture = null;
            currentMapData = null;
            MinimapRenderer.reset();
        });
    }
}