package net.fexcraft.mod.landdev.data.chunk.cap;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.world.chunk.Chunk;

/**
 *
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ChunkCapImpl implements ChunkCap {

	private Chunk_ chunk;

	@Override
	public void setChunk(Chunk ck){
		chunk = new Chunk_(ck);
		chunk.load(new JsonMap());
		ResManager.CHUNKS.put(chunk.key, chunk);
	}

	@Override
	public Chunk_ getChunk(){
		return chunk;
	}

}
