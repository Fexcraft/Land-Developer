package net.fexcraft.mod.landdev.util;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.landdev.data.player.Player;
import net.minecraft.entity.player.EntityPlayerMP;

public class Announcer {

	public static void announce(Target target, int id, String string, Object... objs){
		switch(target){
			case GLOBAL:
				Static.getServer().getPlayerList().getPlayers().forEach(player -> { announce(player, string, objs); });
				break;
			case LOCAL:
				break;
			case COMPANY:
				break;
			case DISTRICT:
				Static.getServer().getPlayerList().getPlayers().forEach(player -> {
					Player ply = ResManager.getPlayer(player);
					if(ply.isCurrentlyInDistrict(id)) announce(player, string, objs);
				});
				break;
			case MUNICIPALITY:
				Static.getServer().getPlayerList().getPlayers().forEach(player -> {
					Player ply = ResManager.getPlayer(player);
					if(ply.municipality.id == id || ply.isCurrentlyInMunicipality(id)) announce(player, string, objs);
				});
				break;
			case COUNTY:
				Static.getServer().getPlayerList().getPlayers().forEach(player -> {
					Player ply = ResManager.getPlayer(player);
					if(ply.county.id == id || ply.isCurrentlyInCounty(id)) announce(player, string, objs);
				});
				break;
			case STATE:
				Static.getServer().getPlayerList().getPlayers().forEach(player -> {
					Player ply = ResManager.getPlayer(player);
					if(ply.county.state.id == id || ply.isCurrentlyInState(id)) announce(player, string, objs);
				});
				break;
			default: return;
		}
	}
	
	
	public static void announce(EntityPlayerMP player, String string, Object[] objs){
		Print.chat(player, TranslationUtil.translate(string, objs));
	}


	public static enum Target {
		
		GLOBAL, LOCAL, DISTRICT, MUNICIPALITY, COUNTY, STATE, COMPANY
		
	}

}
