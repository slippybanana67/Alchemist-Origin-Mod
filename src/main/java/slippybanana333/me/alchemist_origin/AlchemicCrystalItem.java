package slippybanana333.me.alchemist_origin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/**
 * The Alchemic Crystal.
 *
 * Right-clicking consumes one crystal and unlocks the player's next Alchemist
 * effect slot (up to a maximum of AlchemistPlayerData.MAX_SLOTS = 3).
 *
 * Crafting recipe: see data/alchemist_origin/recipes/alchemic_crystal.json
 */
public class AlchemicCrystalItem extends Item {

    public AlchemicCrystalItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient) return TypedActionResult.pass(stack);
        if (!(user instanceof ServerPlayerEntity player)) return TypedActionResult.pass(stack);

        AlchemistPlayerData data = AlchemistEffectManager.getOrCreateData(player);

        if (data.canUnlockMore()) {
            data.unlockNextSlot();
            int newSlot = data.getUnlockedSlots();
            player.sendMessage(
                    Text.literal("⚗ Effect Slot " + newSlot + " unlocked!").formatted(Formatting.GOLD),
                    false
            );
            if (!player.isCreative()) stack.decrement(1);
            return TypedActionResult.success(stack);
        } else {
            player.sendMessage(
                    Text.literal("All " + AlchemistPlayerData.MAX_SLOTS + " effect slots are already unlocked!")
                        .formatted(Formatting.RED),
                    true
            );
            return TypedActionResult.fail(stack);
        }
    }
}

