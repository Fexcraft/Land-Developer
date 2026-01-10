package net.fexcraft.mod.landdev.data.prop;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ChunksIn implements Saveable {

	public SortedSet<ChunkKey> chunks = new TreeSet<>();

	@Override
	public void save(JsonMap map){
		if(chunks.isEmpty()) return;
		JsonArray array = new JsonArray();
		for(ChunkKey key : chunks){
			array.add(key.toString());
		}
		map.add("chunks", array);
	}

	@Override
	public void load(JsonMap map){
		if(!map.has("chunks")) return;
		JsonArray array = map.getArray("chunks");
		for(JsonValue<?> val : array.value){
			chunks.add(new ChunkKey(val.string_value()));
		}
	}

}
