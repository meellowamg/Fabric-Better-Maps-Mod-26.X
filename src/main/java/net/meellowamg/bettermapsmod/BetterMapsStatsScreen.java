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
    private static final int LINE_HEIGHT   = 18;
    private static final int START_Y       = 30;
    private static final int PAD           = 15;

    public BetterMapsStatsScreen(Screen parent) {
        super(Component.literal("Better Maps - Stats"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(
                Component.literal("Back"), btn -> this.minecraft.setScreen(parent)
        ).bounds(this.width / 2 - 50, this.height - 28, 100, 20).build());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = (int) Math.max(0, scrollOffset - scrollY * SCROLL_AMOUNT);
        return true;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Background
        graphics.fill(0, 0, this.width, this.height, 0xDD101010);
        // Title bar
        graphics.fill(0, 0, this.width, 24, 0xFF181818);
        graphics.fill(0, 23, this.width, 24, 0xFF444444);
        graphics.text(this.font, "Better Maps  Stats", this.width / 2, 7, 0xFFD700, true);

        BetterMapsStats stats = BetterMapsStats.get();
        List<StatLine> lines  = buildLines(stats);

        int y = START_Y - scrollOffset;
        for (StatLine line : lines) {
            if (y + LINE_HEIGHT > 24 && y < this.height - 32) {
                if (line.isHeader) {
                    graphics.fill(PAD, y, this.width - PAD, y + LINE_HEIGHT, 0xFF222222);
                    graphics.fill(PAD, y, PAD + 3, y + LINE_HEIGHT, 0xFFFFAA00);
                    graphics.text(this.font, line.label, PAD + 8, y + 5, 0xFFAA00, false);
                } else if (line.isSpacer) {
                    // draw thin line
                    graphics.fill(PAD + 10, y + LINE_HEIGHT / 2, this.width - PAD - 10, y + LINE_HEIGHT / 2 + 1, 0xFF333333);
                } else {
                    graphics.text(this.font, line.label, PAD + 12, y + 4, 0xBBBBBB, false);
                    if (line.value != null) {
                        graphics.text(this.font, line.value,
                                this.width - PAD - this.font.width(line.value), y + 4, 0xFFFFFF, false);
                    }
                }
            }
            y += LINE_HEIGHT;
        }

        // Scroll hint
        if (scrollOffset > 0)
            graphics.text(this.font, "\u25B2 scroll", this.width / 2, this.height - 44, 0x666666, true);

        // Always call super last so buttons render on top
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private List<StatLine> buildLines(BetterMapsStats stats) {
        List<StatLine> lines = new ArrayList<>();

        lines.add(StatLine.header("Map Stats"));
        lines.add(StatLine.stat("Times Pinned",      String.valueOf(stats.mapsPinned)));
        lines.add(StatLine.stat("Unique Maps Used",   String.valueOf(stats.mapsExplored)));
        lines.add(StatLine.spacer());

        lines.add(StatLine.header("Exploration"));
        lines.add(StatLine.stat("Distance Tracked",  stats.getFormattedDistance()));
        lines.add(StatLine.stat("Time with Minimap", stats.getFormattedTime()));
        lines.add(StatLine.spacer());

        lines.add(StatLine.header("Biomes Visited  (" + stats.biomesVisited.size() + ")"));
        if (stats.biomesVisited.isEmpty()) {
            lines.add(StatLine.stat("None yet \u2014 explore with minimap active!", null));
        } else {
            for (String biome : stats.biomesVisited.stream().sorted().toList()) {
                String[] words = biome.split("_");
                StringBuilder sb = new StringBuilder();
                for (String w : words)
                    if (!w.isEmpty())
                        sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
                lines.add(StatLine.stat(sb.toString().trim(), "\u2713"));
            }
        }

        return lines;
    }

    private static class StatLine {
        String  label   = "";
        String  value   = null;
        boolean isHeader = false;
        boolean isSpacer = false;

        static StatLine header(String label) {
            StatLine l = new StatLine(); l.label = label; l.isHeader = true; return l;
        }
        static StatLine stat(String label, String value) {
            StatLine l = new StatLine(); l.label = label; l.value = value; return l;
        }
        static StatLine spacer() {
            StatLine l = new StatLine(); l.isSpacer = true; return l;
        }
    }
}