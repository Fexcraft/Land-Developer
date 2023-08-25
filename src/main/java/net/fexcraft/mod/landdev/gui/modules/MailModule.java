package net.fexcraft.mod.landdev.gui.modules;

import net.fexcraft.mod.landdev.gui.LDGuiContainer;

public class MailModule implements LDGuiModule {
	
	public static MailModule INST = new MailModule();

	@Override
	public void sync_packet(LDGuiContainer container, ModuleResponse resp){
		//
	}

	public void on_interact(LDGuiContainer container, ModuleRequest req){
		//
	}

}
