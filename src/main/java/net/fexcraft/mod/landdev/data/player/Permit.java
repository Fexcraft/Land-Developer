package net.fexcraft.mod.landdev.data.player;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.landdev.util.ResManager;

public class Permit implements Saveable {
	
	public PermAction action;
	public long expiry, issued;
	public int uses, used, origin_id;
	public Layers origin_layer;
	public Layers account;

	@Override
	public void save(JsonMap map){
		map.add("action", action.name());
		map.add("expiry", expiry);
		map.add("issued", issued);
		map.add("uses", uses);
		map.add("used", used);
		map.add("from_id", origin_id);
		map.add("from_layer", origin_layer.name());
	}

	@Override
	public void load(JsonMap map){
		action = PermAction.valueOf(map.getString("action", null));
		expiry = map.getLong("expiry", 0);
		issued = map.getLong("issued", 0);
		uses = map.getInteger("uses", 0);
		used = map.getInteger("used", 0);
		origin_id = map.getInteger("origin_id", -1);
		origin_layer = Layers.valueOf(map.getString("from_layer", "NONE"));
	}

	public Account getAccount(){
		switch(origin_layer){
			case DISTRICT:
				District dis = ResManager.getDistrict(origin_id, false);
				if(dis != null) return dis.account();
				break;
			case MUNICIPALITY:
				Municipality mun = ResManager.getMunicipality(origin_id, false);
				if(mun != null) return mun.account;
				break;
			case COUNTY:
				County ct = ResManager.getCounty(origin_id, false);
				if(ct != null) return ct.account;
				break;
			case STATE:
				State st = ResManager.getState(origin_id, false);
				if(st != null) return st.account;
				break;
			case NONE:
			case UNION:
			case COMPANY:
			default:
				return null;
		}
		return null;
	}

	public boolean expired(){
		return used >= uses || Time.getDate() > expiry;
	}

}
