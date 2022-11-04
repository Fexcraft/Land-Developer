package net.fexcraft.mod.landdev.data;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.PermAction.PermActions;
import net.fexcraft.mod.landdev.data.norm.Norm;
import net.fexcraft.mod.landdev.data.player.Player;

public class Citizens implements Saveable, PermInteractive {
	
	protected TreeMap<UUID, Citizen> citizens = new TreeMap<>();
	protected PermActions actions;
	public Norms norms = new Norms();
	
	public Citizens(PermActions actions){
		this.actions = actions;
	}

	@Override
	public void save(JsonMap map){
		JsonMap sta = map.getMap("citizen");
		citizens.forEach((key, citizen) -> {
			JsonMap cit = new JsonMap();
			citizen.save(cit);
			sta.add(key.toString(), cit);
		});
	}

	@Override
	public void load(JsonMap map){
		if(!map.has("citizen")) return;
		citizens.clear();
		map.getMap("citizen").value.forEach((key, val) -> {
			Citizen cit = new Citizen(UUID.fromString(key), norms);
			cit.load(val.asMap());
			citizens.put(cit.uuid, cit);
		});
	}
	
	public boolean isCitizen(UUID uuid){
		return citizens.get(uuid) != null;
	}
	
	public static class Citizen implements Saveable {
		
		protected HashMap<String, Norm> norms = new HashMap<>();
		protected UUID uuid;
		
		public Citizen(UUID uiid, Norms nerms){
			uuid = uiid;
			nerms.norms.forEach((key, norm) -> norms.put(key, norm.copy()));
		}

		@Override
		public void save(JsonMap map){
			norms.forEach((key, val) -> map.add(key, val.save()));
		}
		
		@Override
		public void load(JsonMap map){
			map.entries().forEach(entry -> {
				Norm norm = norms.get(entry.getKey());
				if(norm != null) norm.load(entry.getValue());
			});
		}

		public boolean isNormTrue(String key){
			Norm norm = norms.get(key);
			return norm != null && norm.bool();
		}
		
	}

	@Override
	public boolean can(PermAction act, UUID uuid){
		if(!actions.isValid(act)) return false;
		Citizen cit = citizens.get(uuid);
		return cit.norms.get(act.norm).bool();
	}

	@Override
	public boolean can(UUID uuid, PermAction... acts){
		for(PermAction act : acts){
			if(!actions.isValid(act)) continue;
			Citizen cit = citizens.get(uuid);
			if(cit.norms.get(act.norm).bool()) return true;
		}
		return false;
	}

	public void remove(Player player){
		citizens.remove(player.uuid);
	}

	public void add(Player player){
		citizens.put(player.uuid, new Citizen(player.uuid, norms));
	}

}
