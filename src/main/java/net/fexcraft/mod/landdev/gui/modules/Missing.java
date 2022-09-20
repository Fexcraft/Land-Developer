package net.fexcraft.mod.landdev.gui.modules;

import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class Missing implements LDGuiModule {
	
	public static Missing INST = new Missing();

	@Override
	public void sync_packet(LDGuiContainer container, NBTTagCompound com){
		com.setString("title_lang", "missing.title");
		com.setTag("elements", new NBTTagList());
	}

	public void on_interact(NBTTagCompound packet, String index, EntityPlayer player, Chunk_ chunk){
		//
	}

}
