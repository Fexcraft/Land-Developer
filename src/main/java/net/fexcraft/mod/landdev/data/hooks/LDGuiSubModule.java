package net.fexcraft.mod.landdev.data.hooks;

import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.modules.LDGuiModule;
import net.fexcraft.mod.landdev.gui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.gui.modules.ModuleResponse;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public interface LDGuiSubModule {

	public boolean sync_packet(LDGuiModule root, LDGuiContainer container, ModuleResponse resp);

	public boolean on_interact(LDGuiModule root, LDGuiContainer container, ModuleRequest req);

}
