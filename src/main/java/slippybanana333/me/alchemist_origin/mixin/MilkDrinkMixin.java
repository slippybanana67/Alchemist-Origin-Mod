package slippybanana333.me.alchemist_origin.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MilkBucketItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slippybanana333.me.alchemist_origin.AlchemistEffectManager;

/**
 * Intercepts milk bucket consumption.
 *
 * For an active Alchemist player (one who has chosen an effect via /alc),
 * drinking milk instantly kills them instead of clearing status effects.
 *
 * Rationale: Milk purges all potion effects — the very foundation of the
 * Alchemist's power.  Consuming it is therefore lethal.
 *
 * Creative/spectator players are exempt so developers can test freely.
 */
@Mixin(MilkBucketItem.class)
public class MilkDrinkMixin {

    @Inject(method = "finishUsing", at = @At("HEAD"), cancellable = true)
    private void onMilkDrink(ItemStack stack, World world, LivingEntity user,
                             CallbackInfoReturnable<ItemStack> cir) {
        // Only runs on the server and only for Alchemist players in survival/adventure
        if (world.isClient) return;
        if (!(user instanceof ServerPlayerEntity player)) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (!AlchemistEffectManager.hasAnyActiveEffect(player)) return;

        // Warn the player, then apply lethal magic damage (bypasses armour)
        player.sendMessage(
                Text.literal("The milk purged your alchemy — and your life!")
                    .formatted(Formatting.DARK_RED),
                false
        );
        player.damage(world.getDamageSources().magic(), Float.MAX_VALUE);

        // Return the empty bucket that milk-drinking normally produces
        cir.setReturnValue(new ItemStack(Items.BUCKET));
    }
}

