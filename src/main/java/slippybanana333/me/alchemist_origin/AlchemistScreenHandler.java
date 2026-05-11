package slippybanana333.me.alchemist_origin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * 3-row chest GUI — one row per effect slot.
 *
 * Progression:
 *   Effect Upgrade I   → unlocks row 0 (1 simultaneous effect)
 *   Effect Upgrade II  → unlocks row 1 (2 simultaneous effects)
 *   Effect Upgrade III → unlocks row 2 (3 simultaneous effects)
 *
 * Layout per row:
 *   Col 0-7  → 8 effect choices (grey if locked)
 *   Col 8    → unlock button (only on next-to-unlock row), or empty for active rows
 */
public class AlchemistScreenHandler extends GenericContainerScreenHandler {

    private static final int ROWS = 3;
    private static final int COLS = 9;

    private final SimpleInventory     guiInventory;
    private final AlchemistPlayerData data;
    private final ServerPlayerEntity  owner;

    public AlchemistScreenHandler(int syncId, PlayerInventory playerInventory,
                                  AlchemistPlayerData data) {
        this(syncId, playerInventory, data, new SimpleInventory(ROWS * COLS));
    }

    private AlchemistScreenHandler(int syncId, PlayerInventory playerInventory,
                                   AlchemistPlayerData data, SimpleInventory inv) {
        super(ScreenHandlerType.GENERIC_9X3, syncId, playerInventory, inv, ROWS);
        this.guiInventory = inv;
        this.data  = data;
        this.owner = (ServerPlayerEntity) playerInventory.player;
        populateGui();
    }

    // ── GUI building ──────────────────────────────────────────────────────────

    private void populateGui() {
        AlchemistEffectType[] effects = AlchemistEffectType.values();

        for (int row = 0; row < ROWS; row++) {
            boolean unlocked       = row < data.getUnlockedSlots();
            boolean isNextToUnlock = row == data.getUnlockedSlots();

            for (int col = 0; col < COLS; col++) {
                int idx = row * COLS + col;

                if (!unlocked) {
                    if (col == 8 && isNextToUnlock) {
                        // Unlock button for the very next locked slot
                        guiInventory.setStack(idx, makeUnlockButton(row));
                    } else {
                        String hint = isNextToUnlock
                                ? "Click ► to unlock with " + upgradeName(row)
                                : "Unlock Slot " + (row) + " first";
                        guiInventory.setStack(idx, makePane(Items.GRAY_STAINED_GLASS_PANE,
                                "Slot " + (row + 1) + " — Locked", Formatting.DARK_GRAY, hint));
                    }
                } else if (col < effects.length) {
                    // Effect choice
                    AlchemistEffectType type   = effects[col];
                    boolean             active = type == data.getEffect(row);
                    guiInventory.setStack(idx, makeEffectItem(type, active));
                } else {
                    // col 8, unlocked row → empty
                    guiInventory.setStack(idx, ItemStack.EMPTY);
                }
            }
        }
    }

    // ── Button builders ───────────────────────────────────────────────────────

    private ItemStack makeUnlockButton(int row) {
        boolean has = hasItem(upgradeItem(row));
        return makePane(
                has ? Items.LIME_STAINED_GLASS_PANE : Items.YELLOW_STAINED_GLASS_PANE,
                "► Unlock Slot " + (row + 1),
                has ? Formatting.GREEN : Formatting.YELLOW,
                has ? "Click — uses 1 " + upgradeName(row) : "Need 1 " + upgradeName(row));
    }

    private static ItemStack makeEffectItem(AlchemistEffectType type, boolean active) {
        ItemStack stack = new ItemStack(type.icon);
        String prefix = active ? "► " : "";
        stack.setCustomName(Text.literal(prefix + type.displayName)
                .formatted(active ? Formatting.GOLD : Formatting.WHITE));
        addLore(stack, active ? "Currently active" : "Click to select");
        return stack;
    }

    // ── Interaction ───────────────────────────────────────────────────────────

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex < 0 || slotIndex >= ROWS * COLS) return;

        int row = slotIndex / COLS;
        int col = slotIndex % COLS;

        if (row >= data.getUnlockedSlots()) {
            // Handle unlock button on the next-to-unlock row
            if (col == 8 && row == data.getUnlockedSlots()) {
                if (consumeItem(upgradeItem(row))) {
                    data.unlockNextSlot();
                    owner.sendMessage(Text.literal("Slot " + (row + 1) + " unlocked! Choose your effect.")
                            .formatted(Formatting.GREEN), true);
                    populateGui();
                } else {
                    owner.sendMessage(Text.literal("Need 1 " + upgradeName(row) + "!")
                            .formatted(Formatting.RED), true);
                }
            }
            return;
        }

        if (col < AlchemistEffectType.values().length) {
            // Effect selection
            AlchemistEffectType chosen = AlchemistEffectType.values()[col];
            data.setEffect(row, chosen);
            owner.sendMessage(Text.literal("Slot " + (row + 1) + ": ")
                    .formatted(Formatting.GRAY)
                    .append(Text.literal(chosen.displayName).formatted(Formatting.GOLD)), true);
            populateGui();
        }
        // col 8 on an unlocked row → do nothing
    }

    @Override public ItemStack quickMove(PlayerEntity player, int slot) { return ItemStack.EMPTY; }
    @Override public boolean canUse(PlayerEntity player) { return true; }

    // ── Per-slot upgrade item mapping ─────────────────────────────────────────

    private static Item upgradeItem(int slot) {
        return switch (slot) {
            case 0 -> AlchemistItems.UPGRADE_1;
            case 1 -> AlchemistItems.UPGRADE_2;
            default -> AlchemistItems.UPGRADE_3;
        };
    }

    private static String upgradeName(int slot) {
        return switch (slot) {
            case 0 -> "Effect Upgrade I";
            case 1 -> "Effect Upgrade II";
            default -> "Effect Upgrade III";
        };
    }

    // ── Inventory helpers ─────────────────────────────────────────────────────

    private boolean hasItem(Item item) {
        for (int i = 0; i < owner.getInventory().size(); i++) {
            if (owner.getInventory().getStack(i).isOf(item)) return true;
        }
        return false;
    }

    private boolean consumeItem(Item item) {
        for (int i = 0; i < owner.getInventory().size(); i++) {
            ItemStack s = owner.getInventory().getStack(i);
            if (s.isOf(item)) { s.decrement(1); return true; }
        }
        return false;
    }

    // ── Static helpers ────────────────────────────────────────────────────────

    private static ItemStack makePane(Item item, String name, Formatting color, String... lore) {
        ItemStack stack = new ItemStack(item);
        stack.setCustomName(Text.literal(name).formatted(color));
        if (lore.length > 0) addLore(stack, lore);
        return stack;
    }

    private static void addLore(ItemStack stack, String... lines) {
        NbtCompound display = stack.getOrCreateSubNbt("display");
        NbtList list = new NbtList();
        for (String line : lines) {
            list.add(NbtString.of(Text.Serializer.toJson(
                    Text.literal(line).formatted(Formatting.GRAY))));
        }
        display.put("Lore", list);
    }
}
