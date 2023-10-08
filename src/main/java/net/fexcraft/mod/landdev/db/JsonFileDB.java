package net.fexcraft.mod.landdev.db;

import java.io.File;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonHandler.PrintOption;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.util.Settings;

public class JsonFileDB implements Database {

	@Override
	public void save(Saveable type){
		File file = new File(LandDev.SAVE_DIR, type.saveTable() + "/" + type.saveId() + ".json");
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		JsonMap map = null;
		if(type.equals("chunks") && file.exists()){
			try{
				map = JsonHandler.parse(file);
			}
			catch(Exception e){
				e.printStackTrace();
				map = new JsonMap();
			}
		}
		else map = new JsonMap();
		type.save(map);
		JsonHandler.print(file, map, Settings.SAVE_SPACED_JSON ? PrintOption.FLAT : PrintOption.SPACED);
	}

	@Override
	public JsonMap load(String table, String id){
		if(table.equals("chunks") && !id.startsWith("r_")){
			File file = new File(LandDev.SAVE_DIR, table + "/" + id + ".json");
			if(file.exists()){
				file.deleteOnExit();
				return JsonHandler.parse(file);
			}
			file = new File(LandDev.SAVE_DIR, table + "/r_" + new ChunkKey(id).asRegion().toString() + ".json");
			if(!file.exists()) return new JsonMap();
			JsonMap map = JsonHandler.parse(file);
			if(map.has("chunks")){
				if(map.getMap("chunks").has(id)) return map.getMap("chunks").getMap(id);
			}
			return new JsonMap();
		}
		File file = new File(LandDev.SAVE_DIR, table + "/" + id + ".json");
		return file.exists() ? JsonHandler.parse(file) : new JsonMap();
	}

	@Override
	public boolean exists(String table, String id){
		if(table.equals("chunks")) return true;
		return new File(LandDev.SAVE_DIR, table + "/" + id + ".json").exists();
	}

	@Override
	public boolean internal(){
		return true;
	}

	@Override
	public int getNewEntryId(String table){
		File file = new File(LandDev.SAVE_DIR, table + "/");
		int i = 1;
		while(new File(file, i + ".json").exists()) i++;
		return i;
	}

}
