package net.fexcraft.mod.landdev.ui.modules;

import java.util.UUID;

import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.uni.tag.TagCW;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ModuleRequest {

	private TagCW compound;
	private String event;

	public ModuleRequest(TagCW packet){
		event = packet.getString("interact");
		compound = packet;
	}

	public TagCW getCompound(){
		return compound;
	}

	public String event(){
		return event;
	}

	public String getField(String key){
		return compound.getCompound("fields").getString(key);
	}

	public UUID getUUIDField(){
		return UUID.fromString(compound.getCompound("fields").getString("uuid"));
	}

	public LDPlayer getPlayerField(String key){
		return ResManager.getPlayer(getField(key), true);
	}

	public LDPlayer getPlayerField(String key, boolean load){
		return ResManager.getPlayer(getField(key), load);
	}

	public String getRadio(){
		return compound.getString("radiobox");
	}

	public String getRadio(String rep){
		return compound.getString("radiobox").replace(rep, "");
	}

	public int getRadioInt(String rep){
		return Integer.parseInt(getRadio(rep));
	}

	public float getRadioFloat(String rep){
		return Float.parseFloat(getRadio(rep));
	}

	public boolean getCheck(String key){
		return compound.getCompound("checkboxes").getBoolean(key);
	}

}
