package net.fexcraft.mod.landdev.util.broad;

import static net.fexcraft.mod.landdev.util.broad.BroadcastChannel.CHAT;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.LDConfig;

public class Broadcaster {
	
	public static ConcurrentHashMap<TransmitterType, Transmitter> SENDERS = new ConcurrentHashMap<>();
	static {
		SENDERS.put(TransmitterType.INTERNAL, new InternalTransmitter());
		SENDERS.put(TransmitterType.LOG, new LogTransmitter());
	}

	public static void send(LDPlayer player, String message){
		send(TargetTransmitter.ALL, CHAT.name, player.name(), message, player.adm ? LDConfig.CHAT_ADMIN_COLOR : LDConfig.CHAT_PLAYER_COLOR);
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
		
		INTERNAL, DISCORD, LOG;
		
		public boolean is(TransmitterType other){
			return this == other;
		}

		public boolean internal(){
			return this == INTERNAL || this == LOG;
		}
		
	}
	
	public static enum TargetTransmitter {

		ALL(TransmitterType.values()),
		NO_INTERNAL(TransmitterType.DISCORD),
		NO_DISCORD(TransmitterType.INTERNAL),
		INTERNAL_ONLY(TransmitterType.INTERNAL),
		LOG_ONLY(TransmitterType.LOG);

		private TransmitterType[] types;

		TargetTransmitter(TransmitterType... types){
			this.types = types;
		}
		
	}

}
