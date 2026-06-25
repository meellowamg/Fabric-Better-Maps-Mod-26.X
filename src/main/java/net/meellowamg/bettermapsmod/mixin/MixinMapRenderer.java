package net.meellowamg.bettermapsmod.mixin;

import net.meellowamg.bettermapsmod.BetterMapsModClient;
import net.meellowamg.bettermapsmod.MinimapRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapRenderer.class)
public class MixinMapRenderer {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(MapRenderState mapRenderState, PoseStack poseStack,
                          SubmitNodeCollector submitNodeCollector, boolean showOnlyFrame,
                          int lightCoords, CallbackInfo ci) {
        if (mapRenderState.texture == null) return;

        // Always capture the texture, even before minimap is enabled
        if (!BetterMapsModClient.minimapEnabled) {
            BetterMapsModClient.currentMapTexture = mapRenderState.texture;
        }

        if (!mapRenderState.texture.equals(BetterMapsModClient.currentMapTexture)) return;

        // Try to get map data (for future use when maps have real centers)
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && BetterMapsModClient.currentMapData == null) {
            String path = mapRenderState.texture.getPath();
            if (path.startsWith("map/")) {
                try {
                    int mapId = Integer.parseInt(path.substring(4));
                    MapItemSavedData data = mc.level.getMapData(new MapId(mapId));
                    if (data != null) {
                        BetterMapsModClient.currentMapData = data;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        // Use decoration data for marker position and rotation
        // This is the only reliable source - decoration.x/y are updated by vanilla
        // every frame based on real player position relative to map center
        for (MapRenderState.MapDecorationRenderState decoration : mapRenderState.decorations) {
            if (decoration.atlasSprite != null) {
                // decoration.x and .y are signed bytes: -128 to 127
                // Map these to 0..1 range: -128 -> 0, 127 -> ~1
                float dx = (decoration.x + 128f) / 256f;
                float dy = (decoration.y + 128f) / 256f;

                MinimapRenderer.targetMarkerX = dx;
                MinimapRenderer.targetMarkerY = dy;
                MinimapRenderer.markerRot = decoration.rot;

                // Since bytes are clamped to -128..127 by vanilla when off-map,
                // we check if the player decoration type indicates off-map.
                // Vanilla uses decoration type 0 (player arrow) when on-map,
                // and type 1 (player off-map dot) when off-map.
                // We detect this via the sprite name if possible.
                String spriteName = decoration.atlasSprite.contents().name().getPath();
                MinimapRenderer.markerOffMap = spriteName.contains("player_off_map")
                        || spriteName.contains("off_map");
                break;
            }
        }
    }
}