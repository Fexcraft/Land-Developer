package net.fexcraft.mod.landdev.data.chunk.cap;

import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.world.chunk.Chunk;

/**
 *
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ChunkCapImpl implements ChunkCap {

	private Chunk chunk;
	private Chunk_ chunk_;

	@Override
	public void setChunk(Chunk ck){
		chunk_ = new Chunk_(chunk = ck);
		ResManager.CHUNKS.put(chunk_.key, chunk_);
	}

	@Override
	public Chunk_ getChunk(){
		return chunk_;
	}

}
