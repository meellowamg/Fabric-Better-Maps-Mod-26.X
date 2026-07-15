package net.meellowamg.bettermapsmod.mixin;

import net.meellowamg.bettermapsmod.BetterMapsModClient;
import net.meellowamg.bettermapsmod.BetterMapsStats;
import net.meellowamg.bettermapsmod.MinimapRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinUseItem {

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void onStartUseItem(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        if (!client.player.getMainHandItem().is(Items.FILLED_MAP)) return;
        if (!client.player.isCrouching()) return;

        if (BetterMapsModClient.minimapEnabled) {
            BetterMapsModClient.minimapEnabled = false;
            MinimapRenderer.reset();
        } else {
            BetterMapsModClient.minimapEnabled = true;
            if (BetterMapsModClient.currentMapId >= 0) {
                BetterMapsStats.onMapPinned(BetterMapsModClient.currentMapId);
            }
        }
    }
}