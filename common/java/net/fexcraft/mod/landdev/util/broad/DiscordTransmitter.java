package net.fexcraft.mod.landdev.util.broad;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonHandler.PrintOption;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TransmitterType;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class DiscordTransmitter implements Transmitter {
	
	private static DiscordTransmitter INST;
	private static ChannelFuture fut;
	private static JsonMap map = new JsonMap();

	@Override
	public void transmit(String channel, String sender, String message, Object[] args){
		Static.getServer().addScheduledTask(() -> {
			if(fut != null && !fut.channel().isActive()) return;
	        try{
	        	JsonMap map = new JsonMap();
	        	map.add("c", channel);
	        	if(sender != null) map.add("s", sender.startsWith("&") ? sender.substring(2) : sender);
	        	map.add("m", message);
	            fut.channel().writeAndFlush(new Message("msg=" + JsonHandler.toString(map, PrintOption.FLAT)));
	        }
	        catch(Exception e){
	        	LandDev.log("Error on sending message to discord bot. " + map);
	        	e.printStackTrace();
	        }
		});
	}

	public static void restart(){
		exit();
		if(!LDConfig.DISCORD_BOT_ACTIVE) return;
		Broadcaster.SENDERS.values().removeIf(transmitter -> transmitter instanceof DiscordTransmitter);
		Broadcaster.SENDERS.put(TransmitterType.DISCORD, INST = new DiscordTransmitter());
		new Thread(() -> {
			try{
				INST.start();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}, "DiscordIntegrationStarter").start();
	}

	private void start() throws Exception {
		NioEventLoopGroup group = new NioEventLoopGroup();
		try {
            Bootstrap boot = new Bootstrap();
            boot.group(group);
            boot.channel(NioSocketChannel.class);
            boot.option(ChannelOption.SO_KEEPALIVE, true);
            boot.handler(new ChannelInitializer<SocketChannel>(){
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new MsgDecoder(), new MsgEncoder(), new ClientHandler());
                }
            });
			fut = boot.connect(LDConfig.DISCORD_BOT_ADRESS, LDConfig.DISCORD_BOT_PORT).sync();
			Channel channel = fut.sync().channel();
			channel.writeAndFlush(new Message("token=" + LDConfig.DISCORD_BOT_TOKEN));
			fut.channel().closeFuture().sync();
		}
		finally{
			group.shutdownGracefully();
		}
	}

	public static void exit(){
		INST = null;
		if(fut != null) fut.channel().close();
	}

	private static class ClientHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
			Message msg = (Message)obj;
			if(msg.length <= 0) return;
			if(!msg.value.startsWith("msg=")) LandDev.log("Discord bot response: " + msg.value);
			JsonMap map = (JsonMap)JsonHandler.parse(msg.value.substring(4), true);
			String user = map.getString("s", "DiscordUser");
			if(map.get("m").string_value().length() > 0){
				Broadcaster.send(TargetTransmitter.NO_DISCORD, BroadcastChannel.CHAT, "&2" + user, map.getString("m", "<MESSAGE_TEXT>"), LDConfig.CHAT_DISCORD_COLOR);
				Broadcaster.send(TargetTransmitter.LOG_ONLY, BroadcastChannel.CHAT, "D|" + user, map.getString("m", "<MESSAGE_TEXT>"));
			}
			else Broadcaster.send(TargetTransmitter.INTERNAL_ONLY, BroadcastChannel.CHAT, "&2" + user, "&b[!] &6Embeds: " + (map.has("a") ? map.get("a").asArray().size() : "ERR"), LDConfig.CHAT_DISCORD_COLOR);
			if(map.has("a")){
				int[] idx = { 1 };
				map.get("a").asArray().elements().forEach(elm -> {
					JsonArray array = elm.asArray();
					Broadcaster.send(TargetTransmitter.INTERNAL_ONLY, BroadcastChannel.CHAT, "", "&l&6Embed " + idx[0]++ + ": ", "img", array.get(0).string_value(), array.get(1).string_value(), array.get(2).string_value());
					Broadcaster.send(TargetTransmitter.LOG_ONLY, BroadcastChannel.CHAT, "D|" + user, array.get(0).string_value());
				});
			}
		}

	}

	@Override
	public TransmitterType type(){
		return TransmitterType.DISCORD;
	}
	
}
