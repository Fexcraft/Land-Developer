package net.fexcraft.mod.landdev.gui.modules;

import static net.fexcraft.mod.landdev.LandDev.INSTANCE;
import static net.fexcraft.mod.landdev.gui.GuiHandler.CHUNK;
import static net.fexcraft.mod.landdev.gui.GuiHandler.COMPANY;
import static net.fexcraft.mod.landdev.gui.GuiHandler.DISTRICT;
import static net.fexcraft.mod.landdev.gui.GuiHandler.MAILBOX;
import static net.fexcraft.mod.landdev.gui.GuiHandler.MUNICIPALITY;
import static net.fexcraft.mod.landdev.gui.GuiHandler.PLAYER;
import static net.fexcraft.mod.landdev.gui.GuiHandler.PROPERTY;
import static net.fexcraft.mod.landdev.gui.GuiHandler.STATE;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_GENERIC;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_OPEN;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class Main implements LDGuiModule {
	
	public static Main INST = new Main();

	@Override
	public void sync_packet(LDGuiContainer container, NBTTagCompound com){
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
	}

	public void on_interact(NBTTagCompound packet, String index, EntityPlayer player, Chunk_ chunk){
		switch(index){
			case "player": player.openGui(INSTANCE, PLAYER, player.world, 0, 0, 0); return;
			case "mail": player.openGui(INSTANCE, MAILBOX, player.world, 0, 0, 0); return;
			case "property": player.openGui(INSTANCE, PROPERTY, player.world, 0, 0, 0); return;
			case "company": player.openGui(INSTANCE, COMPANY, player.world, 0, 0, 0); return;
			case "chunk": player.openGui(INSTANCE, CHUNK, player.world, 0, chunk.key.x, chunk.key.z); return;
			case "district": player.openGui(INSTANCE, DISTRICT, player.world, 0, chunk.district.id, 0); return;
			case "municipality":{
				if(!chunk.district.owner.county){
					player.openGui(INSTANCE, MUNICIPALITY, player.world, 0, chunk.district.owner.municipality.id, 0);
				}
				else{
					Print.chat(player, TranslationUtil.translate("district.not_part_of_municipality"));
					player.closeScreen();
				}
				return;
			}
			case "county":{
				player.openGui(INSTANCE, MUNICIPALITY, player.world, 0, chunk.district.owner.county_id(), 0);
				return;
			}
			case "state": player.openGui(INSTANCE, STATE, player.world, 0, 0, 0); return;
		}
	}

}