package net.fexcraft.mod.landdev.data.norm;

import org.apache.commons.lang3.math.NumberUtils;

import net.fexcraft.app.json.JsonValue;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
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
	public JsonValue save(){
		return new JsonValue<Integer>(value);
	}

	@Override
	public void load(JsonValue obj){
		value = obj.integer_value();
	}

	@Override
	public void set(Object val){
		if(val instanceof Number) value = ((Number)val).intValue();
		if(val instanceof Boolean) value = ((Boolean)val) ? 1 : 0;
		if(val instanceof String){
			String str = val.toString();
			if(!NumberUtils.isCreatable(val.toString())) return;
			value = str.contains(".") ? (int)Float.parseFloat(str) : Integer.parseInt(str);
		}
	}

	@Override
	public Norm copy(){
		return new IntegerNorm(id, value);
	}

}
