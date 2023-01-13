package net.fexcraft.mod.landdev.util.broad;

import static net.fexcraft.mod.landdev.util.broad.BroadcastChannel.CHAT;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.Settings;

public class Broadcaster {
	
	public static ArrayList<Transmitter> SENDERS = new ArrayList<>();
	static { SENDERS.add(new InternalTransmitter()); }

	public static void send(Player player, String message){
		send(CHAT.name, player.name(), message, player.adm ? Settings.CHAT_ADMIN_COLOR : Settings.CHAT_PLAYER_COLOR);
	}

	private static void send(String channel, String sender, String message, String arg){
		for(Transmitter trs : SENDERS) trs.transmit(channel, sender, message, arg);
	}

	public static interface Transmitter {
		
		public void transmit(String channel, String sender, String msg, @Nullable String color);
		
		public String category();
		
	}

}
