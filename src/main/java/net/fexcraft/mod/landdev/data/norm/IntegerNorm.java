package net.fexcraft.mod.landdev.data.norm;

import net.fexcraft.app.json.JsonObject;

public class IntegerNorm extends Norm {
	
	private int value;

	public IntegerNorm(String id, int def){
		super(id, NormType.INTEGER);
		value = def;
	}

	@Override
	public String string(){
		return value + "";
	}

	@Override
	public int integer(){
		return value;
	}

	@Override
	public float decimal(){
		return value;
	}

	@Override
	public boolean bool(){
		return value > 0;
	}

	@Override
	public JsonObject save(){
		return new JsonObject<Integer>(value);
	}

	@Override
	public void load(JsonObject obj){
		value = obj.integer_value();
	}

}
