package net.fexcraft.mod.landdev.util.broad;

import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TransmitterType;

import java.util.Arrays;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class LogTransmitter implements Transmitter {

	@Override
	public void transmit(Broadcaster.Message msg){
		LandDev.log("[" + msg.channel + "] " + (msg.sender.startsWith("&") ? msg.sender.substring(2) : msg.sender) + ": " + msg.message + (msg.args != null ? " / " + Arrays.toString(msg.args) : ""));
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
