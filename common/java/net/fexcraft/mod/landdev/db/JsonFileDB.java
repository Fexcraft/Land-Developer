package net.fexcraft.mod.landdev.db;

import java.io.File;

import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonHandler.PrintOption;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.util.LDConfig;

public class JsonFileDB implements Database {

	@Override
	public void save(Saveable type){
		File file = new File(LandDev.SAVE_DIR, type.saveTable() + "/" + type.saveId() + ".json");
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		JsonMap map = new JsonMap();
		type.save(map);
		JsonHandler.print(file, map, LDConfig.SAVE_SPACED_JSON ? PrintOption.SPACED : PrintOption.FLAT);
	}

	@Override
	public JsonMap load(String table, String id){
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
