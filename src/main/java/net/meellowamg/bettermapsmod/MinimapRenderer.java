package net.meellowamg.bettermapsmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class MinimapRenderer {

    private static final Identifier MAP_BACKGROUND = Identifier.fromNamespaceAndPath("bettermapsmod", "textures/map_background.png");
    private static final Identifier PLAYER_ICON = Identifier.fromNamespaceAndPath("bettermapsmod", "textures/player.png");
    private static final Identifier PLAYER_OFF_MAP = Identifier.fromNamespaceAndPath("bettermapsmod", "textures/player_off_map.png");

    // Base sizes at scale 1.0 — everything is relative to these
    // The vanilla map item render is 128x128 map inside a frame
    // We use a border of 8px at scale 1.0 on each side
    private static final int BASE_MAP_SIZE = 128;  // map texture pixels shown
    private static final int BASE_BORDER = 8;       // border pixels at scale 1.0
    private static final int BASE_ICON_SIZE = 8;    // player icon pixels at scale 1.0

    // Background texture and map texture native sizes
    private static final int BG_TEX_SIZE = 256;
    private static final int MAP_TEX_SIZE = 128;
    private static final int ICON_TEX_SIZE = 8;

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

        BetterMapsConfig config = BetterMapsConfig.get();
        float scale = config.minimapScale;
        int MARGIN = config.minimapMargin;
        int alpha = (int)(config.minimapOpacity * 255);
        int color = (alpha << 24) | 0xFFFFFF;

        // All sizes scale together uniformly
        int border = Math.round(BASE_BORDER * scale);
        int mapSize = Math.round(BASE_MAP_SIZE * scale);
        int iconSize = Math.round(BASE_ICON_SIZE * scale);
        int totalSize = mapSize + border * 2;

        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        // Position of the top-left corner of the whole minimap (border + map)
        int x, y;
        switch (config.minimapPosition) {
            case "TOP_LEFT"     -> { x = MARGIN; y = MARGIN; }
            case "BOTTOM_RIGHT" -> { x = screenWidth - totalSize - MARGIN; y = screenHeight - totalSize - MARGIN; }
            case "BOTTOM_LEFT"  -> { x = MARGIN; y = screenHeight - totalSize - MARGIN; }
            default             -> { x = screenWidth - totalSize - MARGIN; y = MARGIN; }
        }

        int mapX = x + border;
        int mapY = y + border;

        // Smooth position
        if (targetMarkerX >= 0 && targetMarkerY >= 0) {
            if (smoothMarkerX < 0) {
                smoothMarkerX = targetMarkerX;
                smoothMarkerY = targetMarkerY;
            } else {
                float distSq = (targetMarkerX - smoothMarkerX) * (targetMarkerX - smoothMarkerX)
                        + (targetMarkerY - smoothMarkerY) * (targetMarkerY - smoothMarkerY);
                if (distSq > 0.1f) {
                    smoothMarkerX = targetMarkerX;
                    smoothMarkerY = targetMarkerY;
                } else {
                    smoothMarkerX += (targetMarkerX - smoothMarkerX) * SMOOTH_SPEED;
                    smoothMarkerY += (targetMarkerY - smoothMarkerY) * SMOOTH_SPEED;
                }
            }
        }

        // Smooth rotation
        float targetRot = (markerRot & 0xFF) * 360.0f / 16.0f;
        float rotDiff = targetRot - smoothMarkerRot;
        while (rotDiff > 180) rotDiff -= 360;
        while (rotDiff < -180) rotDiff += 360;
        smoothMarkerRot += rotDiff * ROT_SMOOTH;

        // Draw background border — sample the full 256x256 background texture
        // scaled to totalSize x totalSize on screen
        context.blit(RenderPipelines.GUI_TEXTURED, MAP_BACKGROUND,
                x, y, 0, 0, totalSize, totalSize, BG_TEX_SIZE, BG_TEX_SIZE, color);

        // Draw map — sample all 128x128 map pixels, scaled to mapSize x mapSize on screen
        context.blit(RenderPipelines.GUI_TEXTURED, BetterMapsModClient.currentMapTexture,
                mapX, mapY, 0, 0, mapSize, mapSize, MAP_TEX_SIZE, MAP_TEX_SIZE, color);

        if (smoothMarkerX < 0 || smoothMarkerY < 0) return;

        float clampedX = Math.max(0.02f, Math.min(0.98f, smoothMarkerX));
        float clampedY = Math.max(0.02f, Math.min(0.98f, smoothMarkerY));

        int mx = mapX + (int)(clampedX * mapSize) - iconSize / 2;
        int my = mapY + (int)(clampedY * mapSize) - iconSize / 2;

        Identifier icon = markerOffMap ? PLAYER_OFF_MAP : PLAYER_ICON;

        context.pose().pushMatrix();
        float cx = mx + iconSize / 2f;
        float cy = my + iconSize / 2f;
        context.pose().translate(cx, cy);
        if (!markerOffMap) {
            context.pose().rotate((float)((smoothMarkerRot + 180.0) * Math.PI / 180.0));
        }
        context.pose().translate(-cx, -cy);
        context.blit(RenderPipelines.GUI_TEXTURED, icon,
                mx, my, 0, 0, iconSize, iconSize, ICON_TEX_SIZE, ICON_TEX_SIZE);
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