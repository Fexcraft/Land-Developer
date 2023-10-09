package net.fexcraft.mod.landdev.data.chunk.cap;

import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 *
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public interface ChunkCap {

	@CapabilityInject(ChunkCap.class)
	public static final Capability<ChunkCap> CHUNK = null;
    public static final ResourceLocation REGNAME = new ResourceLocation("landdev:chunk");
    
    public void setChunk(Chunk chunk);
    
    public Chunk_ getChunk();

}
