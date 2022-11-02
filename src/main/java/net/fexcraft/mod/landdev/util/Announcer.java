package net.fexcraft.mod.landdev.util;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.minecraft.entity.player.EntityPlayerMP;

public class Announcer {

	public static void announce(Target target, int id, String string, Object... objs){
		switch(target){
			case COMPANY:
				break;
			case COUNTY:
				break;
			case DISTRICT:
				break;
			case GLOBAL:
				Static.getServer().getPlayerList().getPlayers().forEach(player -> { announce(player, string, objs); });
				break;
			case LOCAL:
				break;
			case MUNICIPALITY:
				break;
			case STATE:
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
