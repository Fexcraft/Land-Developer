package net.fexcraft.mod.landdev.gui;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class LDGuiBase extends GenericGui<LDGuiContainer> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("landdev:textures/gui/guibase.png");

	public LDGuiBase(EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new LDGuiContainer(player), player);
	}
	
}