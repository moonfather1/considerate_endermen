package moonfather.endermen.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets="net.minecraft.world.entity.monster.EnderMan$EndermanTakeBlockGoal")
public class EndermanDecisionManager
{

	@WrapOperation(
			method = "tick()V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
	)
	private BlockState fixTick2(Level levelInstance, BlockPos pos, Operation<BlockState> original, @Cancellable CallbackInfo ci)
	{
		if (ci.isCancelled()) { return Blocks.AIR.defaultBlockState(); }
		BlockState result = original.call(levelInstance, pos); //  <- this is the call we are wrapping around.

		// first, let's see if the block is one that enderman may pickup.
		// mojang in their code checks line-of-sight regardless of whether the block is in allowed tag. cancelling this will prevent that.
		if (result.isAir() || ! result.is(BlockTags.ENDERMAN_HOLDABLE))
		{
			ci.cancel();
			return result;
		}

		// ok, so block is dirt or similar.
		// now the main thing - if there is at least one free space north/south of the block, and at least one east/west of the block, we allow pickup.
		// if not, we'll help up the big guy again by trying the block to the side, if eligible.
		BlockPos.MutableBlockPos pos2 = new BlockPos.MutableBlockPos();
		pos2.set(pos);
		BlockState adjacent;
		boolean negXEmpty = false, posXEmpty = false, negZEmpty = false, posZEmpty = false; // adjacent blocks
		pos2.move(-1, 0, 0);
		adjacent = levelInstance.getBlockState(pos2);
		negXEmpty = adjacent.canBeReplaced() || adjacent.getCollisionShape(levelInstance, pos).isEmpty();
		pos2.move(+2, 0, 0);
		adjacent = levelInstance.getBlockState(pos2);
		posXEmpty = adjacent.canBeReplaced() || adjacent.getCollisionShape(levelInstance, pos).isEmpty();
		pos2.move(-1, 0, -1);
		adjacent = levelInstance.getBlockState(pos2);
		negZEmpty = adjacent.canBeReplaced() || adjacent.getCollisionShape(levelInstance, pos).isEmpty();
		pos2.move(0, 0, +2);
		adjacent = levelInstance.getBlockState(pos2);
		posZEmpty = adjacent.canBeReplaced() || adjacent.getCollisionShape(levelInstance, pos).isEmpty();
		if ((posXEmpty || negXEmpty) && (posZEmpty || negZEmpty))
		{
			// let him have it.
			return result;      // exit.  vanilla code will check line-of-sight and do the pickup.
		}
		// no clearance on sides? cancel the rest of tick method
		ci.cancel();
		System.out.println("~~~~blocked");
		return result;
	}


	// that is that.
	// we prevented pickup if our condition isn't met and maybe scored some tiny performance gain because
	// ...vanilla code checks both the block tag, and whether enderman can actually reach this block that was randomly picked.
	//
	// next idea was to help them out by checking adjacent blocks and substituting a valid nearby block instead of one we
	// ...rejected but that would require changing values of five local variables.  that mixin would break easily.... in theory.
	// ...in practice, seeing how long mojang is leaving that spaghetti intact, we'd likely be fine for many years.

	// other parts of target class: there is ** canUse ** method in the same class.
	// if enderman has nothing in hands and mob griefing is allowed, it rolls a random with 10% chance.
	// we won't change that for now.  maybe after experiencing real live gameplay.
}