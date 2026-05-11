package slippybanana333.me.alchemist_origin;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Represents every potion effect the Alchemist can permanently channel.
 *
 * amplifier is 0-based (0 = level I, 1 = level II, 3 = level IV, etc.)
 * icon   is the item displayed in the selection GUI for that effect.
 */
public enum AlchemistEffectType {

    SPEED           ("Speed",              StatusEffects.SPEED,              0, Items.SUGAR),
    STRENGTH        ("Strength II",        StatusEffects.STRENGTH,           1, Items.BLAZE_POWDER),
    DOLPHINS_GRACE  ("Dolphin's Grace",    StatusEffects.DOLPHINS_GRACE,     0, Items.HEART_OF_THE_SEA),
    HERO_OF_THE_VILLAGE("Hero of the Village III", StatusEffects.HERO_OF_THE_VILLAGE, 2, Items.EMERALD),
    REGENERATION    ("Regeneration",       StatusEffects.REGENERATION,       0, Items.GHAST_TEAR),
    HASTE           ("Haste",             StatusEffects.HASTE,              0, Items.GOLDEN_PICKAXE),
    FIRE_RESISTANCE ("Fire Resistance",    StatusEffects.FIRE_RESISTANCE,    0, Items.MAGMA_CREAM),
    SATURATION      ("Saturation",         StatusEffects.SATURATION,         0, Items.COOKED_BEEF);

    public final String      displayName;
    public final StatusEffect effect;
    public final int          amplifier;   // 0-based; Strength II → 1, Hero IV → 3
    public final Item         icon;

    AlchemistEffectType(String displayName, StatusEffect effect, int amplifier, Item icon) {
        this.displayName = displayName;
        this.effect      = effect;
        this.amplifier   = amplifier;
        this.icon        = icon;
    }

    /** Builds the ItemStack that represents this effect in the GUI. */
    public ItemStack createGuiItem() {
        ItemStack stack = new ItemStack(icon);
        stack.setCustomName(Text.literal(displayName).formatted(Formatting.GOLD));

        // Write lore line using raw NBT so it survives round-trips
        NbtCompound display = stack.getOrCreateSubNbt("display");
        NbtList lore = new NbtList();
        lore.add(NbtString.of(Text.Serializer.toJson(
                Text.literal("Click to select").formatted(net.minecraft.util.Formatting.GRAY)
        )));
        display.put("Lore", lore);
        return stack;
    }

    /** Deserialises from the enum constant name stored in NBT. Returns null if unknown. */
    public static AlchemistEffectType fromName(String name) {
        if (name == null || name.isEmpty()) return null;
        for (AlchemistEffectType t : values()) {
            if (t.name().equals(name)) return t;
        }
        return null;
    }
}

