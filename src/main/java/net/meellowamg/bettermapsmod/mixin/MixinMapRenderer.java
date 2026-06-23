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
            // Not locked - update texture reference
            BetterMapsModClient.currentMapTexture = mapRenderState.texture;
            // Also try to grab map data now
            tryUpdateMapData(mapRenderState);
        } else {
            if (mapRenderState.texture.equals(BetterMapsModClient.currentMapTexture)) {
                tryUpdateMapData(mapRenderState);
            }
        }
    }

    private void tryUpdateMapData(MapRenderState mapRenderState) {
        if (mapRenderState.texture == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        String path = mapRenderState.texture.getPath();
        if (!path.startsWith("map/")) return;

        try {
            int mapId = Integer.parseInt(path.substring(4));
            MapItemSavedData data = mc.level.getMapData(new MapId(mapId));
            if (data != null) {
                BetterMapsModClient.currentMapData = data;
            }
        } catch (NumberFormatException ignored) {}
    }
}