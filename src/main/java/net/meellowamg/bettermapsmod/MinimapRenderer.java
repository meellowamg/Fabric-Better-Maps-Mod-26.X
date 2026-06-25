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
    private static final float SMOOTH_SPEED = 0.15f;
    private static final float ROT_SMOOTH = 0.3f;

    public static float targetMarkerX = -1;
    public static float targetMarkerY = -1;
    private static float smoothMarkerX = -1;
    private static float smoothMarkerY = -1;
    public static byte markerRot = 0;
    private static float smoothMarkerRot = 0;
    public static boolean markerOffMap = false;

    public static void render(Object contextObj) {
        if (!BetterMapsModClient.minimapEnabled) return;
        if (BetterMapsModClient.currentMapTexture == null) return;
        if (!(contextObj instanceof GuiGraphicsExtractor context)) return;

        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();

        // Smooth position - snap if first frame or large jump
        if (targetMarkerX >= 0 && targetMarkerY >= 0) {
            if (smoothMarkerX < 0) {
                // First time - snap immediately
                smoothMarkerX = targetMarkerX;
                smoothMarkerY = targetMarkerY;
            } else {
                float distSq = (targetMarkerX - smoothMarkerX) * (targetMarkerX - smoothMarkerX)
                        + (targetMarkerY - smoothMarkerY) * (targetMarkerY - smoothMarkerY);
                if (distSq > 0.1f) {
                    // Large jump (e.g. teleport) - snap
                    smoothMarkerX = targetMarkerX;
                    smoothMarkerY = targetMarkerY;
                } else {
                    smoothMarkerX += (targetMarkerX - smoothMarkerX) * SMOOTH_SPEED;
                    smoothMarkerY += (targetMarkerY - smoothMarkerY) * SMOOTH_SPEED;
                }
            }
        }

        // Smooth rotation with shortest-path wraparound
        float targetRot = (markerRot & 0xFF) * 360.0f / 16.0f;
        float rotDiff = targetRot - smoothMarkerRot;
        while (rotDiff > 180) rotDiff -= 360;
        while (rotDiff < -180) rotDiff += 360;
        smoothMarkerRot += rotDiff * ROT_SMOOTH;

        int x = screenWidth - SIZE - MARGIN;
        int y = MARGIN;
        int mapX = x + BORDER;
        int mapY = y + BORDER;
        int mapSize = SIZE - BORDER * 2;

        // Draw border then map
        context.blit(MAP_BACKGROUND, x, y, x + SIZE, y + SIZE, 0.0f, 1.0f, 0.0f, 1.0f);
        context.blit(BetterMapsModClient.currentMapTexture, mapX, mapY,
                mapX + mapSize, mapY + mapSize, 0.0f, 1.0f, 0.0f, 1.0f);

        if (smoothMarkerX < 0 || smoothMarkerY < 0) return;

        // Clamp marker to inside map area
        float clampedX = Math.max(0.02f, Math.min(0.98f, smoothMarkerX));
        float clampedY = Math.max(0.02f, Math.min(0.98f, smoothMarkerY));

        int mx = mapX + (int)(clampedX * mapSize) - ICON_SIZE / 2;
        int my = mapY + (int)(clampedY * mapSize) - ICON_SIZE / 2;

        // Choose icon: arrow when on-map, circle when off-map
        Identifier icon = markerOffMap ? PLAYER_OFF_MAP : PLAYER_ICON;

        context.pose().pushMatrix();
        float cx = mx + ICON_SIZE / 2f;
        float cy = my + ICON_SIZE / 2f;
        context.pose().translate(cx, cy);
        if (!markerOffMap) {
            // Rotate arrow to match player facing direction
            float radians = (float)((smoothMarkerRot + 180.0) * Math.PI / 180.0);
            context.pose().rotate(radians);
        }
        context.pose().translate(-cx, -cy);

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