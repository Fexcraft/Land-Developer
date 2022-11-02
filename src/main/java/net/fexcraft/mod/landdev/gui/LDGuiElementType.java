package net.fexcraft.mod.landdev.gui;

public enum LDGuiElementType {
	
	TOP(0, 0, 225, 19),
	BOTTOM(0, 251, 225, 6),
	ELM_GREEN(0, 19, 225, 14),
	ELM_RED(0, 33, 225, 14),
	ELM_BLUE(0, 47, 225, 14),
	ELM_GENERIC(0, 61, 225, 14),
	ELM_EMPTY(0, 75, 225, 14),
	ELM_BLANK(0, 89, 225, 14),
	ELM_YELLOW(0, 104, 225, 14),
	ICON_ADD(208, 20, 12, 12),
	ICON_REM(208, 34, 12, 12),
	ICON_OPEN(208, 48, 12, 12),
	ICON_UP(208, 62, 12, 12),
	ICON_DOWN(208, 76, 12, 12),
	ICON_BLANK(208, 90, 12, 12),
	ICON_LIST(208, 104, 12, 12),
	ICON_EMPTY(208, 118, 12, 12),
	ICON_ENABLED(208, 132, 12, 12),
	ICON_DISABLED(208, 146, 12, 12),
	ICON_CHECKBOX_CHECKED(208, 160, 12, 12),
	ICON_CHECKBOX_UNCHECKED(208, 174, 12, 12),
	ICON_RADIOBOX_CHECKED(208, 188, 12, 12),
	ICON_RADIOBOX_UNCHECKED(208, 202, 12, 12),
	SCROLLBAR(226, 17, 17, 32),
	SCROLL_UP(229, 21, 11, 11),
	SCROLL_DOWN(229, 34, 11, 11),
	;
	
	public int x, y, w, h, px;
	
	LDGuiElementType(int x, int y, int w, int h){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.px = x;
	}

	boolean icon(){
		return ordinal() >= 9 && ordinal() < 23;
	}

	public static LDGuiElementType checkbox(boolean bool){
		return bool ? ICON_CHECKBOX_CHECKED : ICON_CHECKBOX_UNCHECKED;
	}

	public static LDGuiElementType radio(boolean bool){
		return bool ? ICON_RADIOBOX_CHECKED : ICON_RADIOBOX_UNCHECKED;
	}

	public boolean is_checkbox(){
		return this == ICON_CHECKBOX_CHECKED || this == ICON_CHECKBOX_UNCHECKED;
	}

	public boolean is_radiobox(){
		return this == ICON_RADIOBOX_CHECKED || this == ICON_RADIOBOX_UNCHECKED;
	}

	public boolean checkbox(){
		return this == ICON_CHECKBOX_CHECKED;
	}

	public boolean radiobox(){
		return this == ICON_RADIOBOX_CHECKED;
	}

	String translation(){
		return "landdev.gui." + name().toLowerCase();
	}

}
