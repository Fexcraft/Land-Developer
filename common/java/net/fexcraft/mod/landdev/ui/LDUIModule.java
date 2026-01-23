package net.fexcraft.mod.landdev.ui;

import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public interface LDUIModule {
	
	public static final String LANG_YES = "landdev.gui.yes";
	public static final String LANG_NO = "landdev.gui.no";
	public static final String VALONLY = "!!!";
	public static final String OVERLANG = "###";
	public static final int UI_CREATE = -1;
	public static final int UI_MAIN = 0;

	public void sync_packet(BaseCon container, ModuleResponse resp);

	public void on_interact(BaseCon container, ModuleRequest req);
	
	public default boolean validateName(BaseCon container, String name){
		if(name.length() < 1){
			container.msg("landdev.cmd.name_too_short", false);
			return false;
		}
		if(name.length() > 32){
			container.msg("landdev.cmd.name_too_long", false);
			return false;
		}
		return true;
	}

	class Missing implements LDUIModule {

		public static Missing INST = new Missing();

		@Override
		public void sync_packet(BaseCon container, ModuleResponse resp){
			resp.setTitle("missing.title");
		}

		public void on_interact(BaseCon container, ModuleRequest req){
			//
		}

	}
}
