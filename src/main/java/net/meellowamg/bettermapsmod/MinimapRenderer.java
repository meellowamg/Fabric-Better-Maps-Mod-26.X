package net.meellowamg.bettermapsmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.resources.Identifier;

public class MinimapRenderer {

    private static final Identifier MAP_BACKGROUND = Identifier.fromNamespaceAndPath("bettermapsmod", "textures/map_background.png");
    private static final int SIZE = 90;
    private static final int BORDER = 6;
    private static final int MARGIN = 10;

    public static float lockedMarkerX = -1;
    public static float lockedMarkerY = -1;

    private static int debugTimer = 0;

    public static void render(Object contextObj) {
        if (!BetterMapsModClient.minimapEnabled) return;
        if (BetterMapsModClient.currentMapTexture == null) return;
        if (!(contextObj instanceof GuiGraphicsExtractor context)) return;

        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();

        int x = screenWidth - SIZE - MARGIN;
        int y = MARGIN;
        int mapX = x + BORDER;
        int mapY = y + BORDER;
        int mapSize = SIZE - BORDER * 2;

        // Draw border
        context.blit(
                MAP_BACKGROUND,
                x, y,
                x + SIZE, y + SIZE,
                0.0f, 1.0f,
                0.0f, 1.0f
        );

        // Draw map texture
        context.blit(
                BetterMapsModClient.currentMapTexture,
                mapX, mapY,
                mapX + mapSize, mapY + mapSize,
                0.0f, 1.0f,
                0.0f, 1.0f
        );

        // Debug every 60 frames
        debugTimer++;
        if (debugTimer > 60) {
            debugTimer = 0;
            BetterMapsMod.LOGGER.info("mapData: " + BetterMapsModClient.currentMapData);
            BetterMapsMod.LOGGER.info("player: " + (client.player != null ? client.player.getBlockX() + "," + client.player.getBlockZ() : "null"));
        }

        // Calculate player position every frame
        MapItemSavedData mapData = BetterMapsModClient.currentMapData;
        if (mapData != null && client.player != null) {
            float mapCenterX = mapData.centerX;
            float mapCenterZ = mapData.centerZ;
            int scale = 1 << mapData.scale;

            float playerMapX = (client.player.getBlockX() - mapCenterX) / (scale * 128.0f) + 0.5f;
            float playerMapZ = (client.player.getBlockZ() - mapCenterZ) / (scale * 128.0f) + 0.5f;

            BetterMapsMod.LOGGER.info("playerMapX: " + playerMapX + " playerMapZ: " + playerMapZ);

            int mx = mapX + (int)(playerMapX * mapSize);
            int my = mapY + (int)(playerMapZ * mapSize);

            BetterMapsMod.LOGGER.info("dot at: " + mx + "," + my + " bounds: " + mapX + "-" + (mapX+mapSize) + "," + mapY + "-" + (mapY+mapSize));

            context.fill(mx - 3, my - 3, mx + 3, my + 3, 0xFF000000);
            context.fill(mx - 2, my - 2, mx + 2, my + 2, 0xFFFFFFFF);
        }
    }
}