package net.fexcraft.mod.landdev.data.prop;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.chunk.ChunkOwner;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class PropRent implements Saveable {

	public boolean rentable;
	public boolean renewable;
	public boolean autorenew;
	public ChunkOwner renter = new ChunkOwner();
	public long duration = Time.DAY_MS * 7;
	public long amount = 10000;
	public long until;

	@Override
	public void save(JsonMap map){
		map.add("rentable", rentable);
		map.add("duration", duration);
		map.add("renewable", renewable);
		map.add("amount", amount);
		if(!renter.unowned){
			JsonMap r = new JsonMap();
			renter.save(r);
			r.add("autorenew", autorenew);
			r.add("until", until);
			map.add("rented", r);
		}
	}

	@Override
	public void load(JsonMap map){
		rentable = map.getBoolean("rentable", rentable);
		renewable = map.getBoolean("renewable", renewable);
		duration = map.getLong("duration", duration);
		amount = map.getLong("amount", amount);
		if(map.has("rented")){
			JsonMap r = map.getMap("rented");
			renter.load(r);
			until = r.get("until", 0l);
			autorenew = r.getBoolean("autorenew", autorenew);
		}
	}

	public String duration_string(){
		return duration + "";
	}

}
