package net.fexcraft.mod.landdev.util.broad;

public enum BroadcastChannel {
	
	CHAT, ANNOUNCE, PLAYER, STATE, COUNTY, MUNICIPALITY, DISTRICT, COMPANY;
	
	public String name;
	
	BroadcastChannel(){
		name = name().toLowerCase();
	}

}
