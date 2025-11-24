package net.fexcraft.mod.landdev.data.chunk;

import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.util.ResManager;

/**
 * For Chunks and Properties
 * 
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class ChunkOwner implements Saveable {
	
	public boolean unowned, playerchunk;
	public Layers owner = Layers.NONE;
	public UUID player;
	public int owid;

	@Override
	public void save(JsonMap map){
		if(unowned) return;
		map.add("owner_type", owner.toString());
		if(owner.is(Layers.PLAYER)){
			map.add("owner", player.toString());
			//map.add("owner0", player.getMostSignificantBits());
			//map.add("owner1", player.getLeastSignificantBits());
		}
		else{
			map.add("owner", owid);
		}
	}

	@Override
	public void load(JsonMap map){
		if(unowned = !map.has("owner") && !map.has("owner0")) return;
		owner = Layers.valueOf(map.getString("owner_type", "NONE"));
		if(!owner.isValidChunkOwner()){
			owner = Layers.NONE;
			unowned = true;
			return;
		}
		if(playerchunk = owner.is(Layers.PLAYER)){
			if(map.has("owner")) player = UUID.fromString(map.get("owner").string_value());
			else player = new UUID(map.getLong("owner0", 0), map.getLong("owner1", 0));
		}
		else owid = map.getInteger("owner", -1);
	}
	
	public void set(Layers layer, UUID uuid, int id){
		if(unowned = !layer.isValidChunkOwner()){
			owner = Layers.NONE;
			playerchunk = false;
			player = null;
			owid = -1;
		}
		else{
			playerchunk = (owner = layer).is(Layers.PLAYER);
			player = playerchunk ? uuid : null;
			owid = playerchunk ? -1 : id;
		}
	}

	public String name(){
		return unowned ? "landdev.gui.chunk.unowned" : playerchunk ? ResManager.getPlayerName(player) : owner.name() + ":" + owid;
	}

	public Account getAccount(Chunk_ chunk){
		if(unowned) return ResManager.SERVER_ACCOUNT;
		if(playerchunk) return ResManager.getPlayer(player, true).account;
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
		if(playerchunk) return other.playerchunk && player.equals(other.player);
		return owner == other.owner && owid == other.owid;
	}

	public Layers layer(){
		return owner;
	}

	public boolean taxable(){
		return playerchunk || owner.is(Layers.COMPANY);
	}

}
