package net.fexcraft.mod.landdev.gui.modules;

import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_BLANK;

import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.LDGuiElementType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public interface LDGuiModule {

	public void sync_packet(LDGuiContainer container, NBTTagCompound com);
	
	public default void addToList(NBTTagList root, String index, LDGuiElementType elm, LDGuiElementType icon, boolean button, boolean field, String value){
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagString(index));
		list.appendTag(new NBTTagString(elm.name()));
		list.appendTag(new NBTTagString(icon.name()));
		list.appendTag(new NBTTagString((elm == ELM_BLANK ? "0" : "1") + (button ? "1" : "0") + (field ? "1" : "0")));
		if(value != null) list.appendTag(new NBTTagString(value));
		root.appendTag(list);
	}

	public void on_interact(NBTTagCompound packet, String index, EntityPlayer player, Chunk_ chunk);
	
}
