package net.fexcraft.mod.landdev.data.municipality;

import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.norm.StringNorm;

public class Municipality implements Saveable, Layer {
	
	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail = new MailData();
	public Manageable manage = new Manageable(true);
	public Norms norms = new Norms();
	public ArrayList<Integer> districts = new ArrayList<>();
	
	public Municipality(int id){
		this.id = id;
		norms.add(new StringNorm("name", translate("municipality.norm.name")));
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
		manage.save();
		norms.save();
		JsonArray array = map.addArray("districts").asArray();
		districts.forEach(dis -> array.add(dis));
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
		if(map.has("districts")){
			JsonArray array = map.getArray("districts");
			districts.clear();
			array.value.forEach(elm -> districts.add(elm.integer_value()));
		}
	}
	
	@Override
	public void gendef(){
		if(id == -1){
			norms.get("name").set(translate("municipality.wilderness.name"));
			districts.clear();
		}
		else if(id == 0){
			norms.get("name").set(translate("municipality.spawnzone.name"));
			districts.clear();
			districts.add(0);
		}
		else return;
	}
	
	@Override
	public String saveId(){
		return id + "";
	}
	
	@Override
	public String saveTable(){
		return "municipalities";
	}

	@Override
	public Layers getLayer(){
		return Layers.MUNICIPALITY;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.STATE;
	}

}
