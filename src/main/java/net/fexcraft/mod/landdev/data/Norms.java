package net.fexcraft.mod.landdev.data;

import java.util.HashMap;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.norm.Norm;

public class Norms implements Saveable {
	
	public HashMap<String, Norm> norms = new HashMap<>();
	
	public void add(Norm norm){
		norms.put(norm.id, norm);
	}

	@Override
	public void save(JsonMap map){
		JsonMap nrms = new JsonMap();
		norms.entrySet().forEach(entry -> {
			nrms.add(entry.getKey(), entry.getValue().save());
		});
		map.add("norms", nrms);
	}

	@Override
	public void load(JsonMap map){
		if(!map.has("norms")) return;
		JsonMap nrms = map.getMap("norms");
		nrms.entries().forEach(entry -> {
			Norm norm = norms.get(entry.getKey());
			if(norm != null) norm.load(entry.getValue());
		});
	}

	public Norm get(String key){
		return norms.get(key);
	}

}
