package net.fexcraft.mod.landdev.data.state;

import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.norm.StringNorm;

public class State implements Saveable, Layer {
	
	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail = new MailData();
	public Manageable manage = new Manageable(true);
	public Norms norms = new Norms();
	public ArrayList<Integer> counties = new ArrayList<>();
	
	public State(int id){
		this.id = id;
		norms.add(new StringNorm("name", translate("state.norm.name")));
	}

	@Override
	public void save(JsonMap map){
		map.add("id", id);
		created.save(map);
		sell.save(map);
		icon.save(map);
		color.save(map);
		neighbors.save(map);
		mail.save(map);
		manage.save(map);
		norms.save(map);
		JsonArray array = map.addArray("counties").asArray();
		counties.forEach(mun -> array.add(mun));
	}

	@Override
	public void load(JsonMap map){
		created.load(map);
		sell.load(map);
		icon.load(map);
		color.load(map);
		neighbors.load(map);
		mail.load(map);
		manage.load(map);
		norms.load(map);
		if(map.has("counties")){
			JsonArray array = map.getArray("counties");
			counties.clear();
			array.value.forEach(elm -> counties.add(elm.integer_value()));
		}
	}
	
	@Override
	public void gendef(){
		if(id == -1){
			norms.get("name").set(translate("state.wilderness.name"));
			counties.clear();
			counties.add(-1);
			color.set(0x009900);
		}
		else if(id == 0){
			norms.get("name").set(translate("state.spawnzone.name"));
			counties.clear();
			counties.add(0);
			color.set(0xff9900);
		}
		else return;
	}
	
	@Override
	public String saveId(){
		return id + "";
	}
	
	@Override
	public String saveTable(){
		return "states";
	}

	@Override
	public Layers getLayer(){
		return Layers.STATE;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.UNION;
	}

}
