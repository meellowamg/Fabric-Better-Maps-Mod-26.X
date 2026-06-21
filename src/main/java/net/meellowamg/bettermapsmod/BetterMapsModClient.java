package net.meellowamg.bettermapsmod;

import net.fabricmc.api.ClientModInitializer;

public class BetterMapsModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BetterMapsMod.LOGGER.info("Better Maps Mod Client Loaded!");
    }
}
