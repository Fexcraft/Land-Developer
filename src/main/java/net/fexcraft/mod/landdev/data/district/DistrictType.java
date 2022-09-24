package net.fexcraft.mod.landdev.data.district;

import java.io.File;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonObject;
import net.fexcraft.mod.landdev.data.Saveable;

public class DistrictType implements Saveable {
	
	public static String DEFAULT;
	public static TreeMap<String, DistrictType> TYPES = new TreeMap<>();
	private boolean agricultural, residential, commercial, industrial, exploitable;
	private String id;

	private DistrictType(String id){
		this.id = id;
	}
	
	public String id(){
		return id;
	}
	
	public boolean agricultural(){
		return agricultural;
	}
	
	public boolean residential(){
		return residential;
	}
	
	public boolean commercial(){
		return commercial;
	}
	
	public boolean industrial(){
		return industrial;
	}
	
	public boolean exploitable(){
		return exploitable;
	}
	
	public static class Builder {
		
		private DistrictType type;
		
		public Builder(String id){
			type = new DistrictType(id);
		}
		
		public Builder agricultural(boolean bool){
			type.agricultural = bool;
			return this;
		}
		
		public Builder residential(boolean bool){
			type.residential = bool;
			return this;
		}
		
		public Builder commercial(boolean bool){
			type.commercial = bool;
			return this;
		}
		
		public Builder industrial(boolean bool){
			type.industrial = bool;
			return this;
		}
		
		public Builder exploitable(boolean bool){
			type.exploitable = bool;
			return this;
		}
		
		public DistrictType build(){
			return type;
		}
		
	}

	@Override
	public void save(JsonMap map){
		map.add("type", id);
	}

	@Override
	public void load(JsonMap map){
		//
	}

	public static DistrictType get(JsonMap map){
		String type = map.getString("type", DEFAULT);
		return TYPES.containsKey(type) ? TYPES.get(type) : TYPES.get(DEFAULT);
	}

	public static void loadConfig(File path){
		File file = new File(path, "/landdev/district_types.json");
		JsonMap map = JsonHandler.parse(file);
		TYPES.clear();
		boolean found = false;
		String def = map.getString("default", null);
		for(Entry<String, JsonObject<?>> entry : map.entries()){
			if(entry.getKey().equals("default")) continue;
			if(def == null) def = entry.getKey();
			found = true;
			JsonMap sub = entry.getValue().asMap();
			DistrictType type = new Builder(entry.getKey())
				.agricultural(sub.getBoolean("agricultural", false))
				.residential(sub.getBoolean("residential", false))
				.commercial(sub.getBoolean("commercial", false))
				.industrial(sub.getBoolean("industrial", false))
				.exploitable(sub.getBoolean("exploitable", false))
				.build();
			TYPES.put(type.id, type);
		}
		if(!found){
			def = "wilderness";
			TYPES.put("wilderness", new Builder("wilderness")
				.agricultural(false)
				.residential(false)
				.commercial(false)
				.industrial(false)
				.exploitable(false)
				.build());
			TYPES.put("village", new Builder("village")
				.agricultural(true)
				.residential(true)
				.commercial(false)
				.industrial(false)
				.exploitable(false)
				.build());
			TYPES.put("agricultural", new Builder("agricultural")
				.agricultural(true)
				.residential(false)
				.commercial(false)
				.industrial(false)
				.exploitable(false)
				.build());
			TYPES.put("mineral", new Builder("mineral")
				.agricultural(false)
				.residential(false)
				.commercial(false)
				.industrial(true)
				.exploitable(true)
				.build());
			TYPES.put("residental", new Builder("residental")
				.agricultural(false)
				.residential(true)
				.commercial(true)
				.industrial(false)
				.exploitable(false)
				.build());
			TYPES.put("commercial", new Builder("commercial")
				.agricultural(true)
				.residential(false)
				.commercial(true)
				.industrial(false)
				.exploitable(false)
				.build());
			TYPES.put("industrial", new Builder("industrial")
				.agricultural(false)
				.residential(false)
				.commercial(false)
				.industrial(true)
				.exploitable(false)
				.build());
			TYPES.put("wasteland", new Builder("wasteland")
				.agricultural(false)
				.residential(false)
				.commercial(false)
				.industrial(false)
				.exploitable(true)
				.build());
			TYPES.put("municipial", new Builder("municipial")
				.agricultural(false)
				.residential(false)
				.commercial(true)
				.industrial(false)
				.exploitable(false)
				.build());
		}
		DEFAULT = def;
	}

	public static DistrictType getDefault(){
		return TYPES.get(DEFAULT);
	}

}
