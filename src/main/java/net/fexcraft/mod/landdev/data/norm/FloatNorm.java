package net.fexcraft.mod.landdev.data.norm;

import net.fexcraft.app.json.JsonObject;

public class FloatNorm extends Norm {
	
	private float value;

	public FloatNorm(String id, float def){
		super(id, NormType.DECIMAL);
		value = def;
	}

	@Override
	public String string(){
		return value + "";
	}

	@Override
	public int integer(){
		return (int)value;
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
		return new JsonObject<Float>(value);
	}

	@Override
	public void load(JsonObject obj){
		value = obj.float_value();
	}

}
