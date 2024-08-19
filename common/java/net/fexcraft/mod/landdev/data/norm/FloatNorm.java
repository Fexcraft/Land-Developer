package net.fexcraft.mod.landdev.data.norm;

import org.apache.commons.lang3.math.NumberUtils;

import net.fexcraft.app.json.JsonValue;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
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
	public JsonValue save(){
		return new JsonValue<Float>(value);
	}

	@Override
	public void load(JsonValue obj){
		value = obj.float_value();
	}

	@Override
	public void set(Object val){
		if(val instanceof Number) value = ((Number)val).floatValue();
		if(val instanceof Boolean) value = ((Boolean)val) ? 1 : 0;
		if(val instanceof String){
			String str = val.toString();
			if(!NumberUtils.isCreatable(val.toString())) return;
			value = Float.parseFloat(str);
		}
	}

	@Override
	public Norm copy(){
		return new FloatNorm(id, value);
	}

}
