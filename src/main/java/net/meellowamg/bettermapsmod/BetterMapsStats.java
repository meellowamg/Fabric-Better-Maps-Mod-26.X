package net.meellowamg.bettermapsmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class BetterMapsStats {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path STATS_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("bettermapsmod_stats.json");

    public int    mapsPinned       = 0;
    public long   distanceTracked  = 0; // in blocks
    public long   timeWithMinimap  = 0; // in ticks
    public int    mapsExplored     = 0;
    public Set<String> uniqueMapIds   = new HashSet<>();
    public Set<String> biomesVisited  = new HashSet<>();

    private static BetterMapsStats instance;

    // Tracking helpers (not saved)
    private static double lastX = 0, lastZ = 0;
    private static boolean positionInitialized = false;

    public static BetterMapsStats get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        if (Files.exists(STATS_PATH)) {
            try (Reader reader = Files.newBufferedReader(STATS_PATH)) {
                instance = GSON.fromJson(reader, BetterMapsStats.class);
                if (instance == null) instance = new BetterMapsStats();
                if (instance.uniqueMapIds == null) instance.uniqueMapIds = new HashSet<>();
                if (instance.biomesVisited == null) instance.biomesVisited = new HashSet<>();
            } catch (IOException e) {
                BetterMapsMod.LOGGER.error("Failed to load stats", e);
                instance = new BetterMapsStats();
            }
        } else {
            instance = new BetterMapsStats();
            save();
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(STATS_PATH)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            BetterMapsMod.LOGGER.error("Failed to save stats", e);
        }
    }

    public static void reset() {
        positionInitialized = false;
    }

    public static void onMapPinned(int mapId) {
        BetterMapsStats stats = get();
        stats.mapsPinned++;
        String key = String.valueOf(mapId);
        if (stats.uniqueMapIds.add(key)) {
            stats.mapsExplored++;
        }
        save();
    }

    public static void tick(net.minecraft.client.Minecraft client) {
        if (!BetterMapsModClient.minimapEnabled) {
            positionInitialized = false;
            return;
        }
        if (client.player == null) return;

        BetterMapsStats stats = get();

        // Time tracking
        stats.timeWithMinimap++;

        // Distance tracking
        double px = client.player.getX();
        double pz = client.player.getZ();
        if (positionInitialized) {
            double dx = px - lastX;
            double dz = pz - lastZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 10) { // ignore teleports
                stats.distanceTracked += (long) dist;
            }
        }
        lastX = px;
        lastZ = pz;
        positionInitialized = true;

        // Biome tracking
        if (client.level != null) {
            var biomeHolder = client.level.getBiome(client.player.blockPosition());
            biomeHolder.unwrapKey().ifPresent(k -> {
                String name = k.toString();
                int lastSlash = name.lastIndexOf('/');
                int lastBracket = name.lastIndexOf(']');
                if (lastSlash >= 0 && lastBracket > lastSlash) {
                    String path = name.substring(lastSlash + 1, lastBracket).trim();
                    int colon = path.indexOf(':');
                    if (colon >= 0) path = path.substring(colon + 1);
                    if (stats.biomesVisited.add(path)) {
                        save(); // save when new biome discovered
                    }
                }
            });
        }

        // Save every 20 ticks (1 second)
        if (stats.timeWithMinimap % 20 == 0) {
            save();
        }
    }

    public String getFormattedTime() {
        long ticks = get().timeWithMinimap;
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        if (hours > 0)   return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

    public String getFormattedDistance() {
        long blocks = get().distanceTracked;
        if (blocks >= 1000) return String.format("%.1fk blocks", blocks / 1000.0);
        return blocks + " blocks";
    }
}