package net.fexcraft.mod.landdev.events;

import static net.fexcraft.mod.landdev.util.InteractHandler.control;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.util.Protector;
import net.minecraft.block.BlockSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber
public class InteractionEvents {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockBreak0(BlockEvent.BreakEvent event){
		if(event.getWorld().isRemote || event.getPlayer().dimension != 0) return;
		if(!control(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getPlayer(), false)){
			Print.bar(event.getPlayer(), "landdev.interact.break.noperm");
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockPlace(BlockEvent.PlaceEvent event){
		if(event.getWorld().isRemote || event.getPlayer().dimension != 0){ return; }
		if(!control(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getPlayer(), false)){
			Print.bar(event.getPlayer(), "landdev.interact.place.noperm");
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
		if(event.getWorld().isRemote || event.getEntityPlayer().dimension != 0 || event.getEntityPlayer().getActiveHand() == EnumHand.OFF_HAND) return;
		IBlockState state = event.getWorld().getBlockState(event.getPos());
		boolean check = state.getBlock() instanceof BlockSign == false && Protector.INSTANCE.isProtected(state);
		if(check && !control(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getEntityPlayer(), true)){
			Print.bar(event.getEntityPlayer(), "landdev.interact.interact.noperm");
			event.setCanceled(true);
		}
	}

}
