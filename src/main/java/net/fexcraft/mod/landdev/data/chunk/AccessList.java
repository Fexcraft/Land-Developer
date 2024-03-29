package net.fexcraft.mod.landdev.data.chunk;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.landdev.data.Saveable;

/**
 * For Chunks and Properties
 * 
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class AccessList implements Saveable {
	
	public HashMap<UUID, Long> players = new HashMap<>();
	public HashMap<Integer, Long> companies = new HashMap<>();
	public boolean interact;

	@Override
	public void save(JsonMap map){
		if(players.size() > 0){
			JsonMap pm = new JsonMap();
			for(Entry<UUID, Long> entry : players.entrySet()){
				pm.add(entry.getKey().toString(), entry.getValue());
			}
			map.add("al_players", pm);
		}
		if(companies.size() > 0){
			JsonMap cm = new JsonMap();
			for(Entry<Integer, Long> entry : companies.entrySet()){
				cm.add(entry.getKey() + "", entry.getValue());
			}
			map.add("al_companies", cm);
		}
		map.add("interact", interact);
	}

	@Override
	public void load(JsonMap map){
		if(map.has("al_players")){
			JsonMap pm = map.getMap("al_players");
			for(Entry<String, JsonValue<?>> entry : pm.entries()){
				players.put(UUID.fromString(entry.getKey()), entry.getValue().long_value());
			}
		}
		if(map.has("al_companies")){
			JsonMap cm = map.getMap("al_companies");
			for(Entry<String, JsonValue<?>> entry : cm.entries()){
				companies.put(Integer.parseInt(entry.getKey()), entry.getValue().long_value());
			}
		}
		interact = map.getBoolean("interact", interact);
	}

}
