package net.fexcraft.mod.landdev.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.PermAction.PermActions;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.ResManager;

public class Citizens implements Saveable, PermInteractive {
	
	protected LinkedHashMap<UUID, Citizen> citizens = new LinkedHashMap<>();
	protected PermActions actions;
	
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
			Citizen cit = new Citizen(UUID.fromString(key), actions);
			cit.load(val.asMap());
			citizens.put(cit.uuid, cit);
		});
	}
	
	public boolean isCitizen(UUID uuid){
		return citizens.get(uuid) != null;
	}

	public int size(){
		return citizens.size();
	}

	public Map<UUID, Citizen> map(){
		return citizens;
	}

	public int indexOf(UUID uuid){
		int idx = 0;
		for(UUID uid : citizens.keySet()){
			if(uuid.equals(uid)) return idx;
			idx++;
		}
		return 0;
	}

	public Citizen get(int index){
		int idx = 0;
		for(Citizen cit : citizens.values()){
			if(idx == index) return cit;
			idx++;
		}
		return null;
	}

	public Citizen get(UUID uuid){
		return citizens.get(uuid);
	}

	public static class Citizen implements Saveable {
		
		public LinkedHashMap<PermAction, Boolean> actions = new LinkedHashMap<>();
		public final UUID uuid;
		
		public Citizen(UUID uiid, PermActions pacts){
			uuid = uiid;
			for(PermAction action : pacts.actions){
				actions.put(action, false);
			}
		}

		@Override
		public void save(JsonMap map){
			actions.forEach((key, val) -> map.add(key.name(), val));
		}
		
		@Override
		public void load(JsonMap map){
			map.entries().forEach(entry -> {
				PermAction act = PermAction.get(entry.getKey());
				if(actions.containsKey(act)){
					actions.put(act, entry.getValue().bool());
				}
			});
		}

		public String getPlayerName(){
			return ResManager.getPlayerName(uuid);
		}

	}

	@Override
	public boolean can(PermAction act, UUID uuid){
		if(!actions.isValid(act)) return false;
		Citizen cit = citizens.get(uuid);
		return cit != null && cit.actions.get(act);
	}

	@Override
	public boolean can(UUID uuid, PermAction... acts){
		for(PermAction act : acts){
			if(!actions.isValid(act)) continue;
			Citizen cit = citizens.get(uuid);
			if(cit.actions.get(act)) return true;
		}
		return false;
	}

	public void remove(Player player){
		citizens.remove(player.uuid);
	}

	public void add(Player player){
		citizens.put(player.uuid, new Citizen(player.uuid, actions));
	}

}
