package net.fexcraft.mod.landdev.data.chunk;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface ChunkCap {

    public static final ResourceLocation REGNAME = new ResourceLocation("landdev:chunk");
	@CapabilityInject(ChunkCap.class)
	public static final Capability<ChunkCap> CHUNK = null;
    
    public Chunk getChunk();
    
    public void setChunk(Chunk chunk);
    
    public Chunk_ getLDChunk();

    public Chunk_ getLDChunk(boolean allownull);

}
