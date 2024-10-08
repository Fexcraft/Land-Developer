package net.fexcraft.mod.landdev.util;

import net.fexcraft.mod.uni.ui.UIUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return UIUtils.getServer("landdev", ID, player, x, y, z);
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z){
		return UIUtils.getClient("landdev", ID, player, x, y, z);
	}

}
