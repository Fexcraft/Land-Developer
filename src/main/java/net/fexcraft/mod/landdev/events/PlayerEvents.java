package net.fexcraft.mod.landdev.events;

import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;
import static net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter.NO_INTERNAL;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.chunk.cap.ChunkCap;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;
import net.fexcraft.mod.landdev.util.TaxSystem;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.uni.world.MessageSenderI;
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
    	Player player = ResManager.getPlayer(event.player.getGameProfile().getId(), true);
		player.entity = event.player;
		player.sender = new MessageSenderI(player.entity);
		player.offline = false;
		player.login = Time.getDate();
		player.chunk_last = ResManager.getChunk(event.player);
		TaxSystem.taxPlayer(player, null, false);
		Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, translate("server.player_join", player.name_raw()));
    }
    
	@SubscribeEvent
	public static void onPlayerLogout(PlayerLoggedOutEvent event){
    	if(event.player.world.isRemote) return;
		Player player = ResManager.getPlayer(event.player.getGameProfile().getId(), false);
		if(player != null){
			Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, translate("server.player_left", player.name_raw()));
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
		Player player = ResManager.getPlayer(event.player.getGameProfile().getId(), false);
		if(player != null) player.entity = event.player;
	}
	
	private static long time;
	private static boolean moved, label;
	
	@SubscribeEvent
	public static void onTick(TickEvent.PlayerTickEvent event){
		if(event.player.world.isRemote || event.player.dimension != 0) return;
		Player player = ResManager.getPlayer(event.player);
		if((time = Time.getDate()) > player.last_pos_update){
			player.last_pos_update = time;
			player.chunk_last = player.chunk_current;
			player.chunk_current = event.player.world.getChunk(event.player.getPosition()).getCapability(ChunkCap.CHUNK, null).getChunk();
			if(player.chunk_current == null) return;
			if(player.chunk_last == null) player.chunk_last = player.chunk_current;
			moved = player.chunk_current.district.id != player.chunk_last.district.id;
			label = player.chunk_current.label.present && player.chunk_current != player.chunk_last;
			if(moved || label) player.sendLocationUpdate(moved, label, 0);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onMessage(ServerChatEvent event){
		Player player = ResManager.getPlayer(event.getPlayer());
		Broadcaster.send(player, event.getMessage());
		event.setCanceled(Settings.CHAT_OVERRIDE);
	}

}
