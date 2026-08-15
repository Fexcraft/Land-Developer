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
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TargetTransmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.TransmitterType;
import net.fexcraft.mod.uni.world.WrapperHolder;

import static net.fexcraft.mod.landdev.util.broad.Channel.CHAT;

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
	public void transmit(Broadcaster.Message msg){
		WrapperHolder.schedule(() -> {
			if(fut != null && !fut.channel().isActive()) return;
	        try{
	        	JsonMap map = new JsonMap();
	        	map.add("c", msg.channel.toString());
	        	if(msg.sender != null){
					String sender = msg.sender;
					while(sender.contains("&")) sender = sender.substring(sender.indexOf("&") + 2);
					while(sender.contains("\u00a7")) sender = sender.substring(sender.indexOf("\u00a7") + 2);
					map.add("s", sender);
				}
	        	map.add("m", msg.message);
	            fut.channel().writeAndFlush(new NettyMsg("msg=" + JsonHandler.toString(map, PrintOption.FLAT)));
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
			channel.writeAndFlush(new NettyMsg("token=" + LDConfig.DISCORD_BOT_TOKEN));
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
			NettyMsg msg = (NettyMsg)obj;
			if(msg.length <= 0) return;
			if(!msg.value.startsWith("msg=")) LandDev.log("Discord bot response: " + msg.value);
			JsonMap map = (JsonMap)JsonHandler.parse(msg.value.substring(4), true);
			String user = map.getString("s", "DiscordUser");
			if(map.has("m") && map.get("m").string_value().length() > 0){
				new Broadcaster.Message().tint(LDConfig.CHAT_DISCORD_COLOR).set(map.getString("m", ""))
					.sender("&2" + user).send(TargetTransmitter.NO_DISCORD)
					.sender("D|" + user).send(TargetTransmitter.LOG_ONLY);
			}
			else{
				new Broadcaster.Message().sender("&2" + user).tint(LDConfig.CHAT_DISCORD_COLOR)
					.set(map.has("a") ? "&b[!] &6Embeds: " + map.get("a").asArray().size() : "&b[!] ERROR, check log for details.")
					.send(TargetTransmitter.INTERNAL_ONLY);
			}
			if(map.has("a")){
				int[] idx = { 1 };
				map.get("a").asArray().elements().forEach(elm -> {
					JsonArray array = elm.asArray();
					new Broadcaster.Message().tint(LDConfig.CHAT_DISCORD_COLOR).asImage()
						.set("&l&6Embed " + idx[0]++ + ": ", array.get(0).string_value(), array.get(1).string_value(), array.get(2).string_value())
						.sender("&2" + user).send(TargetTransmitter.INTERNAL_ONLY)
						.sender("D|" + user).send(TargetTransmitter.LOG_ONLY);
				});
			}
		}

	}

	@Override
	public TransmitterType type(){
		return TransmitterType.DISCORD;
	}
	
}
