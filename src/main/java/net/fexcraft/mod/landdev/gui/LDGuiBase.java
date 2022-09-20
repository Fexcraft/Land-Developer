package net.fexcraft.mod.landdev.gui;

import static net.fexcraft.mod.landdev.gui.GuiHandler.MAIN;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_GENERIC;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_OPEN;

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
		texts.put("title", new BasicText(guiLeft + 8, guiTop + 8, 196, 0x0e0e0e, type == MAIN ? "landdev.gui.main.title" : "landdev.gui.loading.title").autoscale().translate());
		elements.clear();
		if(type == MAIN){
			int idx = 0;
			addElm("player", ELM_GENERIC, ICON_OPEN, idx++, "player", () -> {});
			addElm("mail", ELM_GENERIC, ICON_OPEN, idx++, "mail", null);
			addElm("prop", ELM_GENERIC, ICON_OPEN, idx++, "property", null);
			addElm("com", ELM_GENERIC, ICON_OPEN, idx++, "company", null);
			addElm("spacer", ELM_GENERIC, ICON_BLANK, idx++, null, null);
			addElm("ck", ELM_GENERIC, ICON_OPEN, idx++, "chunk", null);
			addElm("dis", ELM_GENERIC, ICON_OPEN, idx++, "district", null);
			addElm("mun", ELM_GENERIC, ICON_OPEN, idx++, "municipality", null);
			addElm("ct", ELM_GENERIC, ICON_OPEN, idx++, "county", null);
			addElm("st", ELM_GENERIC, ICON_OPEN, idx++, "state", null);
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

	private void addElm(String id, LDGuiElementType elm, LDGuiElementType icon, int idx, String string, Runnable run){
		elements.add(new LDGuiElement(id, elm).pos(idx, 0).text(this, string));
		elements.add(new LDGuiElement(id + "_icon", icon).pos(idx, 1).button(this, run));
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

	public void add(BasicButton button){
		buttons.put(button.name, button);
	}

	public String prefix(){
		return prefix;
	}
	
}