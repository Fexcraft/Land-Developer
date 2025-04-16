package net.fexcraft.mod.landdev.data.chunk;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public enum ChunkType {
	
	PRIVATE,
	NORMAL,
	RESTRICTED,
	PUBLIC,
	LOCKED,
	;

	public String l1(){
		switch(this){
			case PRIVATE: return "V";
			case NORMAL: return "N";
			case RESTRICTED: return "R";
			case PUBLIC: return "P";
			case LOCKED: return "L";
		}
		return "N";
	}

	public static ChunkType l1(String string){
		switch(string){
			case "V": return PRIVATE;
			case "N": return NORMAL;
			case "R": return RESTRICTED;
			case "P": return PUBLIC;
			case "L": return LOCKED;
		}
		return NORMAL;
	}

	public String lang(){
		return "landdev.chunk_type." + name().toLowerCase();
	}

	public static ChunkType get(String string){
		switch(string){
			case "private": return PRIVATE;
			case "normal": return NORMAL;
			case "restricted": return RESTRICTED;
			case "public": return PUBLIC;
			case "locked": return LOCKED;
		}
		return null;
	}

	public boolean taxable(){
		return this == PRIVATE || this == NORMAL;
	}

}
