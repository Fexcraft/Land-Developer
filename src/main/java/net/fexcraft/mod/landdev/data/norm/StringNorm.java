package net.fexcraft.mod.landdev.data.norm;

import net.fexcraft.app.json.JsonObject;

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
	public JsonObject save(){
		return new JsonObject<String>(value);
	}

	@Override
	public void load(JsonObject obj){
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
