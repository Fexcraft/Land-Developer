package net.fexcraft.mod.landdev.util.broad;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonHandler.PrintOption;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.landdev.util.Settings;
import net.fexcraft.mod.landdev.util.broad.Broadcaster.Transmitter;

/**
 * 
 * @author Ferdinand Calo' (FEX___96)
 *
 */
public class DiscordTransmitter implements Transmitter {
	
	private static DiscordTransmitter INST;
	private JsonMap map = new JsonMap();
	private static ChannelFuture fut;
	private static boolean invalid;

	@Override
	public void transmit(String channel, String sender, String message, String color){
		Static.getServer().addScheduledTask(() -> {
			if(fut != null && !fut.channel().isActive()) return;
	        try{
	            fut.channel().writeAndFlush("msg=" + JsonHandler.toString(map, PrintOption.FLAT));
	        }
	        catch(Exception e){
	        	Print.log("Error on sending message to discord bot. " + map);
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
		INST = new DiscordTransmitter();
		Broadcaster.SENDERS.add(INST);
		try{
			INST.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void start() throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			if(invalid) return;
			Bootstrap boot = new Bootstrap();
			boot.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					p.addLast(new StringDecoder());
					p.addLast(new StringEncoder());
					p.addLast(new ClientHandler());
				}
			});
			fut = boot.connect(Settings.DISCORD_BOT_ADRESS, Settings.DISCORD_BOT_PORT).sync();
			Channel channel = fut.sync().channel();
			channel.writeAndFlush("token=" + Settings.DISCORD_BOT_TOKEN);
			channel.flush();
			fut.channel().closeFuture().sync();
		}
		finally{
			group.shutdownGracefully();
		}
	}

	public static void exit(){
		if(INST == null) return;
		if(fut != null) fut.channel().close();
	}

	private static class ClientHandler extends SimpleChannelInboundHandler<String> {

		@Override
		protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
			if(msg.equals("OK")) return;
			else if(msg.startsWith("token=invalid")){
				Print.log("Discord bot response: " + msg.substring(14));
				invalid = true;
			}
			else Print.log("Discord bot response: " + msg);
		}

	}
	
}
