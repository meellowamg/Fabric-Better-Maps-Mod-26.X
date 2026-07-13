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

        if (!BetterMapsModClient.minimapEnabled) {
            // Texture changed - different map being viewed, reset
            if (BetterMapsModClient.currentMapTexture != null
                    && !BetterMapsModClient.currentMapTexture.equals(mapRenderState.texture)) {
                BetterMapsModClient.currentMapData = null;
                BetterMapsModClient.currentMapId = -1;
                MinimapRenderer.reset();
            }
            BetterMapsModClient.currentMapTexture = mapRenderState.texture;

            // Parse and store the map ID from the texture path
            String path = mapRenderState.texture.getPath();
            if (path.startsWith("map/")) {
                try {
                    BetterMapsModClient.currentMapId = Integer.parseInt(path.substring(4));
                } catch (NumberFormatException ignored) {}
            }
        }

        if (!mapRenderState.texture.equals(BetterMapsModClient.currentMapTexture)) return;

        if (BetterMapsModClient.currentMapData == null && BetterMapsModClient.currentMapId >= 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                MapItemSavedData data = mc.level.getMapData(new MapId(BetterMapsModClient.currentMapId));
                if (data != null) {
                    BetterMapsModClient.currentMapData = data;
                }
            }
        }
    }
}