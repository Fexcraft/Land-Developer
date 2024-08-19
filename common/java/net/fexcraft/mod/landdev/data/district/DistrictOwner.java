package net.fexcraft.mod.landdev.data.district;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.landdev.data.Citizens;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Manageable;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.ResManager;

/**
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class DistrictOwner implements Saveable {
	
	public Municipality municipality;
	public County county;
	public boolean is_county;
	public int owid;

	@Override
	public void save(JsonMap map){
		map.add("county", is_county);
		map.add("owner", owid);
	}

	@Override
	public void load(JsonMap map){
		is_county = map.getBoolean("county", is_county);
		owid = map.getInteger("owner", -1);
		if(is_county) county = ResManager.getCounty(owid, true);
		else municipality = ResManager.getMunicipality(owid, true);
	}

	public int county_id(){
		return is_county ? county.id : municipality.county.id;
	}

	public Manageable manageable(){
		return is_county ? county.manage : municipality.manage;
	}

	public String name(){
		return is_county ? county.name() : municipality.name();
	}

	public void set(Municipality mun){
		is_county = false;
		county = null;
		municipality = mun;
		owid = municipality.id;
	}

	public void set(County ct){
		is_county = true;
		county = ct;
		municipality = null;
		owid = county.id;
	}

	public boolean isPartOf(Player player){
		if(is_county) return player.county.id == owid;
		else return player.municipality.id == owid;
	}

	public Account account(){
		return is_county ? county.account : municipality.account;
	}

	public Citizens citizen(){
		return is_county ? county.citizens : municipality.citizens;
	}

	public Layers layer(){
		return is_county ? Layers.COUNTY : Layers.MUNICIPALITY;
	}

	public void addTaxStat(long tax){
		if(is_county) county.tax_collected += tax;
		else municipality.tax_collected += tax;
	}
}
