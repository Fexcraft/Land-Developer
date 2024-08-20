package net.fexcraft.mod.landdev.db;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;
import net.fexcraft.mod.uni.tag.TagType;

import java.util.Map;

/**
 * @author Ferdnand Calo' (FEX___96)
 */
public class JsonTagConverter {

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

	public static JsonMap demap(TagCW com){
		JsonMap map = new JsonMap();
		TagType type;
		for(String key : com.keys()){
			type = com.getType(key);
			switch(type){
				case COMPOUND:
					map.add(key, demap(com.getCompound(key)));
					break;
				case LIST:
					map.add(key, dearray(com.getList(key)));
					break;
				case STRING:
					map.add(key, com.getString(key));
					break;
				case LONG:
					map.add(key, com.getLong(key));
					break;
				case INT:
					map.add(key, com.getInteger(key));
					break;
				case BYTE:
					map.add(key, com.getBoolean(key));
					break;
				case FLOAT:
					map.add(key, com.getFloat(key));
					break;
				case DOUBLE:
					map.add(key, com.getDouble(key));
					break;
				case UNKNOWN:
				default:
					break;
			}
		}
		return map;
	}

	public static JsonArray dearray(TagLW list){
		JsonArray array = new JsonArray();
		TagType type;
		for(int idx = 0; idx < list.size(); idx++){
			type = list.getType(idx);
			switch(type){
				case COMPOUND:
					array.add(demap(list.getCompound(idx)));
					break;
				case STRING:
					array.add(list.getString(idx));
					break;
				case INT:
					array.add(list.getInteger(idx));
					break;
				case FLOAT:
					array.add(list.getFloat(idx));
					break;
				case DOUBLE:
					array.add(list.getDouble(idx));
					break;
				case UNKNOWN:
				default:
					break;
			}
		}
		return array;
	}

}
