package net.fexcraft.mod.landdev.mixin;

import net.fexcraft.mod.fcl.FCL;
import net.fexcraft.mod.uni.UniEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.fexcraft.mod.landdev.util.InteractHandler.control;

@Mixin(BlockItem.class)
public class BlockItemMixin extends Item {

	public BlockItemMixin(Properties properties){
		super(properties);
	}

	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/item/BlockItem;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", cancellable = true)
	public void _useOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> info){
		if(!context.getLevel().isClientSide() && context.getLevel() == FCL.SERVER.get().overworld()){
			if(!control(context.getClickedPos().getX(), context.getClickedPos().getY(), context.getClickedPos().getZ(), context.getPlayer(), false)){
				UniEntity.getEntity(context.getPlayer()).bar("interact.place.noperm");
				info.setReturnValue(InteractionResult.FAIL);
				info.cancel();
			}
		}
	}

}
