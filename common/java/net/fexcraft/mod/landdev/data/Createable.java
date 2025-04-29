package net.fexcraft.mod.landdev.data;

import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.util.ResManager;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Createable implements Saveable {
	
	private long created, updated;
	private UUID creator = ResManager.CONSOLE_UUID;
	private boolean chunk = false;

	public Createable(){}

	public Createable(boolean bool){
		chunk = bool;
	}

	@Override
	public void load(JsonMap map){
		created = map.getLongTime("created");
		if(map.has("creator")){
			creator = UUID.fromString(map.get("creator").string_value());
		}
		else if(map.has("creator0")){
			creator = new UUID(map.getLong("creator0", 0), map.getLong("creator1", 0));
		}
		updated = map.getLongTime("updated");
	}

	@Override
	public void save(JsonMap map){
		map.add("created", created);
		if(!creator.equals(ResManager.CONSOLE_UUID)){
			if(chunk){
				map.add("creator0", creator.getMostSignificantBits());
				map.add("creator1", creator.getLeastSignificantBits());
			}
			else map.add("creator", creator.toString());
		}
		map.add("updated", updated);
	}
	
	public long getCreated(){
		return created;
	}
	
	public UUID getCreator(){
		return creator;
	}
	
	public long getUpdated(){
		return created;
	}
	
	public void update(Long time){
		updated = time == null ? Time.getDate() : time;
	}

	public void update(){
		update(null);
	}

	/** Only to be used with CHUNKS. Updates the "updated" value too. */
	public void setClaimer(UUID uuid){
		creator = uuid;
		update();
	}

	public UUID getClaimer(){
		return creator;
	}

	/** Only to be used on creation of NEW Layer instances, e.g. via commands. */
	public void create(UUID uuid){
		creator = uuid;
		created = updated = Time.getDate();
	}

}
