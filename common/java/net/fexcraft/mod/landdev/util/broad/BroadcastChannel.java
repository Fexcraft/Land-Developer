package net.fexcraft.mod.landdev.util.broad;

public enum BroadcastChannel {
	
	CHAT, SERVER, ANNOUNCE, PLAYER, REGION, COUNTY, MUNICIPALITY, DISTRICT, COMPANY;
	
	public String name;
	
	BroadcastChannel(){
		name = name().toLowerCase();
	}
	
	@Override
	public String toString(){
		return name;
	}

}
