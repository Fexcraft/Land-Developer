package net.fexcraft.mod.landdev.data;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public interface Layer {

	public Layers getLayer();

	public default boolean is(Layers lay){
		return lay == getLayer();
	}

	public Layers getParentLayer();

	public default int lid(){
		return 0;
	}

}
