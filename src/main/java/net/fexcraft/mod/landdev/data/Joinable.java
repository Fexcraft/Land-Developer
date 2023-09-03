package net.fexcraft.mod.landdev.data;

import java.util.TreeMap;
import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.player.Player;

public class Joinable implements Saveable {

	public TreeMap<UUID, Long> timeouts = new TreeMap<>();

	public Joinable(){}

	@Override
	public void save(JsonMap map){
		JsonMap req = map.getMap("timeouts");
		timeouts.forEach((key, time) -> {
			req.add(key.toString(), time);
		});
	}

	@Override
	public void load(JsonMap map){
		if(!map.has("requests")) return;
		timeouts.clear();
		map.getMap("citizen").value.forEach((key, val) -> {
			timeouts.put(UUID.fromString(key), val.long_value());
		});
	}

	public boolean contains(Player player){
		return timeouts.get(player.uuid) != null;
	}

	public long get(Player player){
		if(!timeouts.containsKey(player.uuid)) return -1;
		return timeouts.get(player.uuid);
	}

}
