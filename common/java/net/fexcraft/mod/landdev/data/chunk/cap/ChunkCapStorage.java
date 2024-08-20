package net.fexcraft.mod.landdev.data.chunk.cap;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.db.JsonNBTConverter;
import net.fexcraft.mod.uni.tag.TagCW;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

/**
 *
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ChunkCapStorage implements IStorage<ChunkCap> {

	@Override
	public NBTBase writeNBT(Capability<ChunkCap> capability, ChunkCap instance, EnumFacing side){
		NBTTagCompound compound = new NBTTagCompound();
		JsonMap map = new JsonMap();
		instance.getChunk().save(map);
		JsonNBTConverter.map(TagCW.wrap(compound), map);
		return compound;
	}

	@Override
	public void readNBT(Capability<ChunkCap> capability, ChunkCap instance, EnumFacing side, NBTBase nbt){
		if(nbt == null){
			instance.getChunk().gendef();
			return;
		}
		JsonMap map = JsonNBTConverter.demap((NBTTagCompound)nbt);
		if(map.empty()) instance.getChunk().gendef();
		else instance.getChunk().load(map);
	}

}
