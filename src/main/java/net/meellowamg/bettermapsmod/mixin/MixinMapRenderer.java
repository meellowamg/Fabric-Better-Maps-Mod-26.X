package net.meellowamg.bettermapsmod.mixin;

import net.meellowamg.bettermapsmod.BetterMapsMod;
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

        if (!BetterMapsModClient.minimapEnabled) {
            BetterMapsModClient.currentMapTexture = mapRenderState.texture;
            updateMarkerFromDecorations(mapRenderState);
        } else {
            if (mapRenderState.texture.equals(BetterMapsModClient.currentMapTexture)) {
                updateMarkerFromDecorations(mapRenderState);
            }
        }
    }

    private void updateMarkerFromDecorations(MapRenderState mapRenderState) {
        for (MapRenderState.MapDecorationRenderState decoration : mapRenderState.decorations) {
            if (decoration.atlasSprite != null) {
                MinimapRenderer.targetMarkerX = (decoration.x + 128f) / 256f;
                MinimapRenderer.targetMarkerY = (decoration.y + 128f) / 256f;
                MinimapRenderer.markerRot = decoration.rot;
                MinimapRenderer.markerOffMap = decoration.renderOnFrame;
                return;
            }
        }
    }
}