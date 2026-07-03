package net.meellowamg.bettermapsmod.mixin;

import net.meellowamg.bettermapsmod.BetterMapsModClient;
import net.meellowamg.bettermapsmod.MinimapRenderer;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(MapItemSavedData.class)
public class MixinMapItemSavedData {

    @Shadow
    private Map<String, MapDecoration> decorations;

    @Inject(method = "tickCarriedBy", at = @At("TAIL"))
    private void onTickCarriedBy(Player tickingPlayer, ItemStack itemStack,
                                 ItemFrame placedInFrame, CallbackInfo ci) {
        // Only process if this is our tracked map
        if (!BetterMapsModClient.minimapEnabled) return;
        if (BetterMapsModClient.currentMapData == null) return;

        // Check this is our map instance
        MapItemSavedData self = (MapItemSavedData)(Object)this;
        if (self != BetterMapsModClient.currentMapData) return;

        // Find the player decoration - vanilla already computed x/y/rot for us
        String playerName = tickingPlayer.getPlainTextName();
        MapDecoration decoration = this.decorations.get(playerName);

        if (decoration == null) return;

        // Convert the clamped byte coords to 0..1 range for the minimap
        float dx = (decoration.x() + 128f) / 256f;
        float dy = (decoration.y() + 128f) / 256f;

        MinimapRenderer.targetMarkerX = dx;
        MinimapRenderer.targetMarkerY = dy;
        MinimapRenderer.markerRot = decoration.rot();

        // Detect off-map: vanilla uses PLAYER_OFF_MAP type when outside
        MinimapRenderer.markerOffMap = decoration.type().is(MapDecorationTypes.PLAYER_OFF_MAP)
                || decoration.type().is(MapDecorationTypes.PLAYER_OFF_LIMITS);
    }
}