package net.meellowamg.bettermapsmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BetterMapsConfigScreen extends Screen {

    private final Screen parent;
    private final BetterMapsConfig config;

    // Scrolling
    private int scrollOffset = 0;
    private static final int SCROLL_AMOUNT = 20;
    private int contentHeight = 0;

    // Hex input boxes
    private EditBox outerColorBox;
    private EditBox innerColorBox;

    public BetterMapsConfigScreen(Screen parent) {
        super(Component.literal("Better Maps Settings"));
        this.parent = parent;
        this.config = BetterMapsConfig.get();
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int cx      = this.width / 2;
        int y       = 30 - scrollOffset;
        int spacing = 26;
        int w       = 200;
        int h       = 20;

        // ---- VISUAL ----
        // Scale
        addWidget(new AbstractSliderButton(cx - w / 2, y, w, h,
                Component.literal("Map Scale: " + String.format("%.1f", config.minimapScale) + "x"),
                (config.minimapScale - 0.5f) / 2.5f) {
            @Override protected void updateMessage() {
                float v = 0.5f + (float)(this.value * 2.5f);
                setMessage(Component.literal("Map Scale: " + String.format("%.1f", v) + "x"));
            }
            @Override protected void applyValue() {
                config.minimapScale = 0.5f + (float)(this.value * 2.5f);
            }
        });
        y += spacing;

        // Opacity
        addWidget(new AbstractSliderButton(cx - w / 2, y, w, h,
                Component.literal("Opacity: " + Math.round(config.minimapOpacity * 100) + "%"),
                config.minimapOpacity) {
            @Override protected void updateMessage() {
                setMessage(Component.literal("Opacity: " + Math.round(this.value * 100) + "%"));
            }
            @Override protected void applyValue() {
                config.minimapOpacity = (float) Math.max(0.1, this.value);
            }
        });
        y += spacing;

        // Margin
        addWidget(new AbstractSliderButton(cx - w / 2, y, w, h,
                Component.literal("Margin: " + config.minimapMargin),
                config.minimapMargin / 50.0) {
            @Override protected void updateMessage() {
                setMessage(Component.literal("Margin: " + (int)(this.value * 50)));
            }
            @Override protected void applyValue() {
                config.minimapMargin = (int)(this.value * 50);
            }
        });
        y += spacing;

        // Border thickness
        addWidget(new AbstractSliderButton(cx - w / 2, y, w, h,
                Component.literal("Border Thickness: " + config.borderThickness),
                (config.borderThickness - 1) / 9.0) {
            @Override protected void updateMessage() {
                setMessage(Component.literal("Border Thickness: " + (int)(1 + this.value * 9)));
            }
            @Override protected void applyValue() {
                config.borderThickness = (int)(1 + this.value * 9);
            }
        });
        y += spacing;

        // Outer border color hex
        outerColorBox = new EditBox(this.font, cx - w / 2, y, w, h,
                Component.literal("Outer Border Color"));
        outerColorBox.setMaxLength(8);
        outerColorBox.setValue(String.format("%06X", config.borderOuterColor & 0xFFFFFF));
        outerColorBox.setResponder(val -> {
            try {
                config.borderOuterColor = (int)(0xFF000000L | Long.parseLong(val, 16));
            } catch (NumberFormatException ignored) {}
        });
        addWidget(outerColorBox);
        y += spacing;

        // Inner border color hex
        innerColorBox = new EditBox(this.font, cx - w / 2, y, w, h,
                Component.literal("Inner Border Color"));
        innerColorBox.setMaxLength(8);
        innerColorBox.setValue(String.format("%06X", config.borderInnerColor & 0xFFFFFF));
        innerColorBox.setResponder(val -> {
            try {
                config.borderInnerColor = (int)(0xFF000000L | Long.parseLong(val, 16));
            } catch (NumberFormatException ignored) {}
        });
        addWidget(innerColorBox);
        y += spacing;

        // Marker size
        addWidget(new AbstractSliderButton(cx - w / 2, y, w, h,
                Component.literal("Marker Size: " + String.format("%.1f", config.markerScale) + "x"),
                (config.markerScale - 0.5f) / 1.5f) {
            @Override protected void updateMessage() {
                float v = 0.5f + (float)(this.value * 1.5f);
                setMessage(Component.literal("Marker Size: " + String.format("%.1f", v) + "x"));
            }
            @Override protected void applyValue() {
                config.markerScale = 0.5f + (float)(this.value * 1.5f);
            }
        });
        y += spacing;

        // Position
        addWidget(CycleButton.<String>builder(
                        s -> Component.literal("Position: " + s),
                        config.minimapPosition)
                .withValues("TOP_RIGHT", "TOP_LEFT", "BOTTOM_RIGHT", "BOTTOM_LEFT")
                .create(cx - w / 2, y, w, h,
                        Component.literal("Position"),
                        (btn, val) -> config.minimapPosition = val));
        y += spacing;

        // ---- FUNCTIONAL ----
        addWidget(CycleButton.<Boolean>builder(
                        b -> Component.literal("Show Marker: " + (b ? "ON" : "OFF")),
                        config.showMarker)
                .withValues(true, false)
                .create(cx - w / 2, y, w, h,
                        Component.literal("Show Marker"),
                        (btn, val) -> config.showMarker = val));
        y += spacing;

        addWidget(CycleButton.<Boolean>builder(
                        b -> Component.literal("Show Coordinates: " + (b ? "ON" : "OFF")),
                        config.showCoordinates)
                .withValues(true, false)
                .create(cx - w / 2, y, w, h,
                        Component.literal("Show Coordinates"),
                        (btn, val) -> config.showCoordinates = val));
        y += spacing;

        addWidget(CycleButton.<Boolean>builder(
                        b -> Component.literal("Show Biome: " + (b ? "ON" : "OFF")),
                        config.showBiome)
                .withValues(true, false)
                .create(cx - w / 2, y, w, h,
                        Component.literal("Show Biome"),
                        (btn, val) -> config.showBiome = val));
        y += spacing;

        // ---- KEYBIND INFO ----
        // Keybind is set in Minecraft Controls menu, just show info
        y += 4;
        addWidget(Button.builder(
                Component.literal("Toggle Key: set in Controls menu"), btn -> {
                    // Open controls screen
                    this.minecraft.setScreen(
                            new net.minecraft.client.gui.screens.options.controls.ControlsScreen(this, this.minecraft.options)
                    );
                }).bounds(cx - w / 2, y, w, h).build());
        y += spacing;

        // Done
        addWidget(Button.builder(
                Component.literal("Done"), btn -> {
                    BetterMapsConfig.save();
                    this.minecraft.setScreen(parent);
                }).bounds(cx - w / 2, y, w, h).build());
        y += spacing;

        contentHeight = y + scrollOffset + 10;
    }

    private void addWidget(net.minecraft.client.gui.components.AbstractWidget widget) {
        // Only add if visible on screen
        this.addRenderableWidget(widget);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, contentHeight - this.height);
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * SCROLL_AMOUNT));
        this.init();
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        // Title
        graphics.text(this.font, this.title, this.width / 2, 8, 0xFFFFFF, true);
        // Section headers
        int cx = this.width / 2;
        int baseY = 30 - scrollOffset;
        int spacing = 26;
        graphics.text(this.font, "-- Visual --",       cx - 100, baseY - 14,          0xAAAAAA, false);
        graphics.text(this.font, "-- Functional --",   cx - 100, baseY + spacing * 8 - 14,  0xAAAAAA, false);
        graphics.text(this.font, "-- Interaction --",  cx - 100, baseY + spacing * 11 - 14, 0xAAAAAA, false);
        // Hex field labels
        graphics.text(this.font, "Outer Border Color (hex):", cx - 100, baseY + spacing * 4 - 12, 0xCCCCCC, false);
        graphics.text(this.font, "Inner Border Color (hex):", cx - 100, baseY + spacing * 5 - 12, 0xCCCCCC, false);
    }

    @Override
    public void onClose() {
        BetterMapsConfig.save();
        this.minecraft.setScreen(parent);
    }
}