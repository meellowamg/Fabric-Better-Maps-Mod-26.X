package net.meellowamg.bettermapsmod.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.meellowamg.bettermapsmod.BetterMapsModClient;
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

                // Feature 1 & 5: Enhanced height shading for better terrain readability
                float shadeMult = switch (shade) {
                    case 0 -> 0.68f;   // dark - valleys
                    case 1 -> 0.85f;   // medium - slopes
                    case 2 -> 1.0f;    // normal - flat
                    case 3 -> 1.18f;   // bright - peaks
                    default -> 1.0f;
                };

                // Feature 2: Enhanced water visibility
                // Water color id is 12
                if (colorId == 12) {
                    // Make water deeper and more distinct blue
                    r = (int) Math.min(255, r * 0.7f);
                    g = (int) Math.min(255, g * 0.85f);
                    b = (int) Math.min(255, b * 1.2f);

                    // Enhance water depth shading more dramatically
                    float waterShade = switch (shade) {
                        case 0 -> 0.55f;   // deep water - very dark
                        case 1 -> 0.75f;   // medium depth
                        case 2 -> 0.95f;   // shallow
                        case 3 -> 1.1f;    // very shallow/edge
                        default -> 1.0f;
                    };
                    r = (int) Math.min(255, r * waterShade);
                    g = (int) Math.min(255, g * waterShade);
                    b = (int) Math.min(255, b * waterShade);

                    // Feature 2: Detect coastlines by checking neighbors
                    boolean isCoastline = false;
                    if (x > 0 && x < 127 && y > 0 && y < 127) {
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
                    }

                    if (isCoastline) {
                        // Slightly lighten coastline water pixels for border effect
                        r = (int) Math.min(255, r * 1.3f);
                        g = (int) Math.min(255, g * 1.2f);
                        b = (int) Math.min(255, b * 1.1f);
                    }

                } else if (colorId == 0) {
                    // Empty/unexplored - keep as is
                    int enhanced = (0xFF << 24) | (b << 16) | (g << 8) | r;
                    pixels.setPixel(x, y, enhanced);
                    continue;
                } else {
                    // Feature 1: All other terrain - apply height shading + subtle boost
                    r = (int) Math.min(255, r * shadeMult * 1.05f);
                    g = (int) Math.min(255, g * shadeMult * 1.05f);
                    b = (int) Math.min(255, b * shadeMult * 1.05f);
                }

                int enhanced = (0xFF << 24) | (b << 16) | (g << 8) | r;
                pixels.setPixel(x, y, enhanced);
            }
        }

        this.texture.upload();
        ci.cancel();
    }
}