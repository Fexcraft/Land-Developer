package net.fexcraft.mod.landdev.ui;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public enum LDUIButton {
	
	ADD("add"),
	REM("rem"),
	OPEN("open"),
	UP("up"),
	DOWN("down"),
	BLANK("blank"),
	LIST("list"),
	EMPTY("empty"),
	ENABLED("enabled"),
	DISABLED("disabled"),
	CHECK_CHECKED("check+"),
	CHECK_UNCHECKED("check-"),
	RADIO_CHECKED("radio+"),
	RADIO_UNCHECKED("radio-"),
	;

	public final String id;

	LDUIButton(String id){
		this.id = id;
	}

	public static LDUIButton checkbox(boolean bool){
		return bool ? CHECK_CHECKED : CHECK_UNCHECKED;
	}

	public static LDUIButton radio(boolean bool){
		return bool ? RADIO_CHECKED : RADIO_UNCHECKED;
	}

	public static LDUIButton enabled(boolean bool){
		return bool ? ENABLED : DISABLED;
	}

	public boolean isCheck(){
		return this == CHECK_CHECKED || this == CHECK_UNCHECKED;
	}

	public boolean isRadio(){
		return this == RADIO_CHECKED || this == RADIO_UNCHECKED;
	}

	public boolean check(){
		return this == CHECK_CHECKED;
	}

	public boolean radio(){
		return this == RADIO_CHECKED;
	}

	public String translation(){
		return "landdev.gui." + name().toLowerCase();
	}
	
}
