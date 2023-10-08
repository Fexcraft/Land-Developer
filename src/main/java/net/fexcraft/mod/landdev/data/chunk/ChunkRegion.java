package net.fexcraft.mod.landdev.data.chunk;

import java.util.concurrent.ConcurrentHashMap;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.Saveable;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ChunkRegion implements Saveable {

	public final ChunkKey key;
	public ConcurrentHashMap<ChunkKey, Chunk_> chunks = new ConcurrentHashMap<>();

	public ChunkRegion(int x, int z){
		key = new ChunkKey(x, z);
	}

	public ChunkRegion(ChunkKey ckkey){
		key = ckkey;
	}

	@Override
	public void save(JsonMap map){
		map.add("last_save", Time.getAsString(Time.getDate()));
		JsonMap cks = new JsonMap();
		for(Chunk_ ck : chunks.values()){
			JsonMap ckmap = new JsonMap();
			ck.save(ckmap);
			cks.add(ck.key.toString(), ckmap);
		}
		map.add("chunks", cks);
	}

	@Override
	public void load(JsonMap map){
		//
	}

	@Override
	public String saveId(){
		return "r_" + key.toString();
	}

	@Override
	public String saveTable(){
		return "chunks";
	}

}
