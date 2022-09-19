package net.fexcraft.mod.landdev.gui;

import net.fexcraft.lib.mc.gui.GenericGui.BasicText;

public class LDGuiElement {
	
	public String index;
	public LDGuiElementType type;
	protected int pos;
	
	public LDGuiElement(String index, LDGuiElementType type){
		this.index = index;
		this.type = type;
	}

	public LDGuiElement pos(int num){
		pos = num * 14 + (type.icon() ? 20 : 19);
		return this;
	}

	public LDGuiElement text(LDGuiBase gui, String text){
		gui.add(index, new BasicText(gui.getGuiLeft() + 8, gui.getGuiTop() + pos + 3, 196, 0xcdcdcd, "landdev.gui." + (gui.prefix()) + "." + text).translate().autoscale());
		return this;
	}

}
