package net.fexcraft.mod.landdev.data;

import java.util.LinkedHashMap;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.norm.Norm;

public class Norms implements Saveable {
	
	public LinkedHashMap<String, Norm> norms = new LinkedHashMap<>();
	
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

	public int index(Norm norm){
		int idx = 0;
		for(Norm nor : norms.values()){
			if(nor == norm) return idx;
			idx++;
		}
		return 0;
	}

	public Norm get(int index){
		int idx = 0;
		for(Norm norm : norms.values()){
			if(idx == index) return norm;
			idx++;
		}
		return null;
	}

}
