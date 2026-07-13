package net.meellowamg.bettermapsmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BetterMapsConfigScreen extends Screen {

    private final Screen parent;
    private final BetterMapsConfig config;

    // Border color presets: name -> ARGB
    private static final String[] COLOR_NAMES  = { "Dark Gray", "Black", "White", "Brown", "Red", "Blue", "Green" };
    private static final int[]    COLOR_VALUES  = { 0xFF222222, 0xFF000000, 0xFFFFFFFF, 0xFF6B4A2A, 0xFF8B0000, 0xFF00008B, 0xFF006400 };

    public BetterMapsConfigScreen(Screen parent) {
        super(Component.literal("Better Maps Settings"));
        this.parent = parent;
        this.config = BetterMapsConfig.get();
    }

    @Override
    protected void init() {
        int cx      = this.width / 2;
        int y       = 20;
        int spacing = 24;
        int w       = 200;
        int h       = 20;

        // ---- VISUAL ----
        // Scale
        this.addRenderableWidget(new AbstractSliderButton(cx - w / 2, y, w, h,
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
        this.addRenderableWidget(new AbstractSliderButton(cx - w / 2, y, w, h,
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
        this.addRenderableWidget(new AbstractSliderButton(cx - w / 2, y, w, h,
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
        this.addRenderableWidget(new AbstractSliderButton(cx - w / 2, y, w, h,
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

        // Border color
        int currentColorIdx = 0;
        for (int i = 0; i < COLOR_VALUES.length; i++) {
            if (COLOR_VALUES[i] == config.borderColor) { currentColorIdx = i; break; }
        }
        final int[] colorIdx = { currentColorIdx };
        this.addRenderableWidget(CycleButton.<String>builder(
                        s -> Component.literal("Border Color: " + s),
                        COLOR_NAMES[colorIdx[0]])
                .withValues(COLOR_NAMES)
                .create(cx - w / 2, y, w, h,
                        Component.literal("Border Color"),
                        (btn, val) -> {
                            for (int i = 0; i < COLOR_NAMES.length; i++) {
                                if (COLOR_NAMES[i].equals(val)) {
                                    config.borderColor = COLOR_VALUES[i];
                                    break;
                                }
                            }
                        }));
        y += spacing;

        // Marker size
        this.addRenderableWidget(new AbstractSliderButton(cx - w / 2, y, w, h,
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
        this.addRenderableWidget(CycleButton.<String>builder(
                        s -> Component.literal("Position: " + s),
                        config.minimapPosition)
                .withValues("TOP_RIGHT", "TOP_LEFT", "BOTTOM_RIGHT", "BOTTOM_LEFT")
                .create(cx - w / 2, y, w, h,
                        Component.literal("Position"),
                        (btn, val) -> config.minimapPosition = val));
        y += spacing;

        // ---- FUNCTIONAL ----
        // Show marker toggle
        this.addRenderableWidget(CycleButton.<Boolean>builder(
                        b -> Component.literal("Show Marker: " + (b ? "ON" : "OFF")),
                        config.showMarker)
                .withValues(true, false)
                .create(cx - w / 2, y, w, h,
                        Component.literal("Show Marker"),
                        (btn, val) -> config.showMarker = val));
        y += spacing;

        // Show coordinates toggle
        this.addRenderableWidget(CycleButton.<Boolean>builder(
                        b -> Component.literal("Show Coordinates: " + (b ? "ON" : "OFF")),
                        config.showCoordinates)
                .withValues(true, false)
                .create(cx - w / 2, y, w, h,
                        Component.literal("Show Coordinates"),
                        (btn, val) -> config.showCoordinates = val));
        y += spacing;

        // Show biome toggle
        this.addRenderableWidget(CycleButton.<Boolean>builder(
                        b -> Component.literal("Show Biome: " + (b ? "ON" : "OFF")),
                        config.showBiome)
                .withValues(true, false)
                .create(cx - w / 2, y, w, h,
                        Component.literal("Show Biome"),
                        (btn, val) -> config.showBiome = val));
        y += spacing;

        // Show map scale toggle
        this.addRenderableWidget(CycleButton.<Boolean>builder(
                        b -> Component.literal("Show Map Scale: " + (b ? "ON" : "OFF")),
                        config.showMapScale)
                .withValues(true, false)
                .create(cx - w / 2, y, w, h,
                        Component.literal("Show Map Scale"),
                        (btn, val) -> config.showMapScale = val));
        y += spacing;

        // ---- INTERACTION ----
        // Pin map key
        this.addRenderableWidget(CycleButton.<String>builder(
                        s -> Component.literal("Pin Key: " + s),
                        config.pinMapKey)
                .withValues("CROUCH_RIGHT_CLICK", "RIGHT_CLICK")
                .create(cx - w / 2, y, w, h,
                        Component.literal("Pin Key"),
                        (btn, val) -> config.pinMapKey = val));
        y += spacing;

        // Done
        this.addRenderableWidget(Button.builder(
                Component.literal("Done"), btn -> {
                    BetterMapsConfig.save();
                    this.minecraft.setScreen(parent);
                }).bounds(cx - 75, y + 4, 150, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.text(this.font, this.title, this.width / 2, 6, 0xFFFFFF, true);
    }

    @Override
    public void onClose() {
        BetterMapsConfig.save();
        this.minecraft.setScreen(parent);
    }
}