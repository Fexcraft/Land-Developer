package net.fexcraft.mod.landdev.util.broad;

import java.util.concurrent.ConcurrentHashMap;

import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.LDConfig;

public class Broadcaster {

	public static String LD_SENDER = "\u00a79[\u00a72LD\u00a79]";
	public static ConcurrentHashMap<TransmitterType, Transmitter> SENDERS = new ConcurrentHashMap<>();
	static {
		SENDERS.put(TransmitterType.INTERNAL, new InternalTransmitter());
		SENDERS.put(TransmitterType.LOG, new LogTransmitter());
	}

	public static void send(LDPlayer player, String message){
		send(TargetTransmitter.ALL, Channel.CHAT, player.name(), message, player.adm ? LDConfig.CHAT_ADMIN_COLOR : LDConfig.CHAT_PLAYER_COLOR);
	}

	public static void send(Channel channel, String sender, String message, String tint, Object... args){
		send(TargetTransmitter.ALL, channel, sender, message, tint, args);
	}

	public static void announce(Channel channel, String message, Object... args){
		send(TargetTransmitter.ALL, channel, LD_SENDER, "landdev.announce." + message, "&a", args);
	}

	public static void send(TargetTransmitter target, Channel channel, String sender, String message, String tint, Object... args){
		Transmitter trs;
		for(TransmitterType type : target.types){
			if((trs = SENDERS.get(type)) == null) continue;
			trs.transmit(channel, sender, message, tint, args);
		}
	}

	public static interface Transmitter {
		
		public void transmit(Channel channel, String sender, String msg, String tint, Object[] args);

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
