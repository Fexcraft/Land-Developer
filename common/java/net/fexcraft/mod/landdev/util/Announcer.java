package net.fexcraft.mod.landdev.util;

import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.world.MessageSender;
import net.fexcraft.mod.uni.world.WrapperHolder;

public class Announcer {

	public static void announce(Target target, int id, String string, Object... objs){
		switch(target){
			case GLOBAL:
				for(UniEntity player : WrapperHolder.getPlayers()){
					announce(player.entity, string, objs);
				}
				break;
			case LOCAL:
				break;
			case COMPANY:
				break;
			case DISTRICT:
				for(UniEntity player : WrapperHolder.getPlayers()){
					LDPlayer ply = ResManager.getPlayer(player);
					if(ply.isCurrentlyInDistrict(id)) announce(ply.entity, string, objs);
				}
				break;
			case MUNICIPALITY:
				for(UniEntity player : WrapperHolder.getPlayers()){
					LDPlayer ply = ResManager.getPlayer(player);
					if(ply.municipality.id == id || ply.isCurrentlyInMunicipality(id)) announce(ply.entity, string, objs);
				}
				break;
			case COUNTY:
				for(UniEntity player : WrapperHolder.getPlayers()){
					LDPlayer ply = ResManager.getPlayer(player);
					if(ply.county.id == id || ply.isCurrentlyInCounty(id)) announce(ply.entity, string, objs);
				}
				break;
			case REGION:
				for(UniEntity player : WrapperHolder.getPlayers()){
					LDPlayer ply = ResManager.getPlayer(player);
					if(ply.county.region.id == id || ply.isCurrentlyInRegion(id)) announce(ply.entity, string, objs);
				}
				break;
			default: return;
		}
	}
	
	
	public static void announce(MessageSender player, String string, Object[] objs){
		player.send("landdev." + string, objs);
	}


	public static enum Target {
		
		GLOBAL, LOCAL, DISTRICT, MUNICIPALITY, COUNTY, REGION, COMPANY
		
	}

}
