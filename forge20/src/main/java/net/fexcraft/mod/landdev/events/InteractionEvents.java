package net.fexcraft.mod.landdev.events;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.chunk.ChunkType;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.Protector;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.uni.UniEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "landdev", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InteractionEvents {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockBreak0(BlockEvent.BreakEvent event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		if(!control((Level)event.getLevel(), event.getPos(), event.getState(), event.getPlayer(), false)){
			UniEntity.getEntity(event.getPlayer()).bar(translate("interact.break.noperm"));
			event.setCanceled(true);
		}
		return;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockPlace(BlockEvent.EntityPlaceEvent event){
		if(event.getLevel().isClientSide()) return;
		if(event.getEntity() instanceof Player == false) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		if(!control((Level)event.getLevel(), event.getPos(), event.getState(), (Player)event.getEntity(), false)){
			UniEntity.getEntity(event.getEntity()).bar(translate("interact.place.noperm"));
			event.setCanceled(true);
		}
		return;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBlockPlace(BlockEvent.EntityMultiPlaceEvent event){
		if(event.getLevel().isClientSide()) return;
		if(event.getEntity() instanceof Player == false) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		if(!control((Level)event.getLevel(), event.getPos(), event.getState(), (Player)event.getEntity(), false)){
			UniEntity.getEntity(event.getEntity()).bar(translate("interact.place.noperm"));
			event.setCanceled(true);
		}
		return;
	}
	
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		if(event.getHand() == InteractionHand.OFF_HAND) return;
		BlockState state = event.getLevel().getBlockState(event.getPos());
		boolean check = state.getBlock() instanceof SignBlock == false && Protector.INSTANCE.isProtected(state);
		if(check && !control(event.getLevel(), event.getPos(), state, event.getEntity(), true)){
			UniEntity.getEntity(event.getEntity()).bar(translate("interact.interact.noperm"));
			event.setCanceled(true);
		}
	}

	private static boolean control(Level world, BlockPos pos, BlockState state, Player entity, boolean interact){
		LDPlayer player = ResManager.getPlayer(entity);
		if(player == null) return false;
		if(player.adm) return true;
		Chunk_ chunk = ResManager.getChunkS(pos.getX(), pos.getZ());
		if(chunk.district.id < 0){
			if(chunk.district.id == -1){
				return LDConfig.EDIT_WILDERNESS;
			}
			else if(chunk.district.id == -2){
				//TODO temporary claims
				return false;
			}
			else{
				player.entity.bar(translate("interact.control.unknown_district"));
				return false;
			}
		}
		return hasperm(chunk, player, interact);
	}

	private static boolean hasperm(Chunk_ chunk, LDPlayer player, boolean interact){
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
			player.entity.bar(translate("interact.control.unknown_chunk_type"));
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
