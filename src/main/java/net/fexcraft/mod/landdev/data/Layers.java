package net.fexcraft.mod.landdev.data;

public enum Layers {
	
	PROPERTY,
	CHUNK,
	COMPANY,
	DISTRICT,
	MUNICIPALITY,
	COUNTY,
	STATE,
	UNION,
	
	PLAYER,
	NONE;

	public boolean is(Layers other){
		return this == other;
	}

	public boolean isValidChunkOwner(){
		return this != NONE && this.ordinal() > 1;
	}

}
