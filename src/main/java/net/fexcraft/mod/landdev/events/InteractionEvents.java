package net.fexcraft.mod.landdev.events;

import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.data.chunk.ChunkType;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.Protector;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;
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
	
	private static String erpfx = Formatter.PARAGRAPH_SIGN + "c";
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockBreak0(BlockEvent.BreakEvent event){
		if(event.getWorld().isRemote || event.getPlayer().dimension != 0) return;
		if(!control(event.getWorld(), event.getPos(), event.getState(), event.getPlayer(), false)){
			Print.bar(event.getPlayer(), erpfx + translate("interact.break.noperm"));
			event.setCanceled(true);
		}
		return;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockPlace(BlockEvent.PlaceEvent event){
		if(event.getWorld().isRemote || event.getPlayer().dimension != 0){ return; }
		if(!control(event.getWorld(), event.getPos(), event.getState(), event.getPlayer(), false)){
			Print.bar(event.getPlayer(), erpfx + translate("interact.place.noperm"));
			event.setCanceled(true);
		}
		return;
	}
	
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
		if(event.getWorld().isRemote || event.getEntityPlayer().dimension != 0 || event.getEntityPlayer().getActiveHand() == EnumHand.OFF_HAND) return;
		IBlockState state = event.getWorld().getBlockState(event.getPos());
		boolean check = state.getBlock() instanceof BlockSign == false && Protector.INSTANCE.isProtected(state);
		if(check && !control(event.getWorld(), event.getPos(), state, event.getEntityPlayer(), true)){
			Print.bar(event.getEntityPlayer(), erpfx + translate("interact.interact.noperm"));
			event.setCanceled(true);
			return;
		}
		else return;
	}

	private static boolean control(World world, BlockPos pos, IBlockState state, EntityPlayer entity, boolean interact){
		Player player = ResManager.getPlayer(entity);
		if(player.adm) return true;
		Chunk_ chunk = ResManager.getChunk(pos);
		if(chunk.district.id < 0){
			if(chunk.district.id == -1){
				return Settings.EDIT_WILDERNESS;
			}
			else if(chunk.district.id == -2){
				//TODO temporary claims
				return false;
			}
			else{
				Print.bar(player.entity, erpfx + translate("interact.control.unknown_district"));
				return false;
			}
		}
		return hasperm(chunk, player, interact);
	}

	private static boolean hasperm(Chunk_ chunk, Player player, boolean interact){
		if(interact && chunk.access.interact) return true;
		ChunkType type = chunk.owner.unowned ? ChunkType.NORMAL : chunk.type;
		boolean pass = false;
		switch(type){
		case NORMAL:
			pass = chunk.district.owner.isPartOf(player);
			break;
		case PRIVATE:
			if(chunk.owner.playerchunk){
				pass = player.uuid.equals(chunk.owner.player);
			}
			else{
				switch(chunk.owner.owner){
				case COMPANY:
					//TODO
					break;
				case COUNTY:
					pass = chunk.district.owner.manageable().isManager(player.uuid);
					break;
				case DISTRICT:
					pass = chunk.district.manage.isManager(player.uuid) || chunk.district.owner.manageable().isManager(player.uuid);
					break;
				case MUNICIPALITY:
					pass = chunk.district.owner.manageable().isManager(player.uuid);
					break;
				case STATE:
					pass = chunk.district.state().manage.isManager(player.uuid);
					break;
				default:
					break;
				}
			}
			break;
		case PUBLIC:
			pass = true;
			break;
		case RESTRICTED:
			switch(chunk.owner.owner){
			case COMPANY:
				//TODO
				break;
			case COUNTY:
				pass = chunk.district.owner.manageable().isStaff(player.uuid);
				break;
			case DISTRICT:
				pass = chunk.district.manage.isManager(player.uuid) || chunk.district.owner.manageable().isStaff(player.uuid);
				break;
			case MUNICIPALITY:
				pass = chunk.district.owner.manageable().isStaff(player.uuid);
				break;
			case STATE:
				pass = chunk.district.state().manage.isStaff(player.uuid);
				break;
			default:
				break;
			}
			break;
		default:
			Print.bar(player.entity, erpfx + translate("interact.control.unknown_chunk_type"));
			return false;
		
		}
		if(!pass){
			Long l = chunk.access.players.get(player.uuid);
			if(l != null){
				if(Time.getDate() < l) return true;
				else{
					chunk.access.players.remove(player.uuid);
				}
			}
			//TODO company check
		}
		return pass;
	}

}
