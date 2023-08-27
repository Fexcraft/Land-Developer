package net.fexcraft.mod.landdev.gui.modules;

import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ModuleRequest {

	private NBTTagCompound compound;
	private String event;

	public ModuleRequest(NBTTagCompound packet){
		event = packet.getString("interact");
		compound = packet;
	}

	public NBTTagCompound getCompound(){
		return compound;
	}

	public String event(){
		return event;
	}

	public String getField(String key){
		return compound.getCompoundTag("fields").getString(key);
	}

	public Player getPlayerField(String key){
		return ResManager.getPlayer(getField(key), true);
	}

	public Player getPlayerField(String key, boolean load){
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
		return compound.getCompoundTag("checkboxes").getBoolean(key);
	}

}
