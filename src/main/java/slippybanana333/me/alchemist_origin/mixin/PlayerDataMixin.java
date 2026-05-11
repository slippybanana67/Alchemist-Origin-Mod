package slippybanana333.me.alchemist_origin.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slippybanana333.me.alchemist_origin.AlchemistEffectManager;

@Mixin(ServerPlayerEntity.class)
public class PlayerDataMixin {

    private static final String NBT_KEY = "AlchemistData";

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void saveAlchemistData(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        nbt.put(NBT_KEY, AlchemistEffectManager.getSaveNbt(player));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void loadAlchemistData(NbtCompound nbt, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        if (nbt.contains(NBT_KEY)) {
            AlchemistEffectManager.loadData(player, nbt.getCompound(NBT_KEY));
        }
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyAlchemistData(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        if (alive) {
            AlchemistEffectManager.copyData(oldPlayer, (ServerPlayerEntity)(Object)this);
        }
    }
}
