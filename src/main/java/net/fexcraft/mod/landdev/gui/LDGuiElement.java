package net.fexcraft.mod.landdev.gui;

import net.fexcraft.lib.mc.gui.GenericGui.BasicButton;
import net.fexcraft.lib.mc.gui.GenericGui.BasicText;

public class LDGuiElement {
	
	public String index;
	public LDGuiElementType type;
	protected int pos;
	
	public LDGuiElement(String index, LDGuiElementType type){
		this.index = index;
		this.type = type;
	}

	public LDGuiElement pos(int num, int off){
		pos = num * 14 + 19 + off;
		return this;
	}

	public LDGuiElement text(LDGuiBase gui, String text){
		if(text != null) gui.add(index, new BasicText(gui.getGuiLeft() + 8, gui.getGuiTop() + pos + 3, 196, 0xcdcdcd, "landdev.gui." + (gui.prefix()) + "." + text).translate().autoscale());
		return this;
	}

	public LDGuiElement button(LDGuiBase gui, Runnable run){
		if(run != null) gui.add(new BasicButton(index, gui.getGuiLeft() + 208, gui.getGuiTop() + pos, type.x, type.y, type.w, type.h, true));
		return this;
	}

}
