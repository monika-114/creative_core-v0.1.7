package dev.creationcore.mixin;

import dev.creationcore.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Allows Mine Craft to make progress on states whose vanilla destroy speed is -1. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    // With Netherite base speed 9 and Efficiency V's +26 mining-efficiency attribute, Player's
    // normal destroy speed is 35. 35 / 1400 = 0.025 progress/tick = 40 ticks to break.
    private static final float INDESTRUCTIBLE_PROGRESS_DIVISOR = 1400.0F;

    @Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
    private void creationcore$mineCraftBreaksIndestructible(Player player, BlockGetter level, BlockPos pos,
                                                             CallbackInfoReturnable<Float> cir) {
        BlockState state = (BlockState) (Object) this;
        if (!player.getMainHandItem().is(ModItems.MINE_CRAFT.get())) return;
        if (state.getDestroySpeed(level, pos) >= 0.0F) return;

        float playerSpeed = Math.max(0.0F, player.getDestroySpeed(state));
        cir.setReturnValue(playerSpeed / INDESTRUCTIBLE_PROGRESS_DIVISOR);
    }
}
