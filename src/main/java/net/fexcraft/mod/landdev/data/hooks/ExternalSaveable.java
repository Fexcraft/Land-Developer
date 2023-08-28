package net.fexcraft.mod.landdev.data.hooks;

import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.Saveable;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public interface ExternalSaveable extends Saveable {

	public void setup(Layer layer);

	public String id();

}
