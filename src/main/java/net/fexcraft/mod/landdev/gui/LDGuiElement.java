package net.fexcraft.mod.landdev.gui;

public class LDGuiElement {
	
	public String index;
	public LDGuiElementType type;
	protected int pos;
	
	public LDGuiElement(String index, LDGuiElementType type){
		this.index = index;
		this.type = type;
	}

	public LDGuiElement pos(int num){
		pos = num * 14 + 19;
		return this;
	}

}
