package net.fexcraft.mod.landdev.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	
	public static final int PROPERTY = 0;
	public static final int CHUNK = 1;
	public static final int COMPANY = 2;
	public static final int DISTRICT = 3;
	public static final int MUNICIPALITY = 4;
	public static final int COUNTY = 5;
	public static final int STATE = 6;
	public static final int PLAYER = 8;
	public static final int POLL = 9;
	public static final int MAILBOX = 10;
	

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		switch(ID){
			case 0: return new LDGuiContainer(player);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		switch(ID){
			case 0: return new LDGuiBase(player, x, y, z);
		}
		return null;
	}

}
