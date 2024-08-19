package net.fexcraft.mod.landdev.data.norm;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public enum NormType {
	
	BOOLEAN,
	INTEGER,
	DECIMAL,
	STRING;

	public boolean isBool(){
		return this == BOOLEAN;
	}

	public boolean isInteger(){
		return this == INTEGER;
	}

	public boolean isDecimal(){
		return this == DECIMAL;
	}

}
