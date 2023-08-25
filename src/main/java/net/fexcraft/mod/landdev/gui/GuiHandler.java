package net.fexcraft.mod.landdev.gui;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	
	public static final int MAIN = -1;
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
	public static final int MAIL = 11;
	public static final int CLAIM = 100;
	public static final int IMG_PREVIEW = 200;
	

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		if(ID <= 8 || ID == 11) return new LDGuiContainer(player, ID, x, y, z);
		if(ID == CLAIM) return new LDGuiClaimCon(player, x, y, z);
		if(ID == IMG_PREVIEW) return new GenericContainer.DefImpl(player);
		if(ID == MAILBOX) return  new LDGuiMailboxCon(player, x, y, z);
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		if(ID <= 8 || ID == 11) return new LDGuiBase(ID, player, x, y, z);
		if(ID == CLAIM) return new LDGuiClaim(player, x, y, z);
		if(ID == IMG_PREVIEW) return new LDGuiImgPreview(player, x, y, z);
		if(ID == MAILBOX) return  new LDGuiMailbox(player, x, y, z);
		return null;
	}

}
