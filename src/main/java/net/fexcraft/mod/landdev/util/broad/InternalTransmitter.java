package net.fexcraft.mod.landdev.util.broad;

import static net.fexcraft.mod.landdev.util.broad.BroadcastChannel.CHAT;

import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.landdev.util.PacketReceiver;
import net.fexcraft.mod.landdev.util.Settings;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TransmitterType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class InternalTransmitter implements Transmitter {

	@Override
	public void transmit(String channel, String sender, String message, String[] args){
		if(channel.equals(CHAT.name) && !Settings.CHAT_OVERRIDE) return;
		NBTTagCompound com = new NBTTagCompound();
		com.setString("target_listener", PacketReceiver.RECEIVER_ID);
		com.setString("task", "chat_message");
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagString(channel));
		list.appendTag(new NBTTagString(sender));
		list.appendTag(new NBTTagString(message));
		if(args != null) list.appendTag(new NBTTagString(args[0]));
		com.setTag("msg", list);
		PacketHandler.getInstance().sendToAll(new PacketNBTTagCompound(com));
	}
	
	@Override
	public boolean internal(){
		return true;
	}

	@Override
	public TransmitterType type(){
		return TransmitterType.INTERNAL;
	}
	
}
