package net.fexcraft.mod.landdev.data.prop;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.util.ResManager;

import java.util.HashSet;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class PropHolder implements Saveable {

	public HashSet<Property> properties = new HashSet<>();

	@Override
	public void save(JsonMap map){
		JsonArray arr = new JsonArray.Flat();
		for(Property prop : properties) arr.add(prop.id);
		map.add("properties", arr);
	}

	@Override
	public void load(JsonMap map){
		if(!map.has("properties")) return;
		properties.clear();
		JsonArray arr = map.getArray("properties");
		for(JsonValue<?> val : arr.value){
			if(val.integer_value() < 0) continue;
			properties.add(ResManager.getProperty(val.integer_value()));
		}
	}

}
