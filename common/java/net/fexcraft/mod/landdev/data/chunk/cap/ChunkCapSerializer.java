package net.fexcraft.mod.landdev.data.chunk.cap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import static net.fexcraft.mod.landdev.data.chunk.cap.ChunkCap.CHUNK;

/**
 *
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ChunkCapSerializer implements ICapabilitySerializable<NBTBase>{
    
    private ChunkCap instance;
    
    public ChunkCapSerializer(Chunk chunk){
        instance = CHUNK.getDefaultInstance();
        instance.setChunk(chunk);
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing){
        return capability == CHUNK;
    }
    
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing){
        return capability == CHUNK ? CHUNK.cast(this.instance) : null;
    }
    
    @Override
    public NBTBase serializeNBT(){
        return CHUNK.getStorage().writeNBT(CHUNK, instance, null);
    }
    
    @Override
    public void deserializeNBT(NBTBase nbt){
        CHUNK.getStorage().readNBT(CHUNK, instance, null, nbt);
    }

}
