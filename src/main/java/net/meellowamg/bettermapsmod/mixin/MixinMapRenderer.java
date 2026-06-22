package net.meellowamg.bettermapsmod.mixin;

import net.meellowamg.bettermapsmod.BetterMapsModClient;
import net.meellowamg.bettermapsmod.MinimapRenderer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.MapRenderState;
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
        if (!BetterMapsModClient.minimapEnabled) {
            if (mapRenderState.texture != null) {
                BetterMapsModClient.currentMapTexture = mapRenderState.texture;
                updateMarker(mapRenderState);
            }
        } else {
            if (mapRenderState.texture != null &&
                    mapRenderState.texture.equals(BetterMapsModClient.currentMapTexture)) {
                updateMarker(mapRenderState);
            }
        }
    }

    private void updateMarker(MapRenderState mapRenderState) {
        for (MapRenderState.MapDecorationRenderState decoration : mapRenderState.decorations) {
            if (decoration.atlasSprite != null) {
                MinimapRenderer.lockedMarkerX = (decoration.x + 128) / 256.0f;
                MinimapRenderer.lockedMarkerY = (decoration.y + 128) / 256.0f;
                break;
            }
        }
    }
}