package net.fexcraft.mod.landdev.data;

import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.region.Region;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public interface Layer {

	public Layers getLayer();

	public default boolean is(Layers lay){
		return lay == getLayer();
	}

	public Layers getParentLayer();

	public default int lid(){
		return 0;
	}

	public default Manageable manageable(){
		switch(getLayer()){
			case COMPANY:
				break;
			case DISTRICT: return ((District)this).manage;
			case MUNICIPALITY: return ((Municipality)this).manage;
			case COUNTY: return ((County)this).manage;
			case REGION: return ((Region)this).manage;
		}
		return null;
	}

}
