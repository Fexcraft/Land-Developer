package net.fexcraft.mod.landdev.data.player;

import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.uni.tag.TagCW;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class SpaceDefinitionCache {

	public V3I pos;
	public V3I size;

	public SpaceDefinitionCache(V3I vec){
		pos = vec.copy();
		size = new V3I(1, 1, 1);
	}

	public void finish(){

	}

}
