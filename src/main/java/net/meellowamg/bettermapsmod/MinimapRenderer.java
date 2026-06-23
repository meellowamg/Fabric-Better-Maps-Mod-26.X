package net.meellowamg.bettermapsmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class MinimapRenderer {

    private static final Identifier MAP_BACKGROUND = Identifier.fromNamespaceAndPath("bettermapsmod", "textures/map_background.png");
    private static final Identifier PLAYER_ICON = Identifier.fromNamespaceAndPath("bettermapsmod", "textures/player.png");
    private static final Identifier PLAYER_OFF_MAP = Identifier.fromNamespaceAndPath("bettermapsmod", "textures/player_off_map.png");

    private static final int SIZE = 90;
    private static final int BORDER = 6;
    private static final int MARGIN = 10;
    private static final int ICON_SIZE = 8;

    // Target position set by mixin
    public static float targetMarkerX = -1;
    public static float targetMarkerY = -1;

    // Smoothed position for rendering
    private static float smoothMarkerX = -1;
    private static float smoothMarkerY = -1;

    public static byte markerRot = 0;
    private static float smoothMarkerRot = 0;
    public static boolean markerOffMap = false;

    // Feature 3: smooth interpolation speed
    private static final float SMOOTH_SPEED = 0.15f;

    public static void render(Object contextObj) {
        if (!BetterMapsModClient.minimapEnabled) return;
        if (BetterMapsModClient.currentMapTexture == null) return;
        if (!(contextObj instanceof GuiGraphicsExtractor context)) return;

        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();

        // Feature 3: Update marker from player position every frame
        // This allows the marker to update even without holding the map
        if (BetterMapsModClient.currentMapData != null && client.player != null) {
            int scale = 1 << BetterMapsModClient.currentMapData.scale;
            float halfBlocks = 64.0f * scale;
            float px = (client.player.getBlockX() - BetterMapsModClient.currentMapData.centerX + halfBlocks) / (halfBlocks * 2.0f);
            float pz = (client.player.getBlockZ() - BetterMapsModClient.currentMapData.centerZ + halfBlocks) / (halfBlocks * 2.0f);
            // Only update from player position if we have valid map data with correct center
            if (BetterMapsModClient.currentMapData.centerX != 0 || BetterMapsModClient.currentMapData.centerZ != 0) {
                targetMarkerX = px;
                targetMarkerY = pz;
            }
        }

        // Feature 3: Smooth interpolation of marker position
        if (targetMarkerX >= 0 && targetMarkerY >= 0) {
            if (smoothMarkerX < 0) {
                smoothMarkerX = targetMarkerX;
                smoothMarkerY = targetMarkerY;
            } else {
                smoothMarkerX += (targetMarkerX - smoothMarkerX) * SMOOTH_SPEED;
                smoothMarkerY += (targetMarkerY - smoothMarkerY) * SMOOTH_SPEED;
            }
        }

        // Feature 4: Smooth rotation interpolation
        float targetRot = markerRot * 360.0f / 16.0f;
        float rotDiff = targetRot - smoothMarkerRot;
        // Handle wrap-around
        while (rotDiff > 180) rotDiff -= 360;
        while (rotDiff < -180) rotDiff += 360;
        smoothMarkerRot += rotDiff * 0.2f;

        int x = screenWidth - SIZE - MARGIN;
        int y = MARGIN;
        int mapX = x + BORDER;
        int mapY = y + BORDER;
        int mapSize = SIZE - BORDER * 2;

        // Draw border
        context.blit(MAP_BACKGROUND, x, y, x + SIZE, y + SIZE, 0.0f, 1.0f, 0.0f, 1.0f);

        // Draw map texture
        context.blit(BetterMapsModClient.currentMapTexture, mapX, mapY, mapX + mapSize, mapY + mapSize, 0.0f, 1.0f, 0.0f, 1.0f);

        if (smoothMarkerX < 0 || smoothMarkerY < 0) return;

        float clampedX = Math.max(0, Math.min(1, smoothMarkerX));
        float clampedY = Math.max(0, Math.min(1, smoothMarkerY));

        int mx = mapX + (int)(clampedX * mapSize) - ICON_SIZE / 2;
        int my = mapY + (int)(clampedY * mapSize) - ICON_SIZE / 2;

        // markerOffMap true = off map = show circle
        Identifier icon = markerOffMap ? PLAYER_OFF_MAP : PLAYER_ICON;

        context.pose().pushMatrix();
        float centerX = mx + ICON_SIZE / 2f;
        float centerY = my + ICON_SIZE / 2f;
        context.pose().translate(centerX, centerY);
        if (!markerOffMap) {
            context.pose().rotate((float)((smoothMarkerRot + 180.0) * Math.PI / 180.0));
        }
        context.pose().translate(-centerX, -centerY);

        context.blit(icon, mx, my, mx + ICON_SIZE, my + ICON_SIZE, 0.0f, 1.0f, 0.0f, 1.0f);

        context.pose().popMatrix();
    }

    public static void reset() {
        targetMarkerX = -1;
        targetMarkerY = -1;
        smoothMarkerX = -1;
        smoothMarkerY = -1;
        smoothMarkerRot = 0;
        markerOffMap = false;
    }
}