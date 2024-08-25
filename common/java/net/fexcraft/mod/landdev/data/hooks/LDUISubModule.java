package net.fexcraft.mod.landdev.data.hooks;

import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public interface LDUISubModule {

	public boolean sync_packet(LDUIModule root, LDGuiContainer container, ModuleResponse resp);

	public boolean on_interact(LDUIModule root, LDGuiContainer container, ModuleRequest req);

}
