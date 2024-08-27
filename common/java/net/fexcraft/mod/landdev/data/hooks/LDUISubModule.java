package net.fexcraft.mod.landdev.data.hooks;

import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public interface LDUISubModule {

	public boolean sync_packet(LDUIModule root, BaseCon container, ModuleResponse resp);

	public boolean on_interact(LDUIModule root, BaseCon container, ModuleRequest req);

}
