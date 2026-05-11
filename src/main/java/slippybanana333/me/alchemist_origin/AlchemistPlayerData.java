package slippybanana333.me.alchemist_origin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

/**
 * Holds all Alchemist-related data for one player.
 *
 * unlockedSlots  0 = no effects, 1 = 1 effect, 2 = 2 effects, 3 = 3 effects.
 * Each slot stores the chosen AlchemistEffectType (or null if not yet chosen).
 *
 * Slot 0 is unlocked by Effect Upgrade I.
 * Slot 1 is unlocked by Effect Upgrade II.
 * Slot 2 is unlocked by Effect Upgrade III.
 */
public class AlchemistPlayerData {

    public static final int MAX_SLOTS = 3;

    private int unlockedSlots = 0;
    private final AlchemistEffectType[] chosenEffects = new AlchemistEffectType[MAX_SLOTS];

    // ── Slot management ────────────────────────────────────────────────────────
    public int     getUnlockedSlots() { return unlockedSlots; }
    public boolean canUnlockMore()    { return unlockedSlots < MAX_SLOTS; }
    public void    unlockNextSlot()   { if (canUnlockMore()) unlockedSlots++; }

    // ── Effect per slot ────────────────────────────────────────────────────────
    /** Returns the chosen effect for the given slot, or null if none / locked. */
    public AlchemistEffectType getEffect(int slot) {
        return (slot >= 0 && slot < MAX_SLOTS) ? chosenEffects[slot] : null;
    }

    /**
     * Assigns an effect to a slot.  Resets the level to 0 whenever the effect
     * changes, because the player is starting fresh with a new effect.
     */
    public void setEffect(int slot, AlchemistEffectType type) {
        if (slot >= 0 && slot < unlockedSlots) chosenEffects[slot] = type;
    }

    /** True if any unlocked slot has an effect chosen. */
    public boolean hasAnyActiveEffect() {
        for (int i = 0; i < unlockedSlots; i++) {
            if (chosenEffects[i] != null) return true;
        }
        return false;
    }

    // ── NBT ────────────────────────────────────────────────────────────────────
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Slots", unlockedSlots);
        NbtList names = new NbtList();
        for (int i = 0; i < MAX_SLOTS; i++) {
            names.add(NbtString.of(chosenEffects[i] != null ? chosenEffects[i].name() : ""));
        }
        nbt.put("EffectNames", names);
        return nbt;
    }

    public static AlchemistPlayerData fromNbt(NbtCompound nbt) {
        AlchemistPlayerData d = new AlchemistPlayerData();
        d.unlockedSlots = Math.min(nbt.getInt("Slots"), MAX_SLOTS);
        if (nbt.contains("EffectNames", NbtElement.LIST_TYPE)) {
            NbtList names = nbt.getList("EffectNames", NbtElement.STRING_TYPE);
            for (int i = 0; i < Math.min(names.size(), MAX_SLOTS); i++) {
                d.chosenEffects[i] = AlchemistEffectType.fromName(names.getString(i));
            }
        }
        return d;
    }
}
