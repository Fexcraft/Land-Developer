package net.fexcraft.mod.landdev.util.broad;

import static net.fexcraft.mod.landdev.LandDev.CLIENT_RECEIVER_ID;
import static net.fexcraft.mod.landdev.util.broad.BroadcastChannel.CHAT;

import net.fexcraft.lib.mc.network.PacketHandler;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.landdev.util.LDConfig;
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
	public void transmit(String channel, String sender, String message, Object[] args){
		if(channel.equals(CHAT.name) && !LDConfig.CHAT_OVERRIDE) return;
		NBTTagCompound com = new NBTTagCompound();
		com.setString("target_listener", CLIENT_RECEIVER_ID);
		com.setString("task", "chat_message");
		NBTTagList list = new NBTTagList();
		if(args.length > 0 && args[0].equals("img")){
			list.appendTag(new NBTTagString(channel + "_img"));
			list.appendTag(new NBTTagString(message));
			list.appendTag(new NBTTagString(args[1].toString()));
			list.appendTag(new NBTTagString(args[2].toString()));
			list.appendTag(new NBTTagString(args[3].toString()));
		}
		else{
			list.appendTag(new NBTTagString(channel));
			list.appendTag(new NBTTagString(sender));
			list.appendTag(new NBTTagString(message));
			if(args != null) list.appendTag(new NBTTagString(args[0].toString()));
		}
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
