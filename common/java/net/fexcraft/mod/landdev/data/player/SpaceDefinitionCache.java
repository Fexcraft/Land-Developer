package net.fexcraft.mod.landdev.data.player;

import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.prop.Property;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.ResManager;

import java.util.HashSet;

import static net.fexcraft.mod.landdev.util.ResManager.SERVER_ACCOUNT;

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
				cks.add(ok);
			}
		}
		if(failed){
			ldp.entity.send("landdev.gui.property.create.no_perm_chunks");
			return;
		}
		for(Chunk_ ok : cks){
			for(Property prop : ok.propholder.properties){
				if(prop.intersects(pos, pos.add(size))){
					ldp.entity.send("landdev.gui.property.create.intersection", prop.id, ok.key.comma());
					return;
				}
			}
		}
		//
		int nid = ResManager.getNewIdFor(ResManager.getProperty(-1).saveTable());
		if(nid < 0){
			ldp.entity.send("DB ERROR, INVALID NEW ID '" + nid + "'!");
			return;
		}
		Account acc = ck.owner.getAccount(ck);
		long fee = ck.owner.playerchunk ? ck.district.norms.get("new-property-fee").integer() : 0;
		if(acc.getBalance() < fee + LDConfig.PROPERTY_CREATION_FEE){
			ldp.entity.send("landdev.gui.property.create.not_enough_money", Config.getWorthAsString(fee + LDConfig.PROPERTY_CREATION_FEE));
			return;
		}
		if(fee > 0 && !acc.getBank().processAction(Bank.Action.TRANSFER, ldp.entity, acc, fee, ck.district.account())){
			return;
		}
		if(!acc.getBank().processAction(Bank.Action.TRANSFER, ldp.entity, acc, LDConfig.PROPERTY_CREATION_FEE, SERVER_ACCOUNT)){
			return;
		}
		Property prop = new Property(nid);
		prop.start = pos;
		prop.end = pos.add(size);
		prop.created.create(ldp.uuid);
		for(Chunk_ ok : cks){
			ok.propholder.properties.add(prop);
			prop.chunks_in.chunks.add(ok.key);
			ok.save();
		}
		prop.save();
		ldp.entity.send("landdev.gui.property.create.success", prop.id);
		ldp.entity.closeUI();
		ldp.defcache = null;
	}

}
