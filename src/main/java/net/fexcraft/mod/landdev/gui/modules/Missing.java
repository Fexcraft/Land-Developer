package net.fexcraft.mod.landdev.gui.modules;

import net.fexcraft.mod.landdev.gui.LDGuiContainer;

public class Missing implements LDGuiModule {
	
	public static Missing INST = new Missing();

	@Override
	public void sync_packet(LDGuiContainer container, ModuleResponse resp){
		resp.setTitle("missing.title");
	}

	public void on_interact(LDGuiContainer container, ModuleRequest req){
		//
	}

}
