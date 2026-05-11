package slippybanana333.me.alchemist_origin;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Alchemist_origin implements ModInitializer {

    public static final String MOD_ID = "alchemist_origin";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Alchemist Origin mod initialising…");

        // Register custom items (Alchemic Crystal, Pure Essence)
        AlchemistItems.register();

        // Register /alc command
        AlchemistCommand.register();

        // Apply permanent effect + liquid drawbacks once per second (every 20 ticks).
        // Gives 10 seconds (200 ticks) of effect on each refresh so it never expires.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTicks() % 20 != 0) return; // only run once per second
            for (net.minecraft.server.network.ServerPlayerEntity sp : server.getPlayerManager().getPlayerList()) {
                AlchemistEffectManager.onPlayerTick(sp);
            }
        });

        // Free in-memory state when a player leaves
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                AlchemistEffectManager.onPlayerDisconnect(handler.getPlayer())
        );

        LOGGER.info("Alchemist Origin mod ready.");
    }
}
