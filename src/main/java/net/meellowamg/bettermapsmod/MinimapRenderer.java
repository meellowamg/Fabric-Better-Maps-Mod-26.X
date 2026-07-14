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
        float scale       = config.minimapScale;
        int   MARGIN      = config.minimapMargin;
        int   alpha       = (int)(config.minimapOpacity * 255);
        int   texColor    = (alpha << 24) | 0xFFFFFF;
        int   thickness   = Math.max(1, config.borderThickness);
        int   outerColor  = applyAlpha(config.borderOuterColor, alpha);
        int   innerColor  = applyAlpha(config.borderInnerColor, alpha);
        float mScale      = config.markerScale;

        Minecraft client = Minecraft.getInstance();
        int screenWidth  = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int total     = BASE_MAP + thickness * 2;
        int totalSize = Math.round(total * scale);

        // Gather text
        String coordText = null;
        String biomeText = null;

        if (client.player != null) {
            if (config.showCoordinates) {
                coordText = "X: " + client.player.getBlockX()
                        + "  Y: " + client.player.getBlockY()
                        + "  Z: " + client.player.getBlockZ();
            }
            if (config.showBiome && client.level != null) {
                BlockPos pos = client.player.blockPosition();
                Holder<Biome> biomeHolder = client.level.getBiome(pos);
                biomeText = biomeHolder.unwrapKey()
                        .map(k -> {
                            String fullName = k.toString();
                            // Extract path after last '/' and before ']'
                            // e.g. "ResourceKey[minecraft:worldgen/biome / minecraft:cherry_grove]"
                            int lastSlash   = fullName.lastIndexOf('/');
                            int lastBracket = fullName.lastIndexOf(']');
                            String path = (lastSlash >= 0 && lastBracket > lastSlash)
                                    ? fullName.substring(lastSlash + 1, lastBracket).trim()
                                    : fullName;
                            // Strip namespace prefix like "minecraft:"
                            int colon = path.indexOf(':');
                            if (colon >= 0) path = path.substring(colon + 1);
                            // Capitalize words
                            String[] words = path.split("_");
                            StringBuilder sb = new StringBuilder();
                            for (String word : words) {
                                if (!word.isEmpty()) {
                                    sb.append(Character.toUpperCase(word.charAt(0)))
                                            .append(word.substring(1)).append(" ");
                                }
                            }
                            return sb.toString().trim();
                        })
                        .orElse("Unknown");
            }
        }

        // Minimap corner position
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

        // Render minimap
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().scale(scale, scale);

        // Outer border line
        context.fill(0, 0, total, total, outerColor);
        // Inner border fill
        context.fill(1, 1, total - 1, total - 1, innerColor);
        // Cut out map area background (draw map on top)
        context.fill(thickness, thickness, thickness + BASE_MAP, thickness + BASE_MAP, 0xFF000000);

        // Map texture
        context.blit(RenderPipelines.GUI_TEXTURED, BetterMapsModClient.currentMapTexture,
                thickness, thickness,
                0, 0, BASE_MAP, BASE_MAP,
                MAP_TEX_SIZE, MAP_TEX_SIZE, texColor);

        // Player marker — single icon, properly scaled
        if (config.showMarker && smoothMarkerX >= 0 && smoothMarkerY >= 0) {
            float clampedX  = Math.max(0.02f, Math.min(0.98f, smoothMarkerX));
            float clampedY  = Math.max(0.02f, Math.min(0.98f, smoothMarkerY));
            float iconSize  = BASE_ICON * mScale;
            float centerX   = thickness + clampedX * BASE_MAP;
            float centerY   = thickness + clampedY * BASE_MAP;

            Identifier icon = markerOffMap ? PLAYER_OFF_MAP : PLAYER_ICON;

            context.pose().pushMatrix();
            context.pose().translate(centerX, centerY);
            if (!markerOffMap) {
                context.pose().rotate((float)((smoothMarkerRot + 180.0) * Math.PI / 180.0));
            }
            // Blit centered around 0,0 after translate
            context.blit(RenderPipelines.GUI_TEXTURED, icon,
                    (int)(-iconSize / 2f), (int)(-iconSize / 2f),
                    0, 0,
                    (int) iconSize, (int) iconSize,
                    ICON_TEX_SIZE, ICON_TEX_SIZE);
            context.pose().popMatrix();
        }

        context.pose().popMatrix();

        // Text below minimap — positioned to always stay aligned with minimap
        // and scaled with minimap scale so it stays below regardless of position
        int textX     = x;
        int textY     = y + totalSize + 3;
        int textColor = applyAlpha(0xFFFFFFFF, alpha);
        int shadowCol = applyAlpha(0xFF303030, alpha);

        // For bottom positions, put text ABOVE the minimap instead
        boolean isBottom = config.minimapPosition.startsWith("BOTTOM");
        if (isBottom) {
            int lineCount = (coordText != null ? 1 : 0) + (biomeText != null ? 1 : 0);
            textY = y - (lineCount * 10) - 3;
        }

        if (coordText != null) {
            drawTextWithShadow(context, client, coordText, textX, textY, textColor, shadowCol);
            textY += 10;
        }
        if (biomeText != null) {
            drawTextWithShadow(context, client, biomeText, textX, textY, textColor, shadowCol);
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

    public static void reset() {
        targetMarkerX   = -1;
        targetMarkerY   = -1;
        smoothMarkerX   = -1;
        smoothMarkerY   = -1;
        smoothMarkerRot = 0;
        markerOffMap    = false;
    }
}