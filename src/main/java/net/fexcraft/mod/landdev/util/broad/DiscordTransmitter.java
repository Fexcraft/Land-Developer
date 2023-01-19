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
import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonHandler.PrintOption;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.util.Settings;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;

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
	public void transmit(String channel, String sender, String message, String color){
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

	@Override
	public String category(){
		return "discord";
	}

	public static void restart(){
		exit();
		if(!Settings.DISCORD_BOT_ACTIVE) return;
		Broadcaster.SENDERS.removeIf(transmitter -> transmitter instanceof DiscordTransmitter);
		Broadcaster.SENDERS.add(INST = new DiscordTransmitter());
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
			fut = boot.connect(Settings.DISCORD_BOT_ADRESS, Settings.DISCORD_BOT_PORT).sync();
			Channel channel = fut.sync().channel();
			channel.writeAndFlush(new Message("token=" + Settings.DISCORD_BOT_TOKEN));
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
			else LandDev.log("Discord bot response: " + msg.value);
		}

	}
	
}
