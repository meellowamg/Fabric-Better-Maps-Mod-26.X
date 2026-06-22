package net.meellowamg.bettermapsmod.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.meellowamg.bettermapsmod.BetterMapsModClient;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
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

    @Shadow
    private Identifier location;

    @Inject(method = "updateTextureIfNeeded", at = @At("HEAD"), cancellable = true)
    private void onUpdateTexture(CallbackInfo ci) {
        NativeImage pixels = this.texture.getPixels();
        if (pixels == null) return;

        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                int i = x + y * 128;
                int color = MapColor.getColorFromPackedId(this.data.colors[i]);

                int r = color & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = (color >> 16) & 0xFF;

                r = (int) Math.min(255, r * 1.05);
                g = (int) Math.min(255, g * 1.05);
                b = (int) Math.min(255, b * 1.05);

                int enhanced = (0xFF << 24) | (b << 16) | (g << 8) | r;
                pixels.setPixel(x, y, enhanced);
            }
        }

        this.texture.upload();

        // Store map data whenever this is our locked map
        if (this.location != null && this.location.equals(BetterMapsModClient.currentMapTexture)) {
            BetterMapsModClient.currentMapData = this.data;
        }

        // Also store before locking so data is ready
        if (!BetterMapsModClient.minimapEnabled) {
            BetterMapsModClient.currentMapData = this.data;
        }

        ci.cancel();
    }
}