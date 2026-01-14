package net.fexcraft.mod.landdev.data.prop;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.chunk.ChunkLabel;
import net.fexcraft.mod.landdev.data.chunk.ChunkOwner;
import net.fexcraft.mod.landdev.data.hooks.ExternalData;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;

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

	public Property(int ir){
		id = ir;
	}

	@Override
	public void save(JsonMap map){
		map.add("id", id);
		created.save(map);
		owner.save(map);
		sell.save(map);
		chunks_in.save(map);
		label.save(map);
		external.save(this, map);
	}

	@Override
	public void load(JsonMap map){
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
		boolean canman = owner.is(container.ldp) || container.ldp.adm;
		switch(container.pos.x){
			case UI_MAIN:{

				break;
			}
			case UI_CREATE:{

				break;
			}
		}
		external.sync_packet(container, resp);
	}

	@Override
	public void on_interact(BaseCon container, ModuleRequest req){

	}

}
