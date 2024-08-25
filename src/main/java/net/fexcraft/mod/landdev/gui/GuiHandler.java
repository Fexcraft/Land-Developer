package net.fexcraft.mod.landdev.gui;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import static net.fexcraft.mod.landdev.ui.LDKeys.*;

public class GuiHandler implements IGuiHandler {

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
