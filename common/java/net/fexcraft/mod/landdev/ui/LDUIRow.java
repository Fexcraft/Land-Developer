package net.fexcraft.mod.landdev.ui;

public enum LDUIRow {
	
	ELM_GREEN,
	ELM_RED,
	ELM_BLUE,
	ELM_GENERIC,
	ELM_EMPTY,
	ELM_BLANK,
	ELM_YELLOW,
	;

	public String tabid = name().toLowerCase();

	public boolean lighttext(){
		return this != ELM_YELLOW;
	}

}
