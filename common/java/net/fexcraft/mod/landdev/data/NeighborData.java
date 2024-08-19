package net.fexcraft.mod.landdev.data;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class NeighborData implements Saveable {
	
	private ArrayList<Integer> neighbors = new ArrayList<>();

	@Override
	public void load(JsonMap map){
		if(map.has("neighbors")) neighbors = map.getArray("neighbors").toIntegerList();
	}

	@Override
	public void save(JsonMap map){
		if(neighbors.size() > 0){
			JsonArray array = new JsonArray();
			for(int n : neighbors) array.add(n);
			map.add("neighbors", array);
		}
	}
	
	public ArrayList<Integer> get(){
		return neighbors;
	}

	public boolean contains(int id){
		return neighbors.contains(id);
	}

	public void add(int id){
		neighbors.add(id);
	}

	public int get(int integer){
		return neighbors.get(integer);
	}

	public int size(){
		return neighbors.size();
	}

}
