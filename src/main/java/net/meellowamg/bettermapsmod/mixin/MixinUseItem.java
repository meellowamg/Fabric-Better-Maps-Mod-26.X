package net.meellowamg.bettermapsmod.mixin;

import net.meellowamg.bettermapsmod.BetterMapsMod;
import net.meellowamg.bettermapsmod.BetterMapsModClient;
import net.meellowamg.bettermapsmod.MinimapRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinUseItem {

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void onStartUseItem(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.isCrouching()) return;

        ItemStack held = mc.player.getMainHandItem();
        if (held.is(Items.FILLED_MAP)) {
            BetterMapsModClient.minimapEnabled = !BetterMapsModClient.minimapEnabled;
            if (!BetterMapsModClient.minimapEnabled) {
                MinimapRenderer.reset();
            }
            BetterMapsMod.LOGGER.info("Minimap toggled: " + BetterMapsModClient.minimapEnabled);
        }
    }
}