package net.fexcraft.mod.landdev.data.chunk.cap;

/**
 *
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ChunkCapCallable implements java.util.concurrent.Callable<ChunkCap> {

	@Override
	public ChunkCap call() throws Exception {
		return new ChunkCapImpl();
	}

}
