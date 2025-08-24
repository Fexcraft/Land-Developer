package net.fexcraft.mod.landdev.data.hooks;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.Saveable;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public interface ExternalSaveable /*extends Saveable*/ {

	public String id();

	/** Called right after the object is constructed. */
	public void setup(Layer layer);

	/** For cases where a Layer is new and there is no data to load. */
	public default void gendef(Layer layer){};

	/** Run during Layer saving. */
	public void save(Layer layer, JsonMap map);

	/** Run during Layer loading. */
	public void load(Layer layer, JsonMap map);

}
