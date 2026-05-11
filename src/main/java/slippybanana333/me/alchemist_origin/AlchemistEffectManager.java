package slippybanana333.me.alchemist_origin;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Central manager for all Alchemist player state.
 *
 * Stores an AlchemistPlayerData per player UUID.
 * Every second (gated in Alchemist_origin.onInitialize) onPlayerTick() refreshes
 * all active effects with 10-second windows and applies water/lava drawbacks.
 */
public class AlchemistEffectManager {

    private static final Map<UUID, AlchemistPlayerData> playerData = new HashMap<>();

    /** UUIDs whose next addStatusEffect call should bypass debuff immunity. */
    private static final Set<UUID> bypassImmunitySet = new HashSet<>();

    /** Each per-second refresh gives 3 seconds (60 ticks) of duration. */
    private static final int REFRESH_DURATION = 60;

    // ── Tick ──────────────────────────────────────────────────────────────────

    public static void onPlayerTick(ServerPlayerEntity player) {
        AlchemistPlayerData data = playerData.get(player.getUuid());
        if (data == null) return;

        boolean inWater = player.isTouchingWater();
        boolean inLava  = player.isInLava();

        boolean anyActive = false;
        for (int i = 0; i < data.getUnlockedSlots(); i++) {
            AlchemistEffectType type = data.getEffect(i);
            if (type == null) continue;
            anyActive = true;

            if (inWater && type != AlchemistEffectType.DOLPHINS_GRACE) {
                player.removeStatusEffect(type.effect);
                continue;
            }
            if (inLava && type != AlchemistEffectType.FIRE_RESISTANCE) {
                player.removeStatusEffect(type.effect);
                continue;
            }

            player.addStatusEffect(new StatusEffectInstance(
                    type.effect, REFRESH_DURATION, type.amplifier, false, false
            ));
        }

        if (anyActive && (inWater || inLava)) {
            player.addExhaustion(0.5f);
            bypassImmunitySet.add(player.getUuid());
            try {
                player.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.WEAKNESS, 80, 0, false, false
                ));
            } finally {
                bypassImmunitySet.remove(player.getUuid());
            }
        }
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    public static AlchemistPlayerData getOrCreateData(ServerPlayerEntity player) {
        return playerData.computeIfAbsent(player.getUuid(), k -> new AlchemistPlayerData());
    }

    public static boolean hasAnyActiveEffect(ServerPlayerEntity player) {
        AlchemistPlayerData data = playerData.get(player.getUuid());
        return data != null && data.hasAnyActiveEffect();
    }

    public static boolean isBypassingImmunity(ServerPlayerEntity player) {
        return bypassImmunitySet.contains(player.getUuid());
    }

    // ── NBT persistence (called from PlayerDataMixin) ─────────────────────────

    public static void loadData(ServerPlayerEntity player, NbtCompound nbt) {
        playerData.put(player.getUuid(), AlchemistPlayerData.fromNbt(nbt));
    }

    public static NbtCompound getSaveNbt(ServerPlayerEntity player) {
        return getOrCreateData(player).toNbt();
    }

    public static void copyData(ServerPlayerEntity from, ServerPlayerEntity to) {
        AlchemistPlayerData src = playerData.get(from.getUuid());
        playerData.put(to.getUuid(),
                src != null ? AlchemistPlayerData.fromNbt(src.toNbt()) : new AlchemistPlayerData());
    }

    public static void resetData(ServerPlayerEntity player) {
        playerData.put(player.getUuid(), new AlchemistPlayerData());
    }

    public static void onPlayerDisconnect(ServerPlayerEntity player) {
        bypassImmunitySet.remove(player.getUuid());
    }
}
