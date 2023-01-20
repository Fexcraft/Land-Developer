package net.fexcraft.mod.landdev.util.broad;

import static net.fexcraft.mod.landdev.util.broad.BroadcastChannel.CHAT;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.Settings;

public class Broadcaster {
	
	public static ConcurrentHashMap<TransmitterType, Transmitter> SENDERS = new ConcurrentHashMap<>();
	static { SENDERS.put(TransmitterType.INTERNAL, new InternalTransmitter()); }

	public static void send(Player player, String message){
		send(TargetTransmitter.ALL, CHAT.name, player.name(), message, player.adm ? Settings.CHAT_ADMIN_COLOR : Settings.CHAT_PLAYER_COLOR);
	}

	public static void send(TargetTransmitter target, BroadcastChannel channel, String sender, String message, Object... args){
		send(target, channel.name, sender, message, args);
	}

	public static void send(TargetTransmitter target, String channel, String sender, String message, Object... args){
		Transmitter trs = null;
		for(TransmitterType type : target.types){
			if((trs = SENDERS.get(type)) == null) continue;
			trs.transmit(channel, sender, message, args);
		}
	}

	public static interface Transmitter {
		
		public void transmit(String channel, String sender, String msg, @Nullable Object[] args);

		public default boolean internal(){ return false; }
		
		public TransmitterType type();
		
	}
	
	public static enum TransmitterType {
		
		INTERNAL, DISCORD;
		
		public boolean is(TransmitterType other){
			return this == other;
		}
		
	}
	
	public static enum TargetTransmitter {

		ALL(TransmitterType.values()),
		NO_INTERNAL(TransmitterType.DISCORD),
		NO_DISCORD(TransmitterType.INTERNAL),
		INTERNAL_ONLY(TransmitterType.INTERNAL);

		private TransmitterType[] types;

		TargetTransmitter(TransmitterType... types){
			this.types = types;
		}
		
	}

}
