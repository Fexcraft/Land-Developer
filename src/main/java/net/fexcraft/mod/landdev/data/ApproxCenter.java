package net.fexcraft.mod.landdev.data;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;

public class ApproxCenter implements Saveable {
	
	public ChunkKey nw, ne, sw, se, cn;

	@Override
	public void save(JsonMap map){
		if(cn == null) return;
		map.addArray("approx-center");
		JsonArray array = new JsonArray();
		array.add(cn.x);
		array.add(cn.z);
		array.add(nw.x);
		array.add(nw.z);
		array.add(ne.x);
		array.add(ne.z);
		array.add(sw.x);
		array.add(sw.z);
		array.add(se.x);
		array.add(se.z);
		map.add("approx-center", array);
	}

	@Override
	public void load(JsonMap map){
		if(!map.has("approx-center")) return;
		JsonArray array = map.getArray("approx-center");
		cn = new ChunkKey(array.get(0).integer_value(), array.get(1).integer_value());
		nw = new ChunkKey(array.get(2).integer_value(), array.get(3).integer_value());
		ne = new ChunkKey(array.get(4).integer_value(), array.get(5).integer_value());
		sw = new ChunkKey(array.get(6).integer_value(), array.get(7).integer_value());
		se = new ChunkKey(array.get(8).integer_value(), array.get(9).integer_value());
	}
	
	public void update(int x, int z){
		
	}

}
