package net.fexcraft.mod.landdev.util;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.packet.IPacketListener;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.lib.mc.render.ExternalTextureHelper;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.mod.landdev.events.LocationUpdate;
import net.fexcraft.mod.landdev.gui.LDGuiImgPreview;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class PacketReceiver implements IPacketListener<PacketNBTTagCompound> {
	
	public static final String RECEIVER_ID = "landdev:util";

	@Override
	public String getId(){
		return RECEIVER_ID;
	}

	@Override
	public void process(PacketNBTTagCompound packet, Object[] objs){
		if(!packet.nbt.hasKey("task")) return;
		switch(packet.nbt.getString("task")){
		case "location_update":{
			int time = packet.nbt.hasKey("time") ? packet.nbt.getInteger("time") : 10;
			LocationUpdate.clear(Time.getDate() + (time * 1000));
			LocationUpdate.loadIcons((NBTTagList)packet.nbt.getTag("icons"));
			LocationUpdate.loadLines((NBTTagList)packet.nbt.getTag("lines"));
			return;
		}
		case "chat_message":{
			NBTTagList list = (NBTTagList)packet.nbt.getTag("msg");
			String c = list.tagCount() > 3 ? list.getStringTagAt(3) : "&a";
			ITextComponent text = null;
			switch(list.getStringTagAt(0)){
			case "chat":
				text = new TextComponentString(format(Settings.CHAT_OVERRIDE_LANG, c, list.getStringTagAt(1), list.getStringTagAt(2)));
				break;
			case "chat_img":
				text = new TextComponentString(format(list.getStringTagAt(1)));
				text.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ld img " + list.getStringTagAt(2) + " " + c + " " + list.getStringTagAt(4)));
				text.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(format(list.getStringTagAt(1)))));
				break;
			default:
				text = new TextComponentString(list.toString());
				break;
			}
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(text);
			return;
		}
		case "img_preview_url":
			LDGuiImgPreview.IMG_URL = ExternalTextureHelper.get(packet.nbt.getString("url"));
			return;
		}
	}
	
	public static String format(String string, Object... args){
		return Formatter.format(String.format(string, args));
	}

}
