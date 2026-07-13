package net.meellowamg.bettermapsmod.mixin;

import net.meellowamg.bettermapsmod.BetterMapsModClient;
import net.meellowamg.bettermapsmod.MinimapRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapId;
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
        if (!BetterMapsModClient.minimapEnabled) return;
        if (BetterMapsModClient.currentMapId < 0) return;

        // Only fire for the local player
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!tickingPlayer.getUUID().equals(mc.player.getUUID())) return;

        // Get map ID from itemstack data component and compare to pinned ID
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        if (mapId == null) return;
        if (mapId.id() != BetterMapsModClient.currentMapId) return;

        // This is our pinned map - update marker
        String playerName = tickingPlayer.getPlainTextName();
        MapDecoration decoration = this.decorations.get(playerName);
        if (decoration == null) return;

        float dx = (decoration.x() + 128f) / 256f;
        float dy = (decoration.y() + 128f) / 256f;

        MinimapRenderer.targetMarkerX = dx;
        MinimapRenderer.targetMarkerY = dy;
        MinimapRenderer.markerRot = decoration.rot();
        MinimapRenderer.markerOffMap = decoration.type().is(MapDecorationTypes.PLAYER_OFF_MAP)
                || decoration.type().is(MapDecorationTypes.PLAYER_OFF_LIMITS);
    }
}