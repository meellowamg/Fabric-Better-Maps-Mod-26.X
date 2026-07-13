package net.meellowamg.bettermapsmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class BetterMapsConfigScreen extends Screen {

    private final Screen parent;
    private final BetterMapsConfig config;

    public BetterMapsConfigScreen(Screen parent) {
        super(Component.literal("Better Maps Config"));
        this.parent = parent;
        this.config = BetterMapsConfig.get();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 4;
        int spacing = 28;

        // Scale slider (0.5x - 3.0x)
        this.addRenderableWidget(new AbstractSliderButton(
                centerX - 100, startY, 200, 20,
                Component.literal("Scale: " + String.format("%.1f", config.minimapScale) + "x"),
                (config.minimapScale - 0.5f) / 2.5f
        ) {
            @Override
            protected void updateMessage() {
                float val = 0.5f + (float)(this.value * 2.5f);
                setMessage(Component.literal("Scale: " + String.format("%.1f", val) + "x"));
            }

            @Override
            protected void applyValue() {
                config.minimapScale = 0.5f + (float)(this.value * 2.5f);
            }
        });

        // Opacity slider (0.1 - 1.0)
        this.addRenderableWidget(new AbstractSliderButton(
                centerX - 100, startY + spacing, 200, 20,
                Component.literal("Opacity: " + Math.round(config.minimapOpacity * 100) + "%"),
                config.minimapOpacity
        ) {
            @Override
            protected void updateMessage() {
                setMessage(Component.literal("Opacity: " + Math.round(this.value * 100) + "%"));
            }

            @Override
            protected void applyValue() {
                config.minimapOpacity = (float) Math.max(0.1, this.value);
            }
        });

        // Margin slider (0 - 50)
        this.addRenderableWidget(new AbstractSliderButton(
                centerX - 100, startY + spacing * 2, 200, 20,
                Component.literal("Margin: " + config.minimapMargin),
                config.minimapMargin / 50.0
        ) {
            @Override
            protected void updateMessage() {
                int val = (int)(this.value * 50);
                setMessage(Component.literal("Margin: " + val));
            }

            @Override
            protected void applyValue() {
                config.minimapMargin = (int)(this.value * 50);
            }
        });

        // Position cycle button
        this.addRenderableWidget(CycleButton.<String>builder(
                        s -> Component.literal("Position: " + s),
                        config.minimapPosition)
                .withValues("TOP_RIGHT", "TOP_LEFT", "BOTTOM_RIGHT", "BOTTOM_LEFT")
                .create(centerX - 100, startY + spacing * 3, 200, 20,
                        Component.literal("Position"),
                        (button, value) -> config.minimapPosition = value));

        // Done button
        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> {
            BetterMapsConfig.save();
            this.minecraft.setScreen(parent);
        }).bounds(centerX - 75, startY + spacing * 5, 150, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.font, this.title, this.width / 2, 20, 0xFFFFFF, true);
    }

    @Override
    public void onClose() {
        BetterMapsConfig.save();
        this.minecraft.setScreen(parent);
    }
}