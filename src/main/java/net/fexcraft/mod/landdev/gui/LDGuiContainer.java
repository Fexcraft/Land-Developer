package net.fexcraft.mod.landdev.gui;

import static net.fexcraft.mod.landdev.LandDev.INSTANCE;
import static net.fexcraft.mod.landdev.gui.GuiHandler.*;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_GENERIC;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_OPEN;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.gui.GenericGui.BasicText;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LDGuiContainer extends GenericContainer {

	protected String prefix;
	private int type;
	@SideOnly(Side.CLIENT)
	public LDGuiBase gui;

	public LDGuiContainer(EntityPlayer player, int id, int x, int y, int z){
		super(player);
		switch(type = id){
			case MAIN: prefix = "main"; break;
		}
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(side == Side.CLIENT) client_packet(packet, player);
		else server_packet(packet, player);
	}

	private void server_packet(NBTTagCompound packet, EntityPlayer player){
		if(packet.getBoolean("sync")){
			NBTTagCompound com = new NBTTagCompound();
			switch(type){
				case MAIN:{
					com.setString("title_lang", "main.title");
					NBTTagList list = new NBTTagList();
					addToList(list, "player", ELM_GENERIC, ICON_OPEN, true, false, null);
					addToList(list, "mail", ELM_GENERIC, ICON_OPEN, true, false, null);
					addToList(list, "property", ELM_GENERIC, ICON_OPEN, true, false, null);
					addToList(list, "company", ELM_GENERIC, ICON_OPEN, true, false, null);
					addToList(list, "spacer", ELM_BLANK, ICON_BLANK, false, false, null);
					addToList(list, "chunk", ELM_GENERIC, ICON_OPEN, true, false, null);
					addToList(list, "district", ELM_GENERIC, ICON_OPEN, true, false, null);
					addToList(list, "municipality", ELM_GENERIC, ICON_OPEN, true, false, null);
					addToList(list, "county", ELM_GENERIC, ICON_OPEN, true, false, null);
					addToList(list, "state", ELM_GENERIC, ICON_OPEN, true, false, null);
					com.setTag("elements", list);
					break;
				}
			}
			send(Side.CLIENT, com);
		}
		if(packet.hasKey("interact")){
			String index = packet.getString("interact");
			Chunk_ chunk = ResManager.getChunk(player);
			switch(type){
				case MAIN:{
					switch(index){
						case "player": player.openGui(INSTANCE, PLAYER, player.world, 0, 0, 0); return;
						case "mail": player.openGui(INSTANCE, MAILBOX, player.world, 0, 0, 0); return;
						case "property": player.openGui(INSTANCE, PROPERTY, player.world, 0, 0, 0); return;
						case "company": player.openGui(INSTANCE, COMPANY, player.world, 0, 0, 0); return;
						case "chunk": player.openGui(INSTANCE, CHUNK, player.world, 0, chunk.key.x, chunk.key.z); return;
						case "district": player.openGui(INSTANCE, DISTRICT, player.world, 0, chunk.district.id, 0); return;
						case "municipality":{
							if(!chunk.district.owner.county)
								player.openGui(INSTANCE, MUNICIPALITY, player.world, 0, chunk.district.owner.municipality.id, 0);
							return;
						}
						case "county":{
							player.openGui(INSTANCE, MUNICIPALITY, player.world, 0, chunk.district.owner.county_id(), 0);
							return;
						}
						case "state": player.openGui(INSTANCE, STATE, player.world, 0, 0, 0); return;
					}
					break;
				}
			}
		}
	}

	private void addToList(NBTTagList root, String index, LDGuiElementType elm, LDGuiElementType icon, boolean button, boolean field, String value){
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagString(index));
		list.appendTag(new NBTTagString(elm.name()));
		list.appendTag(new NBTTagString(icon.name()));
		list.appendTag(new NBTTagString((elm == ELM_BLANK ? "0" : "1") + (button ? "1" : "0") + (field ? "1" : "0")));
		if(value != null) list.appendTag(new NBTTagString(value));
		root.appendTag(list);
	}

	private void client_packet(NBTTagCompound packet, EntityPlayer player){
		if(!packet.hasKey("elements")) return;
		NBTTagList list = (NBTTagList)packet.getTag("elements");
		gui.clear();
		gui.sizeOf(list.tagCount());
		gui.title = new BasicText(gui.getGuiLeft() + 8, gui.getGuiTop() + 8, 196, 0x0e0e0e, "landdev.gui." + packet.getString("title_lang")).autoscale().translate();
		gui.add("title", gui.title);
		if(packet.hasKey("title")) gui.title.string = String.format(gui.title.string, packet.getString("title"));
		int idx = 0;
		for(NBTBase base : list){
			NBTTagList lis = (NBTTagList)base;
			String index = lis.getStringTagAt(0);
			LDGuiElementType elm = LDGuiElementType.valueOf(lis.getStringTagAt(1));
			LDGuiElementType icon = LDGuiElementType.valueOf(lis.getStringTagAt(2));
			String bools = lis.getStringTagAt(3);
			gui.addElm(index, elm, icon, idx++, bools.charAt(0) == '1', bools.charAt(1) == '1');
		}
	}
	
}