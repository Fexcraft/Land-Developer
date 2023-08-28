package net.fexcraft.mod.landdev.data;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.PermAction.PermActions;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.ResManager;

public class Manageable implements Saveable, PermInteractive {
	
	protected UUID manager;
	public TreeMap<UUID, Staff> staff = null;
	protected PermActions actions;
	protected String manager_name;
	
	public Manageable(boolean hasstaff, PermActions actions){
		if(hasstaff) staff = new TreeMap<>();
		this.actions = actions;
	}

	@Override
	public void save(JsonMap map){
		if(manager != null) map.add("manager", manager.toString());
		if(staff != null){
			JsonMap sta = map.getMap("staff");
			staff.forEach((key, staff) -> {
				JsonMap stf = new JsonMap();
				staff.save(stf);
				sta.add(key.toString(), stf);
			});
		}
	}

	@Override
	public void load(JsonMap map){
		manager = map.getUUID("manager", null);
		if(staff != null && map.has("staff")){
			staff.clear();
			map.getMap("staff").value.forEach((key, val) -> {
				Staff stf = new Staff(UUID.fromString(key), actions);
				stf.load(val.asMap());
				staff.put(stf.uuid, stf);
			});
		}
	}
	
	public boolean isManager(UUID uuid){
		return uuid.equals(manager);
	}
	
	public boolean isStaff(UUID uuid){
		return staff == null ? false : staff.get(uuid) != null;
	}
	
	public static class Staff implements Saveable {
		
		protected HashMap<PermAction, Boolean> actions = new HashMap<>();
		protected UUID uuid;
		
		public Staff(UUID uiid, PermActions pacts){
			uuid = uiid;
			for(PermAction action : pacts.actions){
				actions.put(action, true);
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
		
	}

	@Override
	public boolean can(PermAction act, UUID uuid){
		if(!actions.isValid(act)) return false;
		if(manager != null && manager.equals(uuid)) return true;
		Staff sta = staff.get(uuid);
		return sta != null && sta.actions.get(act);
	}

	@Override
	public boolean can(UUID uuid, PermAction... acts){
		for(PermAction act : acts){
			if(!actions.isValid(act)) continue;
			if(manager != null && manager.equals(uuid)) return true;
			Staff sta = staff.get(uuid);
			if(sta != null && sta.actions.get(act)) return true;
		}
		return false;
	}

	public void clear(){
		manager = null;
		if(staff != null) staff.clear();
	}

	public void add(Player player){
		if(staff == null) return;
		staff.put(player.uuid, new Staff(player.uuid, actions));
	}

	public void setManager(Player player){
		manager = player.uuid;
	}

	public void setManager(UUID uuid){
		manager = uuid;
	}

	public String getManagerName(){
		if(manager == null) return "none";
		return ResManager.getPlayerName(manager);
	}

	public boolean hasManager(){
		return manager != null;
	}

	public void setNoManager(){
		manager = null;
	}

	public PermAction[] actions(){
		return actions.actions;
	}

}
