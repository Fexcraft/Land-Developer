package net.fexcraft.mod.landdev.data;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.landdev.util.Settings;

public class Sellable implements Saveable {
	
	private Layer layer;
	public long price;
	
	public Sellable(Layer root){
		layer = root;
	}

	@Override
	public void save(JsonMap map){
		map.add("price", price);
	}

	@Override
	public void load(JsonMap map){
		price = map.getLong("price", layer.is(Layers.CHUNK) ? Settings.DEFAULT_CHUNK_PRICE : 0);
	}

	public String price_formatted(){
		return Config.getWorthAsString(price, true);
	}

}
