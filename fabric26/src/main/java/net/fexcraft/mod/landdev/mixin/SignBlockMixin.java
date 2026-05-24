package net.fexcraft.mod.landdev.mixin;

import net.fexcraft.mod.fcl.FCL;
import net.fexcraft.mod.uni.UniEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.fexcraft.mod.landdev.util.InteractHandler.control;

@Mixin(SignBlock.class)
public abstract class SignBlockMixin extends BaseEntityBlock {

	public SignBlockMixin(Properties properties){
		super(properties);
	}

	@Inject(at = @At("HEAD"), method="Lnet/minecraft/world/level/block/SignBlock;openTextEdit(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/entity/SignBlockEntity;Z)V")
	protected void _open(Player player, SignBlockEntity sign, boolean isFrontText, CallbackInfo ci){
		if(!player.level().isClientSide() && player.level() == FCL.SERVER.get().overworld()){
			if(!control(sign.getBlockPos().getX(), sign.getBlockPos().getY(), sign.getBlockPos().getZ(), player, true)){
				UniEntity.getEntity(player).bar("landdev.interact.sign.noperm");
				ci.cancel();
			}
		}
	}

}
