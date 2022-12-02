package net.fexcraft.mod.landdev.data;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.PermAction.PermActions;
import net.fexcraft.mod.landdev.data.norm.Norm;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.ResManager;

public class Manageable implements Saveable, PermInteractive {
	
	protected UUID manager;
	protected TreeMap<UUID, Staff> staff = null;
	protected PermActions actions;
	protected String manager_name;
	public Norms norms = new Norms();
	
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
				Staff stf = new Staff(UUID.fromString(key), norms);
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
		
		protected HashMap<String, Norm> norms = new HashMap<>();
		protected UUID uuid;
		
		public Staff(UUID uiid, Norms nerms){
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
		if(manager != null && manager.equals(uuid)) return true;
		Staff sta = staff.get(uuid);
		return sta != null && sta.norms.get(act.norm).bool();
	}

	@Override
	public boolean can(UUID uuid, PermAction... acts){
		for(PermAction act : acts){
			if(!actions.isValid(act)) continue;
			if(manager != null && manager.equals(uuid)) return true;
			Staff sta = staff.get(uuid);
			if(sta != null && sta.norms.get(act.norm).bool()) return true;
		}
		return false;
	}

	public void clear(){
		manager = null;
		if(staff != null) staff.clear();
	}

	public void add(Player player){
		if(staff == null) return;
		staff.put(player.uuid, new Staff(player.uuid, norms));
	}

	public void setManager(Player player){
		manager = player.uuid;
	}

	public String getManagerName(){
		if(manager == null) return "none";
		return ResManager.getPlayerName(manager);
	}

	public boolean hasManager(){
		return manager != null;
	}

}
