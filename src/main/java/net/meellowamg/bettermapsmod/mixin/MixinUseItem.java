package net.meellowamg.bettermapsmod.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.meellowamg.bettermapsmod.BetterMapsKeyBindings;
import net.meellowamg.bettermapsmod.BetterMapsModClient;
import net.meellowamg.bettermapsmod.MinimapRenderer;
import net.minecraft.client.KeyMapping;
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

        // If a dedicated keybind is set, skip crouch+click
        KeyMapping key = BetterMapsKeyBindings.toggleMinimapKey;
        if (key != null && !key.isUnbound()) return;

        if (BetterMapsModClient.minimapEnabled) {
            BetterMapsModClient.minimapEnabled = false;
            MinimapRenderer.reset();
        } else {
            BetterMapsModClient.minimapEnabled = true;
        }
    }
}