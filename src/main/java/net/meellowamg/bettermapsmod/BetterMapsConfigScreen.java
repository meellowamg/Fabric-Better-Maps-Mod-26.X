package net.meellowamg.bettermapsmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.network.chat.Component;

public class BetterMapsConfigScreen extends Screen {

    private final Screen parent;
    private final BetterMapsConfig config;

    private int scrollOffset = 0;
    private static final int SCROLL_AMOUNT = 20;
    private int contentHeight = 0;

    private EditBox outerColorBox;
    private EditBox innerColorBox;
    private int outerBoxY = 0;
    private int innerBoxY = 0;

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

        // Map Scale
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

        // Border Thickness
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

        // Outer border color
        y += 12;
        outerBoxY = y;
        outerColorBox = new EditBox(this.font, cx - w / 2, y, w - 26, h,
                Component.literal("Outer Color"));
        outerColorBox.setMaxLength(6);
        outerColorBox.setValue(String.format("%06X", config.borderOuterColor & 0xFFFFFF));
        outerColorBox.setResponder(val -> {
            try {
                if (val.length() == 6)
                    config.borderOuterColor = (int)(0xFF000000L | Long.parseLong(val, 16));
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(outerColorBox);
        y += spacing;

        // Inner border color
        y += 12;
        innerBoxY = y;
        innerColorBox = new EditBox(this.font, cx - w / 2, y, w - 26, h,
                Component.literal("Inner Color"));
        innerColorBox.setMaxLength(6);
        innerColorBox.setValue(String.format("%06X", config.borderInnerColor & 0xFFFFFF));
        innerColorBox.setResponder(val -> {
            try {
                if (val.length() == 6)
                    config.borderInnerColor = (int)(0xFF000000L | Long.parseLong(val, 16));
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(innerColorBox);
        y += spacing;

        // Marker Size
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
        final String[] positions = {"TOP_RIGHT", "TOP_LEFT", "BOTTOM_RIGHT", "BOTTOM_LEFT"};
        final int[] posIdx = {0};
        for (int i = 0; i < positions.length; i++)
            if (positions[i].equals(config.minimapPosition)) { posIdx[0] = i; break; }
        Button[] posBtn = new Button[1];
        posBtn[0] = Button.builder(
                Component.literal("Position: " + config.minimapPosition), btn -> {
                    posIdx[0] = (posIdx[0] + 1) % positions.length;
                    config.minimapPosition = positions[posIdx[0]];
                    btn.setMessage(Component.literal("Position: " + config.minimapPosition));
                }).bounds(cx - w / 2, y, w, h).build();
        this.addRenderableWidget(posBtn[0]);
        y += spacing;

        // Show Marker
        Button[] markerBtn = new Button[1];
        markerBtn[0] = Button.builder(
                Component.literal("Show Marker: " + (config.showMarker ? "ON" : "OFF")), btn -> {
                    config.showMarker = !config.showMarker;
                    btn.setMessage(Component.literal("Show Marker: " + (config.showMarker ? "ON" : "OFF")));
                }).bounds(cx - w / 2, y, w, h).build();
        this.addRenderableWidget(markerBtn[0]);
        y += spacing;

        // Show Coordinates
        Button[] coordBtn = new Button[1];
        coordBtn[0] = Button.builder(
                Component.literal("Show Coordinates: " + (config.showCoordinates ? "ON" : "OFF")), btn -> {
                    config.showCoordinates = !config.showCoordinates;
                    btn.setMessage(Component.literal("Show Coordinates: " + (config.showCoordinates ? "ON" : "OFF")));
                }).bounds(cx - w / 2, y, w, h).build();
        this.addRenderableWidget(coordBtn[0]);
        y += spacing;

        // Show Biome
        Button[] biomeBtn = new Button[1];
        biomeBtn[0] = Button.builder(
                Component.literal("Show Biome: " + (config.showBiome ? "ON" : "OFF")), btn -> {
                    config.showBiome = !config.showBiome;
                    btn.setMessage(Component.literal("Show Biome: " + (config.showBiome ? "ON" : "OFF")));
                }).bounds(cx - w / 2, y, w, h).build();
        this.addRenderableWidget(biomeBtn[0]);
        y += spacing;

        // Toggle Key
        this.addRenderableWidget(Button.builder(
                Component.literal("Set Toggle Key (Controls menu)"), btn ->
                        this.minecraft.setScreen(new ControlsScreen(this, this.minecraft.options))
        ).bounds(cx - w / 2, y, w, h).build());
        y += spacing;

        // Done
        this.addRenderableWidget(Button.builder(
                Component.literal("Done"), btn -> {
                    BetterMapsConfig.save();
                    this.minecraft.setScreen(parent);
                }).bounds(cx - w / 2, y, w, h).build());
        y += spacing;

        contentHeight = y + scrollOffset + 10;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, contentHeight - this.height);
        scrollOffset  = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * SCROLL_AMOUNT));
        this.init();
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        graphics.text(this.font, "Better Maps Settings", cx, 8, 0xFFFFFF, true);

        if (outerBoxY > 0) {
            graphics.text(this.font, "Outer Border Color:",
                    cx - 100, outerBoxY - 10, 0xCCCCCC, false);
            graphics.text(this.font, "Inner Border Color:",
                    cx - 100, innerBoxY - 10, 0xCCCCCC, false);

            int previewX = cx + 78;
            int ps = 20;

            graphics.fill(previewX, outerBoxY, previewX + ps, outerBoxY + ps, 0xFF000000);
            graphics.fill(previewX + 1, outerBoxY + 1, previewX + ps - 1, outerBoxY + ps - 1,
                    config.borderOuterColor | 0xFF000000);

            graphics.fill(previewX, innerBoxY, previewX + ps, innerBoxY + ps, 0xFF000000);
            graphics.fill(previewX + 1, innerBoxY + 1, previewX + ps - 1, innerBoxY + ps - 1,
                    config.borderInnerColor | 0xFF000000);
        }
    }

    @Override
    public void onClose() {
        BetterMapsConfig.save();
        this.minecraft.setScreen(parent);
    }
}