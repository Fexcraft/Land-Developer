package net.fexcraft.mod.landdev.util.broad.broad;

import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TransmitterType;

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
		//TODO
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
