package net.fexcraft.mod.landdev.data;

import java.awt.Color;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.uni.world.MessageSender;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ColorData implements Saveable {
	
	private int color = 0xffffff;
	
	public ColorData(){}
	
	public ColorData(int value){
		color = value;
	}
	
	public int getInteger(){
		return color;
	}

	public String getString(){
		return "#" + Integer.toHexString(color);
	}

	public ColorData set(int color){
		this.color = color;
		return this;
	}

	public void set(String str){
		color = Integer.parseInt(str.replace("#", ""), 16);
	}

	@Override
	public void load(JsonMap map){
		if(map.has("color")) set(map.get("color").string_value());
		else color = 0xffffff;
		
	}

	@Override
	public void save(JsonMap map){
		if(color < 0xffffff && color > 0) map.add("color", "#" + Integer.toHexString(color));
	}
	
	public static boolean validString(MessageSender sender, String newcolor){
		if(newcolor.replace("#", "").length() != 6){
			if(sender != null) sender.send("Invalid colour string!");
			return false;
		}
		if(!newcolor.contains("#")){
			newcolor = "#" + newcolor;
		}
		try{
			Color.decode(newcolor);
		}
		catch(Exception e){
			if(sender != null) sender.send("Parse Error: " + e.getMessage());
		}
		return true;
	}
	
	@Override
	public String toString(){
		return getString();
	}

}
