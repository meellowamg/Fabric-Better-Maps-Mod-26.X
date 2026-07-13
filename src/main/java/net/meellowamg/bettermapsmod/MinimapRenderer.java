package net.meellowamg.bettermapsmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

public class MinimapRenderer {

    private static final Identifier PLAYER_ICON    = Identifier.fromNamespaceAndPath("bettermapsmod", "textures/player.png");
    private static final Identifier PLAYER_OFF_MAP = Identifier.fromNamespaceAndPath("bettermapsmod", "textures/player_off_map.png");

    private static final int MAP_TEX_SIZE  = 128;
    private static final int ICON_TEX_SIZE = 8;
    private static final int BASE_MAP      = 128;
    private static final int BASE_ICON     = 8;

    private static final float SMOOTH_SPEED = 0.15f;
    private static final float ROT_SMOOTH   = 0.3f;

    public static float   targetMarkerX   = -1;
    public static float   targetMarkerY   = -1;
    private static float  smoothMarkerX   = -1;
    private static float  smoothMarkerY   = -1;
    public static byte    markerRot       = 0;
    private static float  smoothMarkerRot = 0;
    public static boolean markerOffMap    = false;

    public static void render(Object contextObj) {
        if (!BetterMapsModClient.minimapEnabled) return;
        if (BetterMapsModClient.currentMapTexture == null) return;
        if (!(contextObj instanceof GuiGraphicsExtractor context)) return;

        BetterMapsConfig config = BetterMapsConfig.get();
        float scale     = config.minimapScale;
        int   MARGIN    = config.minimapMargin;
        int   alpha     = (int)(config.minimapOpacity * 255);
        int   texColor  = (alpha << 24) | 0xFFFFFF;
        int   thickness = Math.max(1, config.borderThickness);
        int   bColor    = applyAlpha(config.borderColor, alpha);
        float mScale    = config.markerScale;

        Minecraft client = Minecraft.getInstance();
        int screenWidth  = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int total     = BASE_MAP + thickness * 2;
        int totalSize = Math.round(total * scale);

        // Gather text lines
        String coordText = null;
        String biomeText = null;
        String scaleText = null;

        if (client.player != null) {
            if (config.showCoordinates) {
                int px = client.player.getBlockX();
                int py = client.player.getBlockY();
                int pz = client.player.getBlockZ();
                coordText = "X: " + px + "  Y: " + py + "  Z: " + pz;
            }
            if (config.showBiome && client.level != null) {
                BlockPos pos = client.player.blockPosition();
                Holder<Biome> biomeHolder = client.level.getBiome(pos);
                String biomeName = biomeHolder.unwrapKey()
                        .map(k -> {
                            String fullName = k.toString();
                            int lastSlash = fullName.lastIndexOf('/');
                            int lastBracket = fullName.lastIndexOf(']');
                            String path = (lastSlash >= 0 && lastBracket > lastSlash)
                                    ? fullName.substring(lastSlash + 1, lastBracket).trim()
                                    : "Unknown";
                            String[] words = path.split("_");
                            StringBuilder sb = new StringBuilder();
                            for (String w : words) {
                                if (!w.isEmpty()) {
                                    sb.append(Character.toUpperCase(w.charAt(0)))
                                            .append(w.substring(1)).append(" ");
                                }
                            }
                            return sb.toString().trim();
                        })
                        .orElse("Unknown");
                biomeText = biomeName;
            }
            if (config.showMapScale && BetterMapsModClient.currentMapData != null) {
                int mapScale = 1 << BetterMapsModClient.currentMapData.scale;
                scaleText = "Scale 1:" + mapScale;
            }
        }

        // Position of minimap corner
        int x, y;
        switch (config.minimapPosition) {
            case "TOP_LEFT"     -> { x = MARGIN;                            y = MARGIN; }
            case "BOTTOM_RIGHT" -> { x = screenWidth  - totalSize - MARGIN; y = screenHeight - totalSize - MARGIN; }
            case "BOTTOM_LEFT"  -> { x = MARGIN;                            y = screenHeight - totalSize - MARGIN; }
            default             -> { x = screenWidth  - totalSize - MARGIN; y = MARGIN; }
        }

        // Smooth position
        if (targetMarkerX >= 0 && targetMarkerY >= 0) {
            if (smoothMarkerX < 0) {
                smoothMarkerX = targetMarkerX;
                smoothMarkerY = targetMarkerY;
            } else {
                float distSq = (targetMarkerX - smoothMarkerX) * (targetMarkerX - smoothMarkerX)
                        + (targetMarkerY - smoothMarkerY) * (targetMarkerY - smoothMarkerY);
                if (distSq > 0.25f) {
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
        float rotDiff   = targetRot - smoothMarkerRot;
        while (rotDiff >  180) rotDiff -= 360;
        while (rotDiff < -180) rotDiff += 360;
        smoothMarkerRot += rotDiff * ROT_SMOOTH;

        // Scale everything from corner
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);

        // Border layers
        for (int i = 0; i < thickness; i++) {
            float t = thickness <= 1 ? 1f : (float) i / (thickness - 1);
            int layerColor = lerpColor(bColor, lighten(bColor, 0.3f), t);
            context.fill(i, i, total - i, total - i, layerColor);
        }

        // Map texture
        context.blit(RenderPipelines.GUI_TEXTURED, BetterMapsModClient.currentMapTexture,
                thickness, thickness,
                0, 0, BASE_MAP, BASE_MAP,
                MAP_TEX_SIZE, MAP_TEX_SIZE, texColor);

        // Player marker
        if (config.showMarker && smoothMarkerX >= 0 && smoothMarkerY >= 0) {
            float clampedX = Math.max(0.02f, Math.min(0.98f, smoothMarkerX));
            float clampedY = Math.max(0.02f, Math.min(0.98f, smoothMarkerY));

            float iconSize = BASE_ICON * mScale;
            float mx = thickness + clampedX * BASE_MAP - iconSize / 2f;
            float my = thickness + clampedY * BASE_MAP - iconSize / 2f;

            Identifier icon = markerOffMap ? PLAYER_OFF_MAP : PLAYER_ICON;

            context.pose().pushMatrix();
            float cx = mx + iconSize / 2f;
            float cy = my + iconSize / 2f;
            context.pose().translate(cx, cy);
            if (!markerOffMap) {
                context.pose().rotate((float)((smoothMarkerRot + 180.0) * Math.PI / 180.0));
            }
            context.pose().translate(-iconSize / 2f, -iconSize / 2f);
            context.blit(RenderPipelines.GUI_TEXTURED, icon,
                    0, 0, 0, 0,
                    (int) iconSize, (int) iconSize,
                    ICON_TEX_SIZE, ICON_TEX_SIZE);
            context.pose().popMatrix();
        }

        context.pose().popMatrix();

        // Draw text below minimap outside scaled matrix so font stays crisp
        int textX     = x;
        int textY     = y + totalSize + 3;
        int textColor = applyAlpha(0xFFFFFFFF, alpha);
        int shadowCol = applyAlpha(0xFF000000, alpha);

        if (coordText != null) {
            drawTextWithShadow(context, client, coordText, textX, textY, textColor, shadowCol);
            textY += 10;
        }
        if (biomeText != null) {
            drawTextWithShadow(context, client, biomeText, textX, textY, textColor, shadowCol);
            textY += 10;
        }
        if (scaleText != null) {
            drawTextWithShadow(context, client, scaleText, textX, textY, textColor, shadowCol);
        }
    }

    private static void drawTextWithShadow(GuiGraphicsExtractor context, Minecraft client,
                                           String text, int x, int y, int color, int shadow) {
        context.text(client.font, text, x + 1, y + 1, shadow, false);
        context.text(client.font, text, x, y, color, false);
    }

    private static int applyAlpha(int argbColor, int alpha) {
        return (argbColor & 0x00FFFFFF) | (alpha << 24);
    }

    private static int lighten(int argb, float amount) {
        int a = (argb >> 24) & 0xFF;
        int r = Math.min(255, (int)(((argb >> 16) & 0xFF) + 255 * amount));
        int g = Math.min(255, (int)(((argb >>  8) & 0xFF) + 255 * amount));
        int b = Math.min(255, (int)(((argb)       & 0xFF) + 255 * amount));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpColor(int a, int b, float t) {
        int aa = (a >> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        return ((int)(aa + (ba - aa) * t) << 24) | ((int)(ar + (br - ar) * t) << 16)
                | ((int)(ag + (bg - ag) * t) << 8)  |  (int)(ab + (bb - ab) * t);
    }

    public static void reset() {
        targetMarkerX   = -1;
        targetMarkerY   = -1;
        smoothMarkerX   = -1;
        smoothMarkerY   = -1;
        smoothMarkerRot = 0;
        markerOffMap    = false;
    }
}