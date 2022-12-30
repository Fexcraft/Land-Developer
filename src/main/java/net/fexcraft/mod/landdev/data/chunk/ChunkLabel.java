package net.fexcraft.mod.landdev.data.chunk;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.Saveable;

public class ChunkLabel implements Saveable {
	
	public boolean present;
	public String label;

	@Override
	public void save(JsonMap map){
		if(present) map.add("label", label);
	}

	@Override
	public void load(JsonMap map){
		if(present = map.has("label")) label = map.getString("label", null);
	}

}
