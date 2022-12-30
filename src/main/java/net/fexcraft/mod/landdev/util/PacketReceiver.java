package net.fexcraft.mod.landdev.util;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.packet.IPacketListener;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.landdev.events.LocationUpdate;
import net.minecraft.nbt.NBTTagList;

public class PacketReceiver implements IPacketListener<PacketNBTTagCompound> {
	
	public static final String RECEIVER_ID = "landdev:util";

	@Override
	public String getId(){
		return RECEIVER_ID;
	}

	@Override
	public void process(PacketNBTTagCompound packet, Object[] objs){
		if(!packet.nbt.hasKey("task")) return;
		switch(packet.nbt.getString("task")){
		case "location_update":{
			int time = packet.nbt.hasKey("time") ? packet.nbt.getInteger("time") : 10;
			LocationUpdate.clear(Time.getDate() + (time * 1000));
			LocationUpdate.loadIcons((NBTTagList)packet.nbt.getTag("icons"));
			LocationUpdate.loadLines((NBTTagList)packet.nbt.getTag("lines"));
			return;
		}
		}
	}

}
