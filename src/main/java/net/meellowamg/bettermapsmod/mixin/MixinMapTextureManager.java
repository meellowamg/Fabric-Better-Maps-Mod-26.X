package net.meellowamg.bettermapsmod.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.resources.MapTextureManager$MapInstance")
public class MixinMapTextureManager {

    @Shadow
    private MapItemSavedData data;

    @Shadow
    private DynamicTexture texture;

    @Inject(method = "updateTextureIfNeeded", at = @At("HEAD"), cancellable = true)
    private void onUpdateTexture(CallbackInfo ci) {
        NativeImage pixels = this.texture.getPixels();
        if (pixels == null) return;

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int i = x + y * 128;
                int color = MapColor.getColorFromPackedId(this.data.colors[i]);

                // NativeImage is ABGR so extract channels accordingly
                int r = color & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = (color >> 16) & 0xFF;

                // Boost contrast by 30%
                r = (int) Math.min(255, r * 1.3);
                g = (int) Math.min(255, g * 1.3);
                b = (int) Math.min(255, b * 1.3);

                // Pack back into ABGR format
                int enhanced = (0xFF << 24) | (b << 16) | (g << 8) | r;
                pixels.setPixel(x, y, enhanced);
            }
        }

        this.texture.upload();
        ci.cancel();
    }
}