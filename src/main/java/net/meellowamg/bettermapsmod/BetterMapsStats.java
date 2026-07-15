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

    // All public so Gson can serialize/deserialize
    public int    mapsPinned      = 0;
    public long   distanceTracked = 0;
    public long   timeWithMinimap = 0;
    public int    mapsExplored    = 0;
    public Set<String> uniqueMapIds  = new HashSet<>();
    public Set<String> biomesVisited = new HashSet<>();

    // Single static instance — never null after load()
    private static BetterMapsStats INSTANCE = new BetterMapsStats();

    // Position tracking — not saved
    private static double lastX            = 0;
    private static double lastZ            = 0;
    private static boolean posInitialized  = false;
    private static int     tickCounter     = 0;

    public static BetterMapsStats get() {
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(STATS_PATH)) {
            try (Reader reader = Files.newBufferedReader(STATS_PATH)) {
                BetterMapsStats loaded = GSON.fromJson(reader, BetterMapsStats.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                    if (INSTANCE.uniqueMapIds  == null) INSTANCE.uniqueMapIds  = new HashSet<>();
                    if (INSTANCE.biomesVisited == null) INSTANCE.biomesVisited = new HashSet<>();
                    BetterMapsMod.LOGGER.info("Stats loaded: mapsPinned=" + INSTANCE.mapsPinned
                            + " distance=" + INSTANCE.distanceTracked
                            + " biomes=" + INSTANCE.biomesVisited.size());
                }
            } catch (IOException e) {
                BetterMapsMod.LOGGER.error("Failed to load stats: " + e.getMessage());
            }
        } else {
            BetterMapsMod.LOGGER.info("No stats file found, starting fresh at " + STATS_PATH);
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(STATS_PATH)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            BetterMapsMod.LOGGER.error("Failed to save stats: " + e.getMessage());
        }
    }

    public static void reset() {
        posInitialized = false;
        tickCounter    = 0;
    }

    public static void onMapPinned(int mapId) {
        INSTANCE.mapsPinned++;
        if (INSTANCE.uniqueMapIds.add(String.valueOf(mapId))) {
            INSTANCE.mapsExplored++;
        }
        save();
        BetterMapsMod.LOGGER.info("Map pinned! Total: " + INSTANCE.mapsPinned);
    }

    public static void tick(net.minecraft.client.Minecraft client) {
        if (!BetterMapsModClient.minimapEnabled) {
            posInitialized = false;
            return;
        }
        if (client.player == null || client.level == null) return;

        // Time
        INSTANCE.timeWithMinimap++;
        tickCounter++;

        // Distance
        double px = client.player.getX();
        double pz = client.player.getZ();
        if (posInitialized) {
            double dx   = px - lastX;
            double dz   = pz - lastZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 10.0) {
                INSTANCE.distanceTracked += (long) dist;
            }
        }
        lastX          = px;
        lastZ          = pz;
        posInitialized = true;

        // Biome
        var biomeHolder = client.level.getBiome(client.player.blockPosition());
        biomeHolder.unwrapKey().ifPresent(k -> {
            String full     = k.toString();
            int lastSlash   = full.lastIndexOf('/');
            int lastBracket = full.lastIndexOf(']');
            String path     = (lastSlash >= 0 && lastBracket > lastSlash)
                    ? full.substring(lastSlash + 1, lastBracket).trim() : full;
            int colon = path.indexOf(':');
            if (colon >= 0) path = path.substring(colon + 1);
            if (INSTANCE.biomesVisited.add(path)) {
                BetterMapsMod.LOGGER.info("New biome discovered: " + path
                        + " (total: " + INSTANCE.biomesVisited.size() + ")");
                save();
            }
        });

        // Save every 5 seconds
        if (tickCounter % 100 == 0) {
            save();
        }
    }

    public String getFormattedTime() {
        long secs  = INSTANCE.timeWithMinimap / 20;
        long mins  = secs / 60;
        long hours = mins / 60;
        if (hours > 0)  return hours + "h " + (mins % 60) + "m";
        if (mins  > 0)  return mins  + "m " + (secs % 60) + "s";
        return secs + "s";
    }

    public String getFormattedDistance() {
        long b = INSTANCE.distanceTracked;
        if (b >= 1000) return String.format("%.1fk blocks", b / 1000.0);
        return b + " blocks";
    }
}