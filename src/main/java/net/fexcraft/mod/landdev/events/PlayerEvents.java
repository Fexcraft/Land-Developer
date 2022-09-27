package net.fexcraft.mod.landdev.events;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;

@Mod.EventBusSubscriber
public class PlayerEvents {
    
    @SubscribeEvent
    public static void onPlayerLogin(PlayerLoggedInEvent event){
    	if(event.player.world.isRemote) return;
    	Player player = ResManager.getPlayer(event.player.getGameProfile().getId(), true);
		player.player = event.player;
		player.offline = false;
		player.login = Time.getDate();
    }
    
	@SubscribeEvent
	public static void onPlayerLogout(PlayerLoggedOutEvent event){
    	if(event.player.world.isRemote) return;
		Player player = ResManager.getPlayer(event.player.getGameProfile().getId(), false);
		if(player != null){
			player.save();
			player.last_login = player.login;
			player.last_logout = Time.getDate();
			player.login = 0;
			player.offline = true;
			player.player = null;
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerRespawnEvent event){
		Player player = ResManager.getPlayer(event.player.getGameProfile().getId(), false);
		if(player != null) player.player = event.player;
	}

}
