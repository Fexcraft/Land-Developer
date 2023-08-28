package net.fexcraft.mod.landdev.gui.modules;

import net.fexcraft.mod.landdev.gui.LDGuiContainer;

public interface LDGuiModule {
	
	public static final String LANG_YES = "landdev.gui.yes", LANG_NO = "landdev.gui.no", VALONLY = "!!!";
	public static final int UI_MAIN = 0;

	public void sync_packet(LDGuiContainer container, ModuleResponse resp);

	public void on_interact(LDGuiContainer container, ModuleRequest req);
	
	public default boolean validateName(LDGuiContainer container, String name){
		if(name.length() < 1){
			container.sendMsg("landdev.cmd.name_too_short", false);
			return false;
		}
		if(name.length() > 32){
			container.sendMsg("landdev.cmd.name_too_long", false);
			return false;
		}
		return true;
	}
	
}
