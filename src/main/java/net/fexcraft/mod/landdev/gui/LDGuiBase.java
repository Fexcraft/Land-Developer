package net.fexcraft.mod.landdev.gui;

import static net.fexcraft.mod.landdev.gui.GuiHandler.MAIN;

import java.util.ArrayList;

import net.fexcraft.lib.mc.gui.GenericGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class LDGuiBase extends GenericGui<LDGuiContainer> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("landdev:textures/gui/guibase.png");
	private ArrayList<LDGuiElement> elements = new ArrayList<>();
	private String prefix;
	private int type;

	public LDGuiBase(int id, EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new LDGuiContainer(player), player);
		deftexrect = false;
		sizeOf((type = id) == MAIN ? 10 : 0);
		switch(type){
			case -1: prefix = "main"; break;
		}
	}
	
	@Override
	public void init(){
		texts.put("title", new BasicText(guiLeft + 8, guiTop + 8, 196, null, type == MAIN ? "landdev.gui.main.title" : "landdev.gui.loading.title").autoscale().translate());
		elements.clear();
		if(type == MAIN){
			elements.add(new LDGuiElement("player", LDGuiElementType.ELM_GENERIC).pos(0).text(this, "player"));
			elements.add(new LDGuiElement("mail", LDGuiElementType.ELM_GENERIC).pos(1).text(this, "mail"));
			elements.add(new LDGuiElement("prop", LDGuiElementType.ELM_GENERIC).pos(2).text(this, "property"));
			elements.add(new LDGuiElement("com", LDGuiElementType.ELM_GENERIC).pos(2).text(this, "company"));
			elements.add(new LDGuiElement("sp0", LDGuiElementType.ELM_EMPTY).pos(4));
			elements.add(new LDGuiElement("ck", LDGuiElementType.ELM_GENERIC).pos(5).text(this, "chunk"));
			elements.add(new LDGuiElement("dis", LDGuiElementType.ELM_GENERIC).pos(6).text(this, "district"));
			elements.add(new LDGuiElement("mun", LDGuiElementType.ELM_GENERIC).pos(7).text(this, "municipality"));
			elements.add(new LDGuiElement("ct", LDGuiElementType.ELM_GENERIC).pos(8).text(this, "county"));
			elements.add(new LDGuiElement("st", LDGuiElementType.ELM_GENERIC).pos(9).text(this, "state"));
			for(int i = 0; i < 9; i++) elements.add(new LDGuiElement("b" + i, LDGuiElementType.ICON_EMPTY).pos(i));
		}
		else{
			
		}
		/*elements.add(new LDGuiElement("test0", LDGuiElementType.ELM_GREEN).pos(0));
		elements.add(new LDGuiElement("test1", LDGuiElementType.ELM_GENERIC).pos(1));
		elements.add(new LDGuiElement("test2", LDGuiElementType.ELM_BLANK).pos(2));
		elements.add(new LDGuiElement("test3", LDGuiElementType.ELM_RED).pos(3));
		elements.add(new LDGuiElement("test4", LDGuiElementType.ELM_EMPTY).pos(4));
		elements.add(new LDGuiElement("test5", LDGuiElementType.ELM_BLUE).pos(5));*/
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
		drawTexturedModalRect(guiLeft + elm.x, guiTop + y, elm.x, elm.y, elm.w, elm.h);
	}

	public void add(String id, BasicText text){
		texts.put(id, text);
	}

	public String prefix(){
		return prefix;
	}
	
}