package net.fexcraft.mod.landdev.gui;

import java.util.ArrayList;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class LDGuiBase extends GenericGui<LDGuiContainer> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("landdev:textures/gui/guibase.png");
	private ArrayList<LDGuiElement> elements = new ArrayList<>();

	public LDGuiBase(EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new LDGuiContainer(player), player);
		deftexrect = false;
		sizeOf(6);
	}
	
	@Override
	public void init(){
		elements.clear();
		elements.add(new LDGuiElement("test0", LDGuiElementType.ELM_GREEN).pos(0));
		elements.add(new LDGuiElement("test1", LDGuiElementType.ELM_GENERIC).pos(1));
		elements.add(new LDGuiElement("test2", LDGuiElementType.ELM_BLANK).pos(2));
		elements.add(new LDGuiElement("test3", LDGuiElementType.ELM_RED).pos(3));
		elements.add(new LDGuiElement("test4", LDGuiElementType.ELM_EMPTY).pos(4));
		elements.add(new LDGuiElement("test5", LDGuiElementType.ELM_BLUE).pos(5));
	}

	private void sizeOf(int elms){
		ySize = 25 + (elms * 14);
		xSize = 224;
	}

	private void finish_init(){
		//
	}
	
	@Override
	public void drawbackground(float ticks, int mx, int my){
		drawElement(LDGuiElementType.TOP, 0);
		for(LDGuiElement elm : elements){
			drawElement(elm.type, elm.pos);
		}
		drawElement(LDGuiElementType.BOTTOM, ySize - 6);
	}
	
	private void drawElement(LDGuiElementType elm, int y){
		drawTexturedModalRect(guiLeft, guiTop + y, elm.x, elm.y, elm.w, elm.h);
	}
	
}