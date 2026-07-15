package net.meellowamg.bettermapsmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BetterMapsStatsScreen extends Screen {

    private final Screen parent;
    private int scrollOffset = 0;
    private static final int SCROLL_AMOUNT = 15;
    private static final int LINE_HEIGHT = 14;
    private static final int PADDING = 20;

    public BetterMapsStatsScreen(Screen parent) {
        super(Component.literal("Better Maps - Stats"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(
                Component.literal("Back"), btn -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 - 50, this.height - 30, 100, 20).build());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = (int) Math.max(0, scrollOffset - scrollY * SCROLL_AMOUNT);
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        BetterMapsStats stats = BetterMapsStats.get();

        // Background
        graphics.fill(0, 0, this.width, this.height, 0xAA000000);

        // Title
        graphics.text(this.font, this.title, this.width / 2, 10, 0xFFD700, true);

        List<String[]> lines = buildLines(stats);

        int y = PADDING + 20 - scrollOffset;
        for (String[] line : lines) {
            if (y > 0 && y < this.height - 40) {
                if (line.length == 1) {
                    // Section header
                    graphics.text(this.font, line[0], PADDING, y, 0xFFD700, false);
                } else {
                    // Stat line: label on left, value on right
                    graphics.text(this.font, line[0], PADDING + 10, y, 0xCCCCCC, false);
                    graphics.text(this.font, line[1], this.width - PADDING - this.font.width(line[1]), y, 0xFFFFFF, false);
                }
            }
            y += LINE_HEIGHT;
        }
    }

    private List<String[]> buildLines(BetterMapsStats stats) {
        List<String[]> lines = new ArrayList<>();

        lines.add(new String[]{ "--- Map Stats ---" });
        lines.add(new String[]{ "Maps Pinned",    String.valueOf(stats.mapsPinned) });
        lines.add(new String[]{ "Unique Maps",     String.valueOf(stats.mapsExplored) });
        lines.add(new String[]{ "" });

        lines.add(new String[]{ "--- Exploration ---" });
        lines.add(new String[]{ "Distance Tracked", stats.getFormattedDistance() });
        lines.add(new String[]{ "Time with Minimap", stats.getFormattedTime() });
        lines.add(new String[]{ "" });

        lines.add(new String[]{ "--- Biomes Visited (" + stats.biomesVisited.size() + ") ---" });
        for (String biome : stats.biomesVisited.stream().sorted().toList()) {
            String[] words = biome.split("_");
            StringBuilder sb = new StringBuilder();
            for (String w : words) {
                if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
            lines.add(new String[]{ sb.toString().trim(), "✓" });
        }

        return lines;
    }
}