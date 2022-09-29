package net.fexcraft.mod.landdev.gui.modules;

import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_BLANK;

import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.LDGuiElementType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public interface LDGuiModule {
	
	public static String LANG_YES = "landdev.gui.yes", LANG_NO = "landdev.gui.no";

	public void sync_packet(LDGuiContainer container, NBTTagCompound com);
	
	public default void addToList(NBTTagList root, String index, LDGuiElementType elm, LDGuiElementType icon, boolean button, boolean field, Object value){
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagString(index));
		list.appendTag(new NBTTagString(elm.name()));
		list.appendTag(new NBTTagString(icon.name()));
		list.appendTag(new NBTTagString((elm == ELM_BLANK ? "0" : "1") + (button ? "1" : "0") + (field ? "1" : "0")));
		if(value != null) list.appendTag(new NBTTagString(value.toString()));
		root.appendTag(list);
	}

	public void on_interact(LDGuiContainer container, Player player, NBTTagCompound packet, String index);
	
}
