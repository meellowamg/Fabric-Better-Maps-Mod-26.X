package net.meellowamg.bettermapsmod.mixin;

import net.meellowamg.bettermapsmod.BetterMapsMod;
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

        MapItemSavedData self = (MapItemSavedData)(Object)this;

        // Log every tick so we can see what's happening
        BetterMapsMod.LOGGER.info("tickCarriedBy fired. minimapEnabled=" + BetterMapsModClient.minimapEnabled
                + " currentMapData=" + (BetterMapsModClient.currentMapData != null ? "SET" : "NULL")
                + " sameInstance=" + (self == BetterMapsModClient.currentMapData)
                + " decorations=" + this.decorations.keySet()
                + " player=" + tickingPlayer.getPlainTextName());

        if (!BetterMapsModClient.minimapEnabled) return;
        if (BetterMapsModClient.currentMapData == null) return;

        if (self != BetterMapsModClient.currentMapData) {
            BetterMapsMod.LOGGER.info("Instance mismatch! self=" + System.identityHashCode(self)
                    + " currentMapData=" + System.identityHashCode(BetterMapsModClient.currentMapData));
            return;
        }

        String playerName = tickingPlayer.getPlainTextName();
        MapDecoration decoration = this.decorations.get(playerName);

        BetterMapsMod.LOGGER.info("decoration for '" + playerName + "': " + decoration);

        if (decoration == null) return;

        float dx = (decoration.x() + 128f) / 256f;
        float dy = (decoration.y() + 128f) / 256f;

        MinimapRenderer.targetMarkerX = dx;
        MinimapRenderer.targetMarkerY = dy;
        MinimapRenderer.markerRot = decoration.rot();
        MinimapRenderer.markerOffMap = decoration.type().is(MapDecorationTypes.PLAYER_OFF_MAP)
                || decoration.type().is(MapDecorationTypes.PLAYER_OFF_LIMITS);

        BetterMapsMod.LOGGER.info("Marker updated: dx=" + dx + " dy=" + dy + " offMap=" + MinimapRenderer.markerOffMap);
    }
}