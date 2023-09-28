package net.fexcraft.mod.landdev.data;

import java.util.Map;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.LandDev;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public interface Saveable {
	
	public default void save(){
		LandDev.DB.save(this);
	}

	public void save(JsonMap map);
	
	public void load(JsonMap map);
	
	public default Map<String, Object> saveMap(){
		JsonMap map = new JsonMap();
		this.save(map);
		return JsonHandler.dewrap(map);
	}
	
	public default String saveId(){
		return null;
	}
	
	public default String saveTable(){
		return null;
	}

	public default void gendef(){};

}
