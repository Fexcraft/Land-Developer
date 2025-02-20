package net.fexcraft.mod.landdev.util;

import net.fexcraft.mod.uni.world.MessageSender;

public class Announcer {

	public static void announce(Target target, int id, String string, Object... objs){
		switch(target){
			case GLOBAL:
				//TODO Static.getServer().getPlayerList().getPlayers().forEach(player -> { announce(UniEntity.getEntity(player), string, objs); });
				break;
			case LOCAL:
				break;
			case COMPANY:
				break;
			case DISTRICT:
				/*Static.getServer().getPlayerList().getPlayers().forEach(player -> {
					LDPlayer ply = ResManager.getPlayer(player.getGameProfile().getId(), true);
					if(ply.isCurrentlyInDistrict(id)) announce(ply.entity, string, objs);
				});*/
				break;
			case MUNICIPALITY:
				/*Static.getServer().getPlayerList().getPlayers().forEach(player -> {
					LDPlayer ply = ResManager.getPlayer(player.getGameProfile().getId(), true);
					if(ply.municipality.id == id || ply.isCurrentlyInMunicipality(id)) announce(ply.entity, string, objs);
				});*/
				break;
			case COUNTY:
				/*Static.getServer().getPlayerList().getPlayers().forEach(player -> {
					LDPlayer ply = ResManager.getPlayer(player.getGameProfile().getId(), true);
					if(ply.county.id == id || ply.isCurrentlyInCounty(id)) announce(ply.entity, string, objs);
				});*/
				break;
			case REGION:
				/*Static.getServer().getPlayerList().getPlayers().forEach(player -> {
					LDPlayer ply = ResManager.getPlayer(player.getGameProfile().getId(), true);
					if(ply.county.region.id == id || ply.isCurrentlyInRegion(id)) announce(ply.entity, string, objs);
				});*/
				break;
			default: return;
		}
	}
	
	
	public static void announce(MessageSender player, String string, Object[] objs){
		player.send(TranslationUtil.translate(string, objs));
	}


	public static enum Target {
		
		GLOBAL, LOCAL, DISTRICT, MUNICIPALITY, COUNTY, REGION, COMPANY
		
	}

}
