package net.fexcraft.mod.landdev.data.district;

import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.norm.BoolNorm;
import net.fexcraft.mod.landdev.data.norm.IntegerNorm;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.landdev.util.ResManager;

public class District implements Saveable, Layer {
	
	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail = new MailData();
	public DistrictType type;
	public Manageable manage = new Manageable(false);
	public Norms norms = new Norms();
	public DistrictOwner owner = new DistrictOwner();
	public long chunks;
	
	public District(int id){
		this.id = id;
		norms.add(new StringNorm("name", translate("district.norm.name")));
		norms.add(new BoolNorm("explosions", false));
		norms.add(new IntegerNorm("chunk-tax", 1000));
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
		type.save(map);
		manage.save(map);
		norms.save(map);
		owner.save(map);
		map.add("chunks", chunks);
	}

	@Override
	public void load(JsonMap map){
		created.load(map);
		sell.load(map);
		icon.load(map);
		color.load(map);
		neighbors.load(map);
		mail.load(map);
		type = DistrictType.get(map);
		manage.load(map);
		norms.load(map);
		owner.load(map);
		chunks = map.getLong("chunks", 0);
	}
	
	@Override
	public void gendef(){
		type = DistrictType.getDefault();
		if(id == -1){
			norms.get("name").set(translate("district.wilderness.name"));
			owner.owid = -1;
			owner.is_county = true;
			owner.county = ResManager.getCounty(-1, true);
			color.set(0x009900);
		}
		else if(id == 0){
			norms.get("name").set(translate("district.spawnzone.name"));
			owner.owid = 0;
			owner.is_county = false;
			owner.municipality = ResManager.getMunicipality(0, true);
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
		return "districts";
	}

	@Override
	public Layers getLayer(){
		return Layers.DISTRICT;
	}

	@Override
	public Layers getParentLayer(){
		return owner.is_county ? Layers.COUNTY : Layers.MUNICIPALITY;
	}

	public String name(){
		return norms.get("name").string();
	}

	public long tax(){
		return norms.get("chunk-tax").integer();
	}

	public State state(){
		return owner.is_county ? owner.county.state : owner.municipality.county.state;
	}

}
