package net.fexcraft.mod.landdev.data;

public interface Layer {

	public Layers getLayer();

	public default boolean is(Layers lay){
		return lay == getLayer();
	}

	public Layers getParentLayer();

	public default int id(){
		return 0;
	}

}
