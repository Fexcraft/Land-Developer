package net.fexcraft.mod.landdev.data.player;

import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.ResManager;

import java.util.HashSet;

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

	public void finish(LDPlayer ldp){
		if(size.x < LDConfig.MIN_PROPERTY_WIDTH || size.z < LDConfig.MIN_PROPERTY_WIDTH){
			ldp.entity.send("landdev.gui.property.create.too_narrow", LDConfig.MIN_PROPERTY_WIDTH);
			return;
		}
		if(size.y < LDConfig.MIN_PROPERTY_HEIGHT){
			ldp.entity.send("landdev.gui.property.create.too_low", LDConfig.MIN_PROPERTY_HEIGHT);
			return;
		}
		if(size.x > LDConfig.MAX_PROPERTY_WIDTH || size.z > LDConfig.MAX_PROPERTY_WIDTH){
			ldp.entity.send("landdev.gui.property.create.too_wide", LDConfig.MAX_PROPERTY_WIDTH);
			return;
		}
		if(size.y > LDConfig.MAX_PROPERTY_HEIGHT){
			ldp.entity.send("landdev.gui.property.create.too_high", LDConfig.MAX_PROPERTY_HEIGHT);
			return;
		}
		Chunk_ ck = ResManager.getChunk(pos);
		if(!ldp.adm && !ck.owner.isPropMan(ldp, ck)){
			if(ck.owner.unowned){
				ldp.entity.send("landdev.gui.property.create.no_man_perm");
			}
			else{
				ldp.entity.send("landdev.gui.property.create.no_own_perm");
			}
			return;
		}
		int sx = ck.key.x;
		int sz = ck.key.z;
		int ex = ((pos.x + size.x) >> 4) + 1;
		int ez = ((pos.z + size.z) >> 4) + 1;
		HashSet<Chunk_> cks = new HashSet<>();
		boolean failed = false;
		for(int x = sx; x < ex; x++){
			for(int z = sz; z < ez; z++){
				Chunk_ ok = ResManager.getChunk(x, z);
				if(!ldp.adm && !ok.owner.isPropMan(ldp, ok)){
					ldp.entity.send("landdev.gui.property.create.no_perm_chunk", ok.key.comma());
					failed = true;
				}
			}
		}
		if(failed){
			ldp.entity.send("landdev.gui.property.create.no_perm_chunks");
			return;
		}
		//
		ldp.entity.closeUI();
	}

}
