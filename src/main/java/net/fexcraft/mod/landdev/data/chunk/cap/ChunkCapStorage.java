package net.fexcraft.mod.landdev.data.chunk.cap;

import java.util.Map.Entry;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.landdev.db.JsonNBTConverter;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
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
		JsonNBTConverter.map(compound, map);
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
