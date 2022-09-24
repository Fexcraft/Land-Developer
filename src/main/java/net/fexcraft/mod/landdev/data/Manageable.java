package net.fexcraft.mod.landdev.data;

import java.util.ArrayList;
import java.util.UUID;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;

public class Manageable implements Saveable {
	
	public UUID manager;
	public ArrayList<UUID> staff = null;
	
	public Manageable(boolean hasstaff){
		if(hasstaff) staff = new ArrayList<>();
	}

	@Override
	public void save(JsonMap map){
		if(manager != null) map.add("manager", manager.toString());
		if(staff != null){
			map.addArray("staff");
			JsonArray array = map.getArray("staff");
			staff.forEach(staff -> array.add(staff.toString()));
		}
	}

	@Override
	public void load(JsonMap map){
		manager = map.getUUID("manager", null);
		if(staff != null && map.has("staff")){
			staff.clear();
			map.getArray("staff").elements().forEach(elm -> staff.add(UUID.fromString(elm.string_value())));
		}
	}
	
	public boolean isManager(UUID uuid){
		return uuid.equals(manager);
	}
	
	public boolean isStaff(UUID uuid){
		if(staff == null) return false;
		return staff.contains(uuid);
	}

}
