package net.fexcraft.mod.landdev.db;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.landdev.data.chunk.cap.ChunkCap;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import java.util.Map;

import static net.fexcraft.mod.landdev.data.chunk.cap.ChunkCap.CHUNK;

/**
 * @author Ferdnand Calo' (FEX___96)
 */
public class JsonNBTConverter {

    public static NBTTagCompound map(NBTTagCompound compound, JsonMap map){
		for(Map.Entry<String, JsonValue<?>> entry : map.entries()){
			if(entry.getValue().isMap()){
				compound.setTag(entry.getKey(), map(new NBTTagCompound(), entry.getValue().asMap()));
			}
			else if(entry.getValue().isArray()){
				compound.setTag(entry.getKey(), array(new NBTTagList(), entry.getValue().asArray()));
			}
			else{
				if(entry.getValue().value instanceof Long){
					compound.setLong(entry.getKey(), entry.getValue().long_value());
				}
				else if(entry.getValue().value instanceof Float){
					compound.setFloat(entry.getKey(), entry.getValue().float_value());
				}
				else if(entry.getValue().value instanceof Integer){
					compound.setInteger(entry.getKey(), entry.getValue().integer_value());
				}
				else if(entry.getValue().value instanceof Boolean){
					compound.setBoolean(entry.getKey(), entry.getValue().bool());
				}
				else compound.setString(entry.getKey(), entry.getValue().string_value());
			}
		}
		return compound;
	}

	public static NBTTagList array(NBTTagList list, JsonArray array){
		for(JsonValue val : array.value){
			if(val.isMap()){
				list.appendTag(map(new NBTTagCompound(), val.asMap()));
			}
			else if(val.value instanceof Long){
				list.appendTag(new NBTTagLong(val.long_value()));
			}
			else if(val.value instanceof Float){
				list.appendTag(new NBTTagFloat(val.float_value()));
			}
			else if(val.value instanceof Integer){
				list.appendTag(new NBTTagInt(val.integer_value()));
			}
			else if(val.value instanceof Boolean){
				list.appendTag(new NBTTagByte((byte)(val.bool() ? 1 : 0)));
			}
			else list.appendTag(new NBTTagString(val.string_value()));
		}
		return list;
	}

	public static JsonMap demap(NBTTagCompound com){
		JsonMap map = new JsonMap();
		for(String key : com.getKeySet()){
			NBTBase base = com.getTag(key);
			if(base instanceof NBTTagCompound){
				map.add(key, demap((NBTTagCompound)base));
			}
			else if(base instanceof NBTTagList){
				map.add(key, dearray((NBTTagList)base));
			}
			else if(base instanceof NBTTagLong){
				map.add(key, ((NBTTagLong)base).getLong());
			}
			else if(base instanceof NBTTagFloat){
				map.add(key, ((NBTTagFloat)base).getFloat());
			}
			else if(base instanceof NBTTagInt){
				map.add(key, ((NBTTagInt)base).getInt());
			}
			else if(base instanceof NBTTagByte){
				map.add(key, ((NBTTagByte)base).getByte() > 0);
			}
			else if(base instanceof NBTTagString){
				map.add(key, ((NBTTagString)base).getString());
			}
		}
		return map;
	}

	public static JsonArray dearray(NBTTagList list){
		JsonArray array = new JsonArray();
		for(NBTBase base : list){
			if(base instanceof NBTTagCompound){
				array.add(demap((NBTTagCompound)base));
			}
			else if(base instanceof NBTTagList){
				array.add(dearray((NBTTagList)base));
			}
			else if(base instanceof NBTTagLong){
				array.add(((NBTTagLong)base).getLong());
			}
			else if(base instanceof NBTTagFloat){
				array.add(((NBTTagFloat)base).getFloat());
			}
			else if(base instanceof NBTTagInt){
				array.add(((NBTTagInt)base).getInt());
			}
			else if(base instanceof NBTTagByte){
				array.add(((NBTTagByte)base).getByte() > 0);
			}
			else if(base instanceof NBTTagString){
				array.add(((NBTTagString)base).getString());
			}
		}
		return array;
	}

}
