package net.fexcraft.mod.landdev.ui;

public enum LDUIElmType {
	
	TOP,
	BOTTOM,
	ELM_GREEN,
	ELM_RED,
	ELM_BLUE,
	ELM_GENERIC,
	ELM_EMPTY,
	ELM_BLANK,
	ELM_YELLOW,
	SCROLLBAR,
	ICONBAR,
	ICONBARCOLOR,
	SCROLL_UP,
	SCROLL_DOWN,
	GO_BACK,
	;
	
	public int x, y, w, h;

	public String translation(){
		return "landdev.gui." + name().toLowerCase();
	}

	public boolean lighttext(){
		return this != ELM_YELLOW;
	}

}
