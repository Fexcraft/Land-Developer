package net.fexcraft.mod.landdev.data.norm;

import net.fexcraft.app.json.JsonValue;

public class StringNorm extends Norm {
	
	private String value;

	public StringNorm(String id, String def){
		super(id, NormType.STRING);
		value = def;
	}

	@Override
	public String string(){
		return value;
	}

	@Override
	public int integer(){
		return value.length();
	}

	@Override
	public float decimal(){
		return integer();
	}

	@Override
	public boolean bool(){
		return value.equals("true");
	}

	@Override
	public JsonValue save(){
		return new JsonValue<String>(value);
	}

	@Override
	public void load(JsonValue obj){
		value = obj.string_value();
	}

	@Override
	public void set(Object val){
		value = val.toString();
	}

	@Override
	public Norm copy(){
		return new StringNorm(id, value);
	}

}
