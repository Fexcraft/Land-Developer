package net.fexcraft.mod.landdev.events;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.chunk.ChunkApp;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.TaxSystem;
import net.fexcraft.mod.landdev.util.broad.BroadcastChannel;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.uni.UniChunk;
import net.fexcraft.mod.uni.UniEntity;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;
import static net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter.NO_INTERNAL;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "landdev", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEvents {
    
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event){
    	if(event.getEntity().level().isClientSide) return;
    	LDPlayer player = ResManager.getPlayer(event.getEntity().getGameProfile().getId(), true);
		player.entity = UniEntity.getEntity(event.getEntity());
		player.offline = false;
		player.login = Time.getDate();
		player.chunk_last = ResManager.getChunkP(event.getEntity());
		TaxSystem.taxPlayer(player, null, false);
		Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, LDConfig.SERVLANG_JOINED.formatted(player.name_raw()));
    }
    
	@SubscribeEvent
	public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
    	if(event.getEntity().level().isClientSide) return;
		LDPlayer player = ResManager.getPlayer(event.getEntity().getGameProfile().getId(), false);
		if(player != null){
			Broadcaster.send(NO_INTERNAL, BroadcastChannel.SERVER, null, LDConfig.SERVLANG_LEFT.formatted(player.name_raw()));
			player.save();
			player.last_login = player.login;
			player.last_logout = Time.getDate();
			player.login = 0;
			player.offline = true;
			player.entity = null;
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event){
		if(event.getEntity().level().isClientSide) return;
		LDPlayer player = ResManager.getPlayer(event.getEntity().getGameProfile().getId(), false);
		if(player != null) player.entity = UniEntity.getEntity(event.getEntity());
	}
	
	private static long time;
	private static boolean moved, label;
	
	@SubscribeEvent
	public static void onTick(TickEvent.PlayerTickEvent event){
		if(event.player.level().isClientSide) return;
		if(event.player.level() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		LDPlayer player = ResManager.getPlayer(event.player);
		if((time = Time.getDate()) > player.last_pos_update){
			player.last_pos_update = time;
			player.chunk_last = player.chunk_current;
			player.chunk_current = UniChunk.get(event.player.level().getChunk(event.player.blockPosition())).getApp(ChunkApp.class).chunk;
			if(player.chunk_current == null) return;
			if(player.chunk_last == null) player.chunk_last = player.chunk_current;
			moved = player.chunk_current.district.id != player.chunk_last.district.id;
			label = player.chunk_current.label.present && player.chunk_current != player.chunk_last;
			if(moved || label) player.sendLocationUpdate(moved, label, 0);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onMessage(ServerChatEvent event){
		LDPlayer player = ResManager.getPlayer(event.getPlayer());
		Broadcaster.send(player, event.getMessage().getString());
		event.setCanceled(LDConfig.CHAT_OVERRIDE);
	}

}
