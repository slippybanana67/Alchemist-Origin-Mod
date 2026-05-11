package slippybanana333.me.alchemist_origin.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slippybanana333.me.alchemist_origin.AlchemistEffectManager;

/**
 * Grants active Alchemist players immunity to all HARMFUL status effects.
 *
 * Targets: LivingEntity#addStatusEffect(StatusEffectInstance, Entity)
 *   — this is the single choke-point through which every status-effect
 *     application passes, whether from potions, mobs, beacons, or datapacks.
 *
 * Exception: effects that AlchemistEffectManager applies intentionally
 * (currently: Weakness while in water/lava) bypass this check via the
 * bypassImmunitySet so the intended drawback is still enforced.
 */
@Mixin(LivingEntity.class)
public class DebuffImmunityMixin {

    @Inject(
        method = "addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;Lnet/minecraft/entity/Entity;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void cancelHarmfulEffects(StatusEffectInstance effect, Entity source,
                                      CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        // Only care about server-side Alchemist players
        if (!(self instanceof ServerPlayerEntity player)) return;
        if (!AlchemistEffectManager.hasAnyActiveEffect(player)) return;

        // Allow effects that our own code is deliberately applying (e.g. water Weakness)
        if (AlchemistEffectManager.isBypassingImmunity(player)) return;

        // Block all HARMFUL effects
        if (effect.getEffectType().getCategory() == StatusEffectCategory.HARMFUL) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}

