package net.meellowamg.bettermapsmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class BetterMapsConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("bettermapsmod.json");

    // Scale: 1.0 = default size, 2.0 = double size etc
    public float minimapScale = 1.0f;
    public int minimapMargin = 10;
    public float minimapOpacity = 1.0f;
    public String minimapPosition = "TOP_RIGHT";

    private static BetterMapsConfig instance;

    public static BetterMapsConfig get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                instance = GSON.fromJson(reader, BetterMapsConfig.class);
            } catch (IOException e) {
                BetterMapsMod.LOGGER.error("Failed to load config", e);
                instance = new BetterMapsConfig();
            }
        } else {
            instance = new BetterMapsConfig();
            save();
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(instance, writer);
        } catch (IOException e) {
            BetterMapsMod.LOGGER.error("Failed to save config", e);
        }
    }
}