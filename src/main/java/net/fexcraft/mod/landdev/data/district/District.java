package net.fexcraft.mod.landdev.data.district;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.*;

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
	public long chunks;
	
	public District(int id){
		this.id = id;
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
		type.save();
		manage.save();
		norms.save();
		//
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
		//
		chunks = map.getLong("chunks", 0);
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
		return Layers.MUNICIPALITY;
	}

}
