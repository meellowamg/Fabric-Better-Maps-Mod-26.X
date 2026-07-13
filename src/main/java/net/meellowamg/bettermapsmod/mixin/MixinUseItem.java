package net.meellowamg.bettermapsmod.mixin;

import net.meellowamg.bettermapsmod.BetterMapsConfig;
import net.meellowamg.bettermapsmod.BetterMapsModClient;
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

        var stack = client.player.getMainHandItem();
        if (!stack.is(Items.FILLED_MAP)) return;

        BetterMapsConfig config = BetterMapsConfig.get();
        boolean triggerPin = false;

        if ("CROUCH_RIGHT_CLICK".equals(config.pinMapKey)) {
            triggerPin = client.player.isCrouching();
        } else if ("RIGHT_CLICK".equals(config.pinMapKey)) {
            triggerPin = true;
        } else if ("CROUCH_RIGHT_CLICK".equals(config.pinMapKey)) {
            triggerPin = client.player.isCrouching();
        }

        // Default fallback
        if (config.pinMapKey == null || config.pinMapKey.isEmpty()) {
            triggerPin = client.player.isCrouching();
        }

        if (!triggerPin) return;

        if (BetterMapsModClient.minimapEnabled) {
            BetterMapsModClient.minimapEnabled = false;
            MinimapRenderer.reset();
        } else {
            BetterMapsModClient.minimapEnabled = true;
        }
    }
}