package net.fexcraft.mod.landdev.gui.modules;

import static net.fexcraft.mod.landdev.gui.GuiHandler.*;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_GENERIC;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_OPEN;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.minecraft.nbt.NBTTagCompound;

public class Main implements LDGuiModule {
	
	public static Main INST = new Main();

	@Override
	public void sync_packet(LDGuiContainer container, ModuleResponse resp){
		resp.setTitle("main.title");
		resp.addButton("player", ELM_GENERIC, ICON_OPEN);
		resp.addButton("mail", ELM_GENERIC, ICON_OPEN);
		resp.addButton("property", ELM_GENERIC, ICON_OPEN);
		resp.addButton("company", ELM_GENERIC, ICON_OPEN);
		resp.addBlank();
		resp.addButton("chunk", ELM_GENERIC, ICON_OPEN);
		resp.addButton("district", ELM_GENERIC, ICON_OPEN);
		resp.addButton("municipality", ELM_GENERIC, ICON_OPEN);
		resp.addButton("county", ELM_GENERIC, ICON_OPEN);
		resp.addButton("state", ELM_GENERIC, ICON_OPEN);
	}

	public void on_interact(LDGuiContainer container, Player player, NBTTagCompound packet, String index){
		Chunk_ chunk = ResManager.getChunk(container.y, container.z);
		switch(index){
			case "player": player.openGui(PLAYER, 0, 0, 0); return;
			case "mail": player.openGui(MAILBOX, 0, 0, 0); return;
			case "property": player.openGui(PROPERTY, 0, 0, 0); return;
			case "company": player.openGui(COMPANY, 0, 0, 0); return;
			case "chunk": player.openGui(CHUNK, 0, chunk.key.x, chunk.key.z); return;
			case "district": player.openGui(DISTRICT, 0, chunk.district.id, 0); return;
			case "municipality":{
				if(!chunk.district.owner.is_county){
					player.openGui(MUNICIPALITY, 0, chunk.district.owner.municipality.id, 0);
				}
				else{
					Print.chat(player.entity, TranslationUtil.translate("district.not_part_of_municipality"));
					player.entity.closeScreen();
				}
				return;
			}
			case "county":{
				player.openGui(COUNTY, 0, chunk.district.owner.county_id(), 0);
				return;
			}
			case "state": player.openGui(STATE, 0, chunk.district.state().id, 0); return;
		}
	}

}
