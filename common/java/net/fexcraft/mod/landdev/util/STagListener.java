package net.fexcraft.mod.landdev.util;

import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.uni.packet.PacketTagListener;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.world.EntityW;

import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class STagListener implements PacketTagListener {

	public static HashMap<String, BiConsumer<TagCW, EntityW>> TASKS = new HashMap<>();

	@Override
	public void handle(TagCW packet, EntityW player){
		if(!packet.has("task")) return;
		BiConsumer<TagCW, EntityW> cons = TASKS.get(packet.getString("task"));
		if(cons != null) cons.accept(packet, player);
		else{
			LandDev.log("Received packet with unknown task '" + packet.getString("task") + "'.");
			LandDev.log(String.valueOf(packet));
		}
	}

}
