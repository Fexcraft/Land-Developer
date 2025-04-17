package net.fexcraft.mod.landdev.events;

import net.fexcraft.mod.landdev.util.Protector;
import net.fexcraft.mod.uni.UniEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import static net.fexcraft.mod.landdev.util.InteractHandler.control;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "landdev", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InteractionEvents {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockBreak0(BlockEvent.BreakEvent event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		if(!control(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getPlayer(), false)){
			UniEntity.getEntity(event.getPlayer()).bar("interact.break.noperm");
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event){
		if(event.getLevel().isClientSide()) return;
		if(event.getEntity() instanceof Player == false) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		if(!control(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getEntity(), false)){
			UniEntity.getEntity(event.getEntity()).bar("interact.place.noperm");
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockPlace(BlockEvent.EntityMultiPlaceEvent event){
		if(event.getLevel().isClientSide()) return;
		if(event.getEntity() instanceof Player == false) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		if(!control(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getEntity(), false)){
			UniEntity.getEntity(event.getEntity()).bar("interact.place.noperm");
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		if(event.getHand() == InteractionHand.OFF_HAND) return;
		BlockState state = event.getLevel().getBlockState(event.getPos());
		boolean check = state.getBlock() instanceof SignBlock == false && Protector.INSTANCE.isProtected(state);
		if(check && !control(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getEntity(), true)){
			UniEntity.getEntity(event.getEntity()).bar("interact.interact.noperm");
			event.setCanceled(true);
		}
	}

}
