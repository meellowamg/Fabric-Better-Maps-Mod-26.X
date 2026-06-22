package net.meellowamg.bettermapsmod.mixin;

import net.meellowamg.bettermapsmod.BetterMapsModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItem.class)
public class MixinMapItem {

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdate(Level level, Entity player, MapItemSavedData data, CallbackInfo ci) {
        if (!BetterMapsModClient.minimapEnabled) return;
        if (BetterMapsModClient.currentMapTexture == null) return;

        // Force the map texture manager to re-upload so position updates
        Minecraft mc = Minecraft.getInstance();
        if (mc.getMapTextureManager() != null) {
            // This triggers a re-render of the locked map next frame
            BetterMapsModClient.needsTextureUpdate = true;
        }
    }
}