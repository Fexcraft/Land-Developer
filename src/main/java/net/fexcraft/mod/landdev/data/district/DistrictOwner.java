package net.fexcraft.mod.landdev.data.district;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.util.ResManager;

/**
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class DistrictOwner implements Saveable {
	
	public Municipality municipality;
	public boolean county;
	public int owid;

	@Override
	public void save(JsonMap map){
		map.add("county", county);
		map.add("owner", owid);
	}

	@Override
	public void load(JsonMap map){
		county = map.getBoolean("county", county);
		owid = map.getInteger("owner", -1);
		if(county) return;
		else municipality = ResManager.getMunicipality(owid, true);
	}

}
