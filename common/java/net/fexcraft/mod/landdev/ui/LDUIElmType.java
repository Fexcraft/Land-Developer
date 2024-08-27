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
	ICON_ADD,
	ICON_REM,
	ICON_OPEN,
	ICON_UP,
	ICON_DOWN,
	ICON_BLANK,
	ICON_LIST,
	ICON_EMPTY,
	ICON_ENABLED,
	ICON_DISABLED,
	ICON_CHECKBOX_CHECKED,
	ICON_CHECKBOX_UNCHECKED,
	ICON_RADIOBOX_CHECKED,
	ICON_RADIOBOX_UNCHECKED,
	SCROLLBAR,
	ICONBAR,
	ICONBARCOLOR,
	SCROLL_UP,
	SCROLL_DOWN,
	GO_BACK,
	;
	
	public int x, y, w, h, px;

	boolean icon(){
		return ordinal() >= 9 && ordinal() < 23;
	}

	public static LDUIElmType checkbox(boolean bool){
		return bool ? ICON_CHECKBOX_CHECKED : ICON_CHECKBOX_UNCHECKED;
	}

	public static LDUIElmType radio(boolean bool){
		return bool ? ICON_RADIOBOX_CHECKED : ICON_RADIOBOX_UNCHECKED;
	}

	public static LDUIElmType enabled(boolean bool){
		return bool ? ICON_ENABLED : ICON_DISABLED;
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

	public String translation(){
		return "landdev.gui." + name().toLowerCase();
	}

	public boolean lighttext(){
		return this != ELM_YELLOW;
	}

}
