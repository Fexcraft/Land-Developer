package net.fexcraft.mod.landdev.events;

import static net.fexcraft.mod.landdev.util.InteractHandler.control;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.data.chunk.ChunkType;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.Protector;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.block.BlockSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
			Print.bar(event.getPlayer(), translate("interact.break.noperm"));
			event.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockPlace(BlockEvent.PlaceEvent event){
		if(event.getWorld().isRemote || event.getPlayer().dimension != 0){ return; }
		if(!control(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getPlayer(), false)){
			Print.bar(event.getPlayer(), translate("interact.place.noperm"));
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
		if(event.getWorld().isRemote || event.getEntityPlayer().dimension != 0 || event.getEntityPlayer().getActiveHand() == EnumHand.OFF_HAND) return;
		IBlockState state = event.getWorld().getBlockState(event.getPos());
		boolean check = state.getBlock() instanceof BlockSign == false && Protector.INSTANCE.isProtected(state);
		if(check && !control(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getEntityPlayer(), true)){
			Print.bar(event.getEntityPlayer(), translate("interact.interact.noperm"));
			event.setCanceled(true);
		}
	}

}
