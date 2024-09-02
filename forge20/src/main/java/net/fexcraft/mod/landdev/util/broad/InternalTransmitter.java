package net.fexcraft.mod.landdev.util.broad;

import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TransmitterType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import static net.fexcraft.mod.landdev.util.broad.BroadcastChannel.CHAT;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class InternalTransmitter implements Transmitter {

	@Override
	public void transmit(String channel, String sender, String message, Object[] args){
		if(channel.equals(CHAT.name) && !LDConfig.CHAT_OVERRIDE) return;
		CompoundTag com = new CompoundTag();
		com.putString("task", "chat_message");
		ListTag list = new ListTag();
		if(args.length > 0 && args[0].equals("img")){
			list.add(StringTag.valueOf(channel + "_img"));
			list.add(StringTag.valueOf(message));
			list.add(StringTag.valueOf(args[1].toString()));
			list.add(StringTag.valueOf(args[2].toString()));
			list.add(StringTag.valueOf(args[3].toString()));
		}
		else{
			list.add(StringTag.valueOf(channel));
			list.add(StringTag.valueOf(sender));
			list.add(StringTag.valueOf(message));
			if(args != null) list.add(StringTag.valueOf(args[0].toString()));
		}
		com.put("msg", list);
		LandDev.sendToAll(com);
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
