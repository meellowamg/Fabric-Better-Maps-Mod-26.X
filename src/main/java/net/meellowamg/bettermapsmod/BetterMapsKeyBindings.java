package net.meellowamg.bettermapsmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class BetterMapsKeyBindings {

    public static KeyMapping toggleMinimapKey;

    public static void register() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("bettermapsmod", "custom_category")
        );

        toggleMinimapKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bettermapsmod.toggle_minimap",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (toggleMinimapKey.consumeClick()) {
                if (BetterMapsModClient.minimapEnabled) {
                    BetterMapsModClient.minimapEnabled = false;
                    MinimapRenderer.reset();
                } else {
                    if (BetterMapsModClient.currentMapTexture != null) {
                        BetterMapsModClient.minimapEnabled = true;
                    }
                }
            }
        });
    }
}