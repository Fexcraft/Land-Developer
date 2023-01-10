package net.fexcraft.mod.landdev.util;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.landdev.data.player.Player;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public class Broadcaster {
	
	public static ArrayList<Transmitter> SENDERS = new ArrayList<>();
	static { SENDERS.add(new Broadcaster.InternalTransmitter()); }

	public static void send(Player player, String message){
		send("chat", player.name(), message, player.adm ? "&4" : "&6");
	}

	private static void send(String channel, String sender, String message, String arg){
		for(Transmitter trs : SENDERS) trs.transmit(channel, sender, message, arg);
	}

	public static interface Transmitter {
		
		public void transmit(String channel, String sender, String msg, @Nullable String color);
		
		public String category();
		
	}
	
	private static class InternalTransmitter implements Transmitter {

		@Override
		public void transmit(String channel, String sender, String message, String color){
			if(!Settings.CHAT_OVERRIDE) return;
			NBTTagCompound com = new NBTTagCompound();
			com.setString("target_listener", PacketReceiver.RECEIVER_ID);
			com.setString("task", "chat_message");
			NBTTagList list = new NBTTagList();
			list.appendTag(new NBTTagString(channel));
			list.appendTag(new NBTTagString(sender));
			list.appendTag(new NBTTagString(message));
			if(color != null) list.appendTag(new NBTTagString(color));
			com.setTag("msg", list);
			PacketHandler.getInstance().sendToAll(new PacketNBTTagCompound(com));
		}

		@Override
		public String category(){
			return "internal";
		}
		
	}

}
