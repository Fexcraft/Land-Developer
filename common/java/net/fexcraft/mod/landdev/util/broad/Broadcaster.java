package net.fexcraft.mod.landdev.util.broad;

import java.util.concurrent.ConcurrentHashMap;

import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Broadcaster {

	public static String LD_SENDER = "\u00a79\u00a7lLD";
	public static ConcurrentHashMap<TransmitterType, Transmitter> SENDERS = new ConcurrentHashMap<>();
	static {
		SENDERS.put(TransmitterType.INTERNAL, new InternalTransmitter());
		SENDERS.put(TransmitterType.LOG, new LogTransmitter());
	}

	public static void send(LDPlayer player, String message){
		new Message(Channel.CHAT).sender(player.name()).tint(player.adm ? LDConfig.CHAT_ADMIN_COLOR : LDConfig.CHAT_PLAYER_COLOR)
			.set(message).send(TargetTransmitter.ALL);
	}

	public static void announce(Channel channel, String message, Object... args){
		new Message(channel).tint("&b").set("landdev.announce." + message, args).send(TargetTransmitter.ALL);
	}

	public static Message newMessage(Channel channel){
		return new Message(channel);
	}

	public static class Message {

		public String sender = LD_SENDER;
		public String message;
		public String tint = "&a";
		public Object[] args;
		public Channel channel;
		public boolean img;

		public Message(){
			channel = Channel.CHAT;
		}

		public Message(Channel ch){
			channel = ch;
		}

		public Message set(String msg, Object... objs){
			message = msg;
			args = objs;
			return this;
		}

		public Message sender(String s){
			sender = s;
			return this;
		}

		public Message tint(String c){
			tint = c;
			return this;
		}

		public Message asImage(){
			img = true;
			return this;
		}

		public Message send(TargetTransmitter target){
			Transmitter trs;
			for(TransmitterType type : target.types){
				if((trs = SENDERS.get(type)) == null) continue;
				trs.transmit(this);
			}
			return this;
		}

		public TagCW toTag(){
			TagCW com = TagCW.create();
			TagLW lis = TagLW.create();
			com.set("c", channel.toString());
			if(tint != null) com.set("t", tint);
			com.set("s", sender);
			com.set("m", message);
			if(img) com.set("i", true);
			for(Object arg : args) lis.add(arg.toString());
			com.set("a", lis);
			return com;
		}

	}

	public static interface Transmitter {
		
		public void transmit(Message msg);

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
