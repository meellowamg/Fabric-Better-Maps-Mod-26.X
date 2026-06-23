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
                byte rawId = this.data.colors[i];
                int colorId = (rawId & 0xFF) >> 2;
                int shade = rawId & 0x3;

                int color = MapColor.getColorFromPackedId(rawId);
                int r = color & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = (color >> 16) & 0xFF;

                if (colorId == 0) {
                    // Unexplored - keep as is
                    pixels.setPixel(x, y, (0xFF << 24) | (b << 16) | (g << 8) | r);
                    continue;
                }

                if (colorId == 12) {
                    // Water - keep original colors, just enhance depth shading
                    float waterShade = switch (shade) {
                        case 0 -> 0.6f;   // deep
                        case 1 -> 0.8f;   // medium
                        case 2 -> 1.0f;   // normal
                        case 3 -> 1.15f;  // shallow
                        default -> 1.0f;
                    };

                    r = (int) Math.min(255, r * waterShade);
                    g = (int) Math.min(255, g * waterShade);
                    b = (int) Math.min(255, b * waterShade);

                    // Coastline detection - brighten water pixels next to land
                    if (x > 0 && x < 127 && y > 0 && y < 127) {
                        boolean isCoastline = false;
                        int[] neighbors = {
                                (x-1) + y * 128,
                                (x+1) + y * 128,
                                x + (y-1) * 128,
                                x + (y+1) * 128
                        };
                        for (int ni : neighbors) {
                            int neighborColorId = (this.data.colors[ni] & 0xFF) >> 2;
                            if (neighborColorId != 12 && neighborColorId != 0) {
                                isCoastline = true;
                                break;
                            }
                        }
                        if (isCoastline) {
                            r = (int) Math.min(255, r * 1.4f);
                            g = (int) Math.min(255, g * 1.3f);
                            b = (int) Math.min(255, b * 1.2f);
                        }
                    }
                } else {
                    // All terrain - height shading + subtle boost
                    float shadeMult = switch (shade) {
                        case 0 -> 0.68f;
                        case 1 -> 0.85f;
                        case 2 -> 1.0f;
                        case 3 -> 1.18f;
                        default -> 1.0f;
                    };
                    r = (int) Math.min(255, r * shadeMult * 1.05f);
                    g = (int) Math.min(255, g * shadeMult * 1.05f);
                    b = (int) Math.min(255, b * shadeMult * 1.05f);
                }

                pixels.setPixel(x, y, (0xFF << 24) | (b << 16) | (g << 8) | r);
            }
        }

        this.texture.upload();
        ci.cancel();
    }
}