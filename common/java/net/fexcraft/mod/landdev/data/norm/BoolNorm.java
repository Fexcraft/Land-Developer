package net.fexcraft.mod.landdev.data.norm;

import net.fexcraft.app.json.JsonValue;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class BoolNorm extends Norm {
	
	private boolean value;

	public BoolNorm(String id, boolean def){
		super(id, NormType.BOOLEAN);
		value = def;
	}

	@Override
	public String string(){
		return value + "";
	}

	@Override
	public int integer(){
		return value ? 1 : 0;
	}

	@Override
	public float decimal(){
		return value ? 1f : 0f;
	}

	@Override
	public boolean bool(){
		return value;
	}

	@Override
	public JsonValue save(){
		return new JsonValue<Boolean>(value);
	}

	@Override
	public void load(JsonValue obj){
		value = obj.bool();
	}

	@Override
	public void set(Object val){
		if(val instanceof Boolean) value = (boolean)val;
		if(val instanceof String) value = val.toString().toLowerCase().equals("true");
		if(val instanceof Number) value = ((Number)val).intValue() > 0;
	}

	@Override
	public Norm copy(){
		return new BoolNorm(id, value);
	}

	@Override
	public void toggle(){
		value = !value;
	}

}
