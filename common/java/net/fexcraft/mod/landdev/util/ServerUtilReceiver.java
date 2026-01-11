package net.fexcraft.mod.landdev.util;

import net.fexcraft.mod.uni.packet.PacketTagListener;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.EntityW;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ServerUtilReceiver implements PacketTagListener {

	@Override
	public void handle(TagCW packet, EntityW player){
		if(!packet.has("task")) return;
		switch(packet.getString("task")){
			case ".":{
				//
				return;
			}
		}
	}

}
