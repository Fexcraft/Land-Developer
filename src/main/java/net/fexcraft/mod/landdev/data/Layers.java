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

	public boolean isValidChunkOwner2(){
		return this != PLAYER && this.ordinal() > 2;
	}

	public boolean isPlayerBased(){
		return this == PLAYER || this == COMPANY;
	}

	public static Layers get(String string){
		switch(string){
			case "property": return PROPERTY;
			case "chunk": return CHUNK;
			case "company": return COMPANY;
			case "district": return DISTRICT;
			case "municipality": return MUNICIPALITY;
			case "county": return COUNTY;
			case "state": return STATE;
			case "union": return UNION;
			case "player": return PLAYER;
			case "none": return NONE;
		}
		return NONE;
	}

}
