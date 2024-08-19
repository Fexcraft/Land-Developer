package net.fexcraft.mod.landdev.data.chunk;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.landdev.data.Saveable;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ChunkLink implements Saveable {

	public ArrayList<ChunkKey> linked;
	public ChunkKey root_key;
	public Chunk_ chunk;

	public ChunkLink(Chunk_ chunk_){
		chunk = chunk_;
	}

	@Override
	public void save(JsonMap map){
		if(linked != null){
			JsonArray array = new JsonArray();
			for(ChunkKey key : linked){
				array.add(key.toString());
			}
			map.add("linked", array);
		}
		else{
			map.add("linked", root_key.toString());
		}
	}

	@Override
	public void load(JsonMap map){
		JsonValue<?> elm = map.get("linked");
		if(elm.isArray()){
			linked = new ArrayList<>();
			for(JsonValue<?> obj : elm.asArray().value){
				linked.add(new ChunkKey(obj.value.toString()));
			}
		}
		else{
			root_key = new ChunkKey(map.getString("linked", null));
		}
	}

	public boolean validate(ChunkKey key){
		if(valid(key, chunk.key)) return true;
		for(ChunkKey link : linked) if(valid(key, link)) return true;
		return false;
	}

	private boolean valid(ChunkKey key, ChunkKey other){
		if(key.equals(new int[]{other.x + 1, other.z})) return true;
		if(key.equals(new int[]{other.x - 1, other.z})) return true;
		if(key.equals(new int[]{other.x, other.z + 1})) return true;
		if(key.equals(new int[]{other.x, other.z - 1})) return true;
		return false;
	}

}
