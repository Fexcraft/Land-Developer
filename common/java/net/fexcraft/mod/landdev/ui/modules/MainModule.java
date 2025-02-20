package net.fexcraft.mod.landdev.ui.modules;

import static net.fexcraft.mod.landdev.ui.LDKeys.*;
import static net.fexcraft.mod.landdev.ui.LDKeys.COUNTY;
import static net.fexcraft.mod.landdev.ui.LDUIButton.OPEN;
import static net.fexcraft.mod.landdev.ui.LDUIRow.ELM_GENERIC;

import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.TranslationUtil;

public class MainModule implements LDUIModule {
	
	public static MainModule INST = new MainModule();

	@Override
	public void sync_packet(BaseCon container, ModuleResponse resp){
		resp.setTitle("main.title");
		resp.addButton("player", ELM_GENERIC, OPEN);
		resp.addButton("mail", ELM_GENERIC, OPEN);
		resp.addButton("property", ELM_GENERIC, OPEN);
		resp.addButton("company", ELM_GENERIC, OPEN);
		resp.addBlank();
		resp.addButton("chunk", ELM_GENERIC, OPEN);
		resp.addButton("district", ELM_GENERIC, OPEN);
		resp.addButton("municipality", ELM_GENERIC, OPEN);
		resp.addButton("county", ELM_GENERIC, OPEN);
		resp.addButton("state", ELM_GENERIC, OPEN);
	}

	public void on_interact(BaseCon container, ModuleRequest req){
		Chunk_ chunk = ResManager.getChunk(container.pos.y, container.pos.z);
		switch(req.event()){
			case "player": container.open(PLAYER, 0, 0, 0); return;
			case "mail": container.open(MAILBOX, Layers.PLAYER.ordinal(), 0, 0); return;
			case "property": container.open(PROPERTY, 0, 0, 0); return;
			case "company": container.open(COMPANY, 0, 0, 0); return;
			case "chunk": container.open(CHUNK, 0, chunk.key.x, chunk.key.z); return;
			case "district": container.open(DISTRICT, 0, chunk.district.id, 0); return;
			case "municipality":{
				if(!chunk.district.owner.is_county){
					container.open(MUNICIPALITY, 0, chunk.district.owner.municipality.id, 0);
				}
				else{
					container.player.entity.send(TranslationUtil.translate("district.not_part_of_municipality"));
					container.player.entity.closeUI();
				}
				return;
			}
			case "county":{
				container.open(COUNTY, 0, chunk.district.owner.county_id(), 0);
				return;
			}
			case "state": container.open(STATE, 0, chunk.district.state().id, 0); return;
		}
	}

}
