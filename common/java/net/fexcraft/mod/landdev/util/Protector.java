package net.fexcraft.mod.landdev.util;

import java.io.File;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonHandler.PrintOption;
import net.fexcraft.app.json.JsonMap;

public class Protector {
	
	public static ProtectorInstance INSTANCE;
	
	public static void load(){
		File file = new File(LDConfig.CONFIG_PATH, "landdev-protector.json");
		if(!file.exists()) generate(file);
		JsonMap map = JsonHandler.parse(file);
		boolean abs = map.getBoolean("absolute_block_protection", false);
		Protector.INSTANCE = ProtectorInstance.get(abs, map);
	}

	private static void generate(File file){
		JsonMap map = new JsonMap();
		map.add("__comment1", "In this file you can define custom interaction protection, by adding an entry the block becomes protected.");
		map.add("__comment2", "If you set 'absolute_block_protection' to true, the list in this file is ignored and all blocks become protected.");
		map.add("__comment3", "Note: There is a hardcoded exception for SIGN blocks.");
		map.add("absolute_block_protection", false);
		JsonArray blocks = new JsonArray();
		blocks.add("minecraft:chest");
		blocks.add("minecraft:ender_chest");
		blocks.add("minecraft:trapped_chest");
		blocks.add("minecraft:furnace");
		blocks.add("minecraft:lit_furnace");
		blocks.add("minecraft:hopper");
		blocks.add("minecraft:dispenser");
		blocks.add("minecraft:dropper");
		blocks.add("minecraft:unpowered_repeater");
		blocks.add("minecraft:powered_repeater");
		map.add("blocks", blocks);
		JsonArray entities = new JsonArray();
		map.add("entities", entities);
		JsonHandler.print(file, map, PrintOption.SPACED);
	}

}
