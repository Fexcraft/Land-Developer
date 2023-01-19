package net.fexcraft.mod.landdev.util.broad;

import static net.fexcraft.mod.landdev.util.broad.BroadcastChannel.CHAT;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nullable;

import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.Settings;

public class Broadcaster {
	
	public static ConcurrentLinkedQueue<Transmitter> SENDERS = new ConcurrentLinkedQueue<>();
	static { SENDERS.add(new InternalTransmitter()); }

	public static void send(Player player, String message){
		send(CHAT.name, player.name(), message, player.adm ? Settings.CHAT_ADMIN_COLOR : Settings.CHAT_PLAYER_COLOR, false);
	}

	public static void send(BroadcastChannel channel, String sender, String message, String arg, boolean no_internal){
		send(channel.name(), sender, message, arg, no_internal);
	}

	public static void send(String channel, String sender, String message, String arg, boolean no_internal){
		for(Transmitter trs : SENDERS){
			if(no_internal && trs.internal()) continue;
			trs.transmit(channel, sender, message, arg);
		}
	}

	public static interface Transmitter {
		
		public void transmit(String channel, String sender, String msg, @Nullable String color);
		
		public default boolean internal(){ return false; }

		public String category();
		
	}

}
