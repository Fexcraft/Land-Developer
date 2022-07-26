package net.fexcraft.mod.landdev.gui;

public enum LDGuiElementType {
	
	TOP(0, 0, 225, 19),
	ELM_GREEN(0, 19, 225, 14),
	ELM_RED(0, 33, 225, 14),
	ELM_BLUE(0, 47, 225, 14),
	ELM_GENERIC(0, 61, 225, 14),
	ELM_EMPTY(0, 75, 225, 14),
	ELM_BLANK(0, 89, 225, 14),
	ICON_ADD(208, 20, 12, 12),
	ICON_REM(208, 34, 12, 12),
	ICON_OPEN(208, 48, 12, 12),
	ICON_UP(208, 62, 12, 12),
	ICON_DOWN(208, 76, 12, 12),
	ICON_BLANK(208, 90, 12, 12),
	ICON_LIST(208, 104, 12, 12),
	ICON_EMPTY(208, 118, 12, 12),
	SCROLLBAR(226, 17, 17, 32),
	SCROLL_UP(229, 21, 11, 11),
	SCROLL_DOWN(229, 34, 11, 11),
	;
	
	LDGuiElementType(int x, int y, int w, int h){
		
	}

}
