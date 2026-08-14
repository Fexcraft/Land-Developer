package net.fexcraft.mod.landdev.util.broad;

import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.Manageable;
import net.fexcraft.mod.landdev.data.player.LDPlayer;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Channel {

	public static final Channel CHAT = new Channel(ChannelType.CHAT, SubChannelType.ALL, null);
	public static final Channel SERVER = new Channel(ChannelType.SERVER, SubChannelType.ALL, null);
	public final ChannelType type;
	public final SubChannelType sub;
	public final Layer layer;

	public Channel(ChannelType ct, SubChannelType s, Layer lay){
		type = ct;
		sub = s;
		layer = lay;
	}

	public boolean isStaffOrManager(LDPlayer ply){
		return ((Manageable)layer).isStaffOrManager(ply.uuid);
	}

	public boolean isMember(LDPlayer ply){
		switch(type){
			case MUNICIPALITY: return ply.municipality != null && ply.municipality.id == layer.lid();
			case COUNTY: return ply.county.id == layer.lid();
			case REGION: return ply.county.region.id == layer.lid();
			case COMPANY: break;//TODO
		}
		return false;
	}

	public static enum ChannelType {

		CHAT,
		SERVER,

		REGION,
		COUNTY,
		MUNICIPALITY,
		DISTRICT,
		COMPANY,
		;

		public String name;

		ChannelType(){
			name = name().toLowerCase();
		}

		@Override
		public String toString(){
			return name;
		}

	}

	public static enum SubChannelType {

		ALL, MEMBER, STAFF;

		public String name;

		SubChannelType(){
			name = name().toLowerCase();
		}

		@Override
		public String toString(){
			return name;
		}

	}

	@Override
	public String toString(){
		if(this == CHAT || this == SERVER) return type.name;
		return type + ":" + sub + ":" + layer.lid();
	}

}
