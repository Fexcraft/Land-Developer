package net.fexcraft.mod.landdev.data.chunk;

import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Mail;
import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.ResManager;

/**
 * 
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ChunkOwner implements Saveable {

	public Layers owner = Layers.DISTRICT;
	public UUID player;
	public int comid = -1;

	@Override
	public void save(JsonMap map){
		map.add("owner_type", owner.toString());
		if(owner.is(Layers.PLAYER)){
			map.add("owner", player.toString());
		}
		if(owner.is(Layers.COMPANY)){
			map.add("owner", comid);
		}
	}

	@Override
	public void load(JsonMap map){
		owner = Layers.valueOf(map.getString("owner_type", "DISTRICT"));
		if(owner.is(Layers.NONE)) owner = Layers.DISTRICT;
		if(owner.is(Layers.PLAYER)){
			try{
				player = UUID.fromString(map.get("owner").string_value());
			}
			catch(Exception e){
				e.printStackTrace();
				owner = Layers.DISTRICT;
			}
		}
		if(owner.is(Layers.COMPANY)){
			comid = map.get("owner").integer_value();
			//TODO check if company still exists
		}
	}

	/** Sets the owning layer, id is necessary in cases where the owner is a player or company. */
	public void set(Layers layer, Object id){
		owner = layer;
		player = null;
		comid = -1;
		if(!owner.isValidChunkOwner()){
			owner = Layers.DISTRICT;
		}
		else if(owner.is(Layers.PLAYER)){
			player = (UUID)id;
			comid = -1;
		}
		else if(owner.is(Layers.COMPANY)){
			player = null;
			comid = (int)id;
		}
	}

	public String name(){
		return player != null ? ResManager.getPlayerName(player) : comid >= 0 ? "//todo" : owner.name();
	}

	public Account getAccount(Chunk_ chunk){
		if(player != null) return ResManager.getPlayer(player, true).account;
		switch(owner){
			case DISTRICT: return chunk.district.account();
			case MUNICIPALITY: return chunk.district.municipality().account;
			case COUNTY: return chunk.district.county().account;
			case REGION: return chunk.district.region().account;
			case COMPANY://TODO
			default: return ResManager.SERVER_ACCOUNT;
		
		}
	}

	public boolean issame(ChunkOwner other){
		if(player != null) return player.equals(other.player);
		return owner == other.owner && (!owner.is(Layers.COMPANY) || comid == other.comid);
	}

	public Layers layer(){
		return owner;
	}

	public boolean taxable(){
		return owner.is(Layers.PLAYER, Layers.COMPANY);
	}

	public boolean canManProp(LDPlayer ldp, Chunk_ ck){
		if(player != null) return player.equals(ldp.uuid);
		if(owner.is(Layers.COMPANY)) return false;//TODO
		return ck.district.can(PermAction.MANAGE_PROPERTY, ldp.uuid);
	}

	public boolean isPlayer(){
		return player != null;
	}

	public boolean isCompany(){
		return comid >= 0;
	}

	public boolean isPlayerOrCompany(){
		return player != null || comid >= 0;
	}

	public boolean isNotPLayerOrCompany(){
		return player == null && comid < 0;
	}

}
