package net.fexcraft.mod.landdev.data.prop;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.chunk.ChunkLabel;
import net.fexcraft.mod.landdev.data.chunk.ChunkOwner;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.hooks.ExternalData;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.player.SpaceDefinitionCache;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;

import static net.fexcraft.mod.landdev.ui.LDUIButton.OPEN;
import static net.fexcraft.mod.landdev.ui.LDUIRow.*;
import static net.fexcraft.mod.landdev.ui.LDUIRow.ELM_BLUE;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Property implements Saveable, Layer, LDUIModule {

	public final int id;
	public Createable created = new Createable(true);
	public ChunkOwner owner = new ChunkOwner();
	public Sellable sell = new Sellable(this);
	public ExternalData external = new ExternalData(this);
	public ChunksIn chunks_in = new ChunksIn();
	public ChunkLabel label = new ChunkLabel();
	public V3I start = new V3I();
	public V3I end = new V3I();

	public Property(int ir){
		id = ir;
	}

	@Override
	public void save(JsonMap map){
		map.add("id", id);
		map.add("start", new JsonArray.Flat(start.x, start.y, start.z));
		map.add("end", new JsonArray.Flat(end.x, end.y, end.z));
		created.save(map);
		owner.save(map);
		sell.save(map);
		chunks_in.save(map);
		label.save(map);
		external.save(this, map);
	}

	@Override
	public void load(JsonMap map){
		JsonArray arr = map.getArray("start");
		start.set(arr.get(0).integer_value(), arr.get(1).integer_value(), arr.get(2).integer_value());
		arr = map.getArray("end");
		end.set(arr.get(0).integer_value(), arr.get(1).integer_value(), arr.get(2).integer_value());
		created.load(map);
		owner.load(map);
		sell.load(map);
		chunks_in.load(map);
		label.load(map);
		external.load(this, map);
	}

	@Override
	public String saveId(){
		return id + "";
	}

	@Override
	public String saveTable(){
		return "props";
	}

	@Override
	public void gendef(){
		external.gendef(this);
	}

	@Override
	public Layers getLayer(){
		return Layers.PROPERTY;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.CHUNK;
	}

	@Override
	public void sync_packet(BaseCon container, ModuleResponse resp){
		resp.setTitle("property.title");
		boolean canman = owner.isPropMan(container.ldp) || container.ldp.adm;
		switch(container.pos.x){
			case UI_MAIN:{

				break;
			}
			case UI_CREATE:{
				Chunk_ ck = container.ldp.chunk_current;
				resp.setTitle("property.create.title");
				resp.addBlank();
				resp.addRow("create.info", ELM_YELLOW);
				Layers ow = ck.owner.layer();
				if(ow.is(Layers.NONE)) ow = ck.district.getParentLayer();
				resp.addRow("create.owner_type", ELM_BLUE, ow);
				resp.addBlank();
				resp.addButton("create.submit", ELM_BLUE, OPEN);
				resp.setFormular();
				resp.setNoBack();
				break;
			}
		}
		external.sync_packet(container, resp);
	}

	@Override
	public void on_interact(BaseCon container, ModuleRequest req){
		boolean canman = owner.isPropMan(container.ldp) || container.ldp.adm;
		LDPlayer player = container.ldp;
		switch(req.event()){
			case "create.submit":{
				player.defcache = new SpaceDefinitionCache(player.entity.getV3I());
				player.entity.openUI(LDKeys.DEF_SPACE, player.defcache.pos);
				break;
			}
		}
	}

	public boolean isInside(V3I pos){
		if(pos.x >= start.x && pos.x <= end.x){
			if(pos.y >= start.y && pos.y <= end.y){
				if(pos.z >= start.z && pos.z <= end.z){
					return true;
				}
			}
		}
		return false;
	}

}
