package net.fexcraft.mod.landdev.util.broad;

import static net.fexcraft.mod.landdev.util.broad.BroadcastChannel.CHAT;

import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TransmitterType;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class InternalTransmitter implements Transmitter {

	@Override
	public void transmit(String channel, String sender, String message, Object[] args){
		if(channel.equals(CHAT.name) && !LDConfig.CHAT_OVERRIDE) return;
		TagCW com = TagCW.create();
		com.set("task", "chat_message");
		TagLW list = TagLW.create();
		if(args.length > 0 && args[0].equals("img")){
			list.add(channel + "_img");
			list.add(message);
			list.add(args[1].toString());
			list.add(args[2].toString());
			list.add(args[3].toString());
		}
		else{
			list.add(channel);
			list.add(sender);
			list.add(message);
			if(args != null) list.add(args[0].toString());
		}
		com.set("msg", list);
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
