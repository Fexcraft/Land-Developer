package net.fexcraft.mod.landdev.data.chunk;

import java.util.UUID;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.api.Account;
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
		}
		else{
			map.add("owner", owid);
		}
	}

	@Override
	public void load(JsonMap map){
		if(unowned = !map.has("owner")) return;
		owner = Layers.valueOf(map.getString("owner_type", "NONE"));
		if(!owner.isValidChunkOwner()){
			owner = Layers.NONE;
			unowned = true;
			return;
		}
		if(playerchunk = owner.is(Layers.PLAYER)) player = UUID.fromString(map.getString("owner", ResManager.CONSOLE_UUID));
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
			owner = layer;
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
			case STATE: return chunk.district.state().account;
			case COMPANY://TODO
			default: return ResManager.SERVER_ACCOUNT;
		
		}
	}

	public boolean issame(ChunkOwner other){
		if(other.unowned) return unowned;
		if(playerchunk) return playerchunk && player.equals(other.player);
		return owner == other.owner && owid == other.owid;
	}

}
