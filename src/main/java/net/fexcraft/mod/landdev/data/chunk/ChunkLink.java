package net.fexcraft.mod.landdev.data.chunk;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonObject;
import net.fexcraft.mod.landdev.data.Saveable;

public class ChunkLink implements Saveable {

	public ArrayList<ChunkKey> linked;
	public ChunkKey root_key;
	public Chunk_ chunk;

	public ChunkLink(Chunk_ chunk_){
		chunk = chunk_;
	}

	@Override
	public void save(JsonMap map){
		if(linked.isEmpty()) return;
		if(root_key.equals(chunk.key)){
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
		JsonObject<?> elm = map.get("linked");
		if(elm.isArray()){
			linked = new ArrayList<>();
			for(JsonObject<?> obj : elm.asArray().value){
				linked.add(new ChunkKey(obj.value.toString()));
			}
		}
		else{
			root_key = new ChunkKey(map.getString("linked", null));
		}
	}

}
