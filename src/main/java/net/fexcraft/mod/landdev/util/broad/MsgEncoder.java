package net.fexcraft.mod.landdev.util.broad;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MsgEncoder extends MessageToByteEncoder<Message> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
		out.writeInt(msg.length);
		if(msg.length > 0) out.writeBytes(msg.value.getBytes(StandardCharsets.UTF_8));
	}

}
