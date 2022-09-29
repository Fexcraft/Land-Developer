package net.fexcraft.mod.landdev.gui;

import static net.fexcraft.mod.landdev.gui.GuiHandler.CHUNK;
import static net.fexcraft.mod.landdev.gui.GuiHandler.MAIN;
import static net.fexcraft.mod.landdev.gui.GuiHandler.MUNICIPALITY;

import java.util.HashMap;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.gui.GenericGui.BasicText;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.gui.modules.Main;
import net.fexcraft.mod.landdev.gui.modules.Missing;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LDGuiContainer extends GenericContainer {

	protected String prefix;
	public final int type, x, y, z;
	@SideOnly(Side.CLIENT)
	public LDGuiBase gui;
	public Player player;
	private boolean form;
	protected HashMap<String, Boolean> checkboxes = new HashMap<>();

	public LDGuiContainer(EntityPlayer player, int id, int x, int y, int z){
		super(player);
		switch(type = id){
			case MAIN: prefix = "main"; break;
			case CHUNK: prefix = "chunk"; break;
			case MUNICIPALITY: prefix = "municipality"; break;
		}
		this.player = ResManager.getPlayer(player);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	protected void packet(Side side, NBTTagCompound packet, EntityPlayer player){
		if(side == Side.CLIENT) client_packet(packet, player);
		else server_packet(packet, player);
	}

	private void server_packet(NBTTagCompound packet, EntityPlayer player){
		if(packet.getBoolean("sync")){
			NBTTagCompound com = new NBTTagCompound();
			Chunk_ chunk = ResManager.getChunk(player);
			switch(type){
				case MAIN:{
					Main.INST.sync_packet(this, com);
					break;
				}
				case CHUNK:{
					chunk.sync_packet(this, com);
					break;
				}
				case MUNICIPALITY:{
					if(x < 0){
						ResManager.getMunicipality(-1, true).sync_packet(this, com);
						break;
					}
					Municipality mun = ResManager.getMunicipality(y, y > -2);
					if(mun != null){
						mun.sync_packet(this, com);
						break;
					}
				}
				default: Missing.INST.sync_packet(this, com); break;
			}
			send(Side.CLIENT, com);
		}
		if(packet.hasKey("interact")){
			String index = packet.getString("interact");
			Chunk_ chunk = ResManager.getChunk(player);
			switch(type){
				case MAIN:{
					Main.INST.on_interact(packet, index, player, chunk);
					break;
				}
				case CHUNK:{
					chunk.on_interact(packet, index, player, chunk);
					break;
				}
				case MUNICIPALITY:{
					if(x < 0){
						ResManager.getMunicipality(-1, true).on_interact(packet, index, player, chunk);
						break;
					}
					Municipality mun = ResManager.getMunicipality(y, y > -2);
					if(mun != null){
						mun.on_interact(packet, index, player, chunk);
						break;
					}
				}
				default: Missing.INST.on_interact(packet, index, player, chunk); break;
			}
		}
	}

	private void client_packet(NBTTagCompound packet, EntityPlayer player){
		if(!packet.hasKey("elements")) return;
		NBTTagList list = (NBTTagList)packet.getTag("elements");
		gui.clear();
		gui.sizeOf(list.tagCount());
		gui.title = new BasicText(gui.getGuiLeft() + 8, gui.getGuiTop() + 8, 196, 0x0e0e0e, "landdev.gui." + packet.getString("title_lang")).autoscale().translate();
		gui.add("title", gui.title);
		gui.elements().clear();
		if(packet.hasKey("title")) gui.title.string = String.format(gui.title.string, packet.getString("title"));
		int idx = 0;
		for(NBTBase base : list){
			NBTTagList lis = (NBTTagList)base;
			String index = lis.getStringTagAt(0);
			LDGuiElementType elm = LDGuiElementType.valueOf(lis.getStringTagAt(1));
			LDGuiElementType icon = LDGuiElementType.valueOf(lis.getStringTagAt(2));
			String bools = lis.getStringTagAt(3);
			String val = lis.tagCount() > 4 ? lis.getStringTagAt(4) : null;
			gui.addElm(index, elm, icon, idx++, bools.charAt(0) == '1', bools.charAt(1) == '1', bools.charAt(2) == '1', val);
			if(icon.is_checkbox()){
				checkboxes.put(index, icon.checkbox());
			}
		}
		if(packet.hasKey("form")) form = packet.getBoolean("form");
	}

	public Player player(){
		return player;
	}

	public boolean form(){
		return form;
	}
	
}