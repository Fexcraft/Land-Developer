package net.fexcraft.mod.landdev.db;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;
import net.minecraft.nbt.*;

import java.util.Map;

/**
 * @author Ferdnand Calo' (FEX___96)
 */
public class JsonNBTConverter {

    public static TagCW map(TagCW compound, JsonMap map){
		for(Map.Entry<String, JsonValue<?>> entry : map.entries()){
			if(entry.getValue().isMap()){
				compound.set(entry.getKey(), map(TagCW.create(), entry.getValue().asMap()));
			}
			else if(entry.getValue().isArray()){
				compound.set(entry.getKey(), array(TagLW.create(), entry.getValue().asArray()));
			}
			else{
				if(entry.getValue().value instanceof Long){
					compound.set(entry.getKey(), entry.getValue().long_value());
				}
				else if(entry.getValue().value instanceof Float){
					compound.set(entry.getKey(), entry.getValue().float_value());
				}
				else if(entry.getValue().value instanceof Integer){
					compound.set(entry.getKey(), entry.getValue().integer_value());
				}
				else if(entry.getValue().value instanceof Boolean){
					compound.set(entry.getKey(), entry.getValue().bool());
				}
				else compound.set(entry.getKey(), entry.getValue().string_value());
			}
		}
		return compound;
	}

	public static TagLW array(TagLW list, JsonArray array){
		for(JsonValue val : array.value){
			if(val.isMap()){
				list.add(map(TagCW.create(), val.asMap()));
			}
			else if(val.value instanceof Long){
				list.add(val.long_value());
			}
			else if(val.value instanceof Float){
				list.add(val.float_value());
			}
			else if(val.value instanceof Integer){
				list.add(val.integer_value());
			}
			else if(val.value instanceof Boolean){
				list.add((byte)(val.bool() ? 1 : 0));
			}
			else list.add(val.string_value());
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
