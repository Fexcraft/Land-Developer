package net.fexcraft.mod.landdev.data.norm;

import net.fexcraft.app.json.JsonObject;

public abstract class Norm {
	
	public final String id;
	public final NormType type;
	
	public Norm(String id, NormType type){
		this.id = id;
		this.type = type;
	}
	
	public abstract String string();
	
	public abstract int integer();
	
	public abstract float decimal();
	
	public abstract boolean bool();
	
	public abstract JsonObject save();
	
	public abstract void load(JsonObject obj);

	public abstract void set(Object val);

}
