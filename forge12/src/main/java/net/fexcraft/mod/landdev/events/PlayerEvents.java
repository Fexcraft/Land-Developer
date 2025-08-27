package net.fexcraft.mod.landdev.events;

import static net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter.NO_INTERNAL;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.chunk.ChunkApp;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.event.LDEvent;
import net.fexcraft.mod.landdev.event.PlayerLocationEvent;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.TaxSystem;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.uni.UniChunk;
import net.fexcraft.mod.uni.UniEntity;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public class PlayerEvents {
    
    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event){
    	if(event.player.world.isRemote) return;
    	LDPlayer player = ResManager.getPlayer(event.player.getGameProfile().getId(), true);
		player.entity = UniEntity.getEntity(event.player);
		player.offline = false;
		player.login = Time.getDate();
		player.chunk_last = ResManager.getChunkP(event.player);
		TaxSystem.taxPlayer(player, null, false);
		Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, "landdev.server.player_join", player.name_raw());
    }
    
	@SubscribeEvent
	public static void onPlayerLogout(PlayerLoggedOutEvent event){
    	if(event.player.world.isRemote) return;
		LDPlayer player = ResManager.getPlayer(event.player.getGameProfile().getId(), false);
		if(player != null){
			Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, "landdev.server.player_left", player.name_raw());
			player.save();
			player.last_login = player.login;
			player.last_logout = Time.getDate();
			player.login = 0;
			player.offline = true;
			player.entity = null;
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerRespawnEvent event){
		LDPlayer player = ResManager.getPlayer(event.player.getGameProfile().getId(), false);
		if(player != null) player.entity = UniEntity.getEntity(event.player);
	}
	
	private static long time;
	private static boolean moved, label;
	
	@SubscribeEvent
	public static void onTick(TickEvent.PlayerTickEvent event){
		if(event.player.world.isRemote || event.player.dimension != 0) return;
		LDPlayer player = ResManager.getPlayer(event.player);
		if((time = Time.getDate()) > player.last_pos_update){
			player.last_pos_update = time;
			player.chunk_last = player.chunk_current;
			player.chunk_current = UniChunk.get(event.player.world.getChunk(event.player.getPosition())).getApp(ChunkApp.class).chunk;
			if(player.chunk_current == null) return;
			if(player.chunk_last == null) player.chunk_last = player.chunk_current;
			moved = player.chunk_current.district.id != player.chunk_last.district.id;
			label = player.chunk_current.label.present && player.chunk_current != player.chunk_last;
			if(moved || label){
				if(LDConfig.RUN_LOCATION_EVENT) LDEvent.run(new PlayerLocationEvent(player, !moved && label));
				player.sendLocationUpdate(moved, label, 0);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onMessage(ServerChatEvent event){
		LDPlayer player = ResManager.getPlayer(event.getPlayer());
		Broadcaster.send(player, event.getMessage());
		event.setCanceled(LDConfig.CHAT_OVERRIDE);
	}

}
