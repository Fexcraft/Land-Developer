package net.fexcraft.mod.landdev.util.broad;

import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TransmitterType;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class LogTransmitter implements Transmitter {

	@Override
	public void transmit(String channel, String sender, String message, Object[] args){
		LandDev.log("[" + channel + "] " + (sender.startsWith("&") ? sender.substring(2) : sender) + ": " + message);
	}
	
	@Override
	public boolean internal(){
		return true;
	}

	@Override
	public TransmitterType type(){
		return TransmitterType.LOG;
	}
	
}
