package net.fexcraft.mod.landdev.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonHandler;
import net.fexcraft.app.json.JsonHandler.PrintOption;
import net.fexcraft.app.json.JsonMap;

public class AliasLoader {
	
	public static final HashMap<String, ArrayList<String>> ALIASES = new HashMap<>();
	public static final HashMap<String, String> OVERRIDES = new HashMap<>();
	private static final ArrayList<String> NONE = new ArrayList<>();

	public static void load(){
		File file = new File(LDConfig.CONFIG_PATH, "landdev-cmds.json");
		if(!file.exists()) generate(file);
		JsonMap map = JsonHandler.parse(file);
		if(map.has("aliases")){
			map.getMap("aliases").entries().forEach(entry -> {
				ArrayList<String> list = new ArrayList<>();
				entry.getValue().asArray().elements().forEach(elm -> list.add(elm.string_value()));
				ALIASES.put(entry.getKey(), list);
			});
		}
		if(map.has("override")){
			map.getMap("override").entries().forEach(entry -> {
				OVERRIDES.put(entry.getKey(), entry.getValue().string_value());
			});
		}
	}

	private static void generate(File file){
		JsonMap map = new JsonMap();
		map.add("__comment", "In this file you can define custom aliases for LandDev's Commands.");
		JsonMap als = new JsonMap();
		als.add("ld-admin", new JsonArray());
		als.add("ld-debug", new JsonArray());
		als.add("ld-self", new JsonArray());
		JsonArray ldgui = new JsonArray();
		ldgui.add("land-developer");
		ldgui.add("/landdev");
		ldgui.add("ld");
		als.add("landdev", ldgui);
		als.add("mail", new JsonArray());
		als.add("ck", new JsonArray());
		als.add("dis", new JsonArray());
		als.add("mun", new JsonArray());
		als.add("ct", new JsonArray());
		als.add("st", new JsonArray());
		map.add("aliases", als);
		map.add("___comment", "Bellow you can even override the default command prefix (with example included).");
		JsonMap ovr = new JsonMap();
		ovr.add("some-cmd-prefix", "new-cmd-prefix");
		ovr.add("ld-example", "ld-override");
		map.add("override", ovr);
		JsonHandler.print(file, map, PrintOption.SPACED);
	}

	public static String getOverride(String string){
		return OVERRIDES.containsKey(string) ? OVERRIDES.get(string) : string;
	}

	public static ArrayList<String> getAlias(String string){
		return ALIASES.containsKey(string) ? ALIASES.get(string) : NONE;
	}

}
