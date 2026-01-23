package net.fexcraft.mod.landdev.data.prop;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Mail;
import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.ResManager;

import java.util.UUID;

/**
 * 
 * @author Ferdnand Calo' (FEX___96)
 *
 */
public class PropOwner implements Saveable {
	
	public boolean unowned = true;
	public boolean player;
	public UUID uuid;
	public int cid;

	@Override
	public void save(JsonMap map){
		if(unowned) return;
		map.add("owner_type", player ? "player" : "company");
		if(player) map.add("owner", uuid.toString());
		else map.add("owner", cid);
	}

	@Override
	public void load(JsonMap map){
		if(unowned = !map.has("owner")) return;
		player = map.getString("owner_type", "player").equals("player");
		if(player) uuid = UUID.fromString(map.get("owner").string_value());
		else cid = map.getInteger("owner", -1);
	}
	
	public void set(UUID uid, int id){
		unowned = uid == null && id < 0;
		player = uid != null;
		uuid = uid;
		cid = id;
	}

	public String name(){
		return unowned ? "landdev.gui.chunk.unowned" : player ? ResManager.getPlayerName(uuid) : "C:" + cid;
	}

	public Account getAccount(Chunk_ chunk){
		if(unowned) return chunk.district.account();
		if(player) return ResManager.getPlayer(uuid, true).account;
		else return ResManager.SERVER_ACCOUNT;//TODO
	}

	public boolean issame(PropOwner other){
		if(unowned) return other.unowned;
		if(player) return other.player && uuid.equals(other.uuid);
		return cid == other.cid;
	}

	public Layers layer(){
		return unowned ? Layers.DISTRICT : player ? Layers.PLAYER : Layers.COMPANY;
	}

	public boolean hasPerm(LDPlayer ldp, Property prop){
		if(unowned) return ResManager.getChunk(prop.start).district.can(PermAction.MANAGE_PROPERTY, ldp.uuid);
		if(player) return uuid.equals(ldp.uuid);
		else return false;//TODO
	}

	public boolean hasPerm(LDPlayer ldp, Chunk_ ck){
		if(unowned) return ck.district.can(PermAction.MANAGE_PROPERTY, ldp.uuid);
		if(player) return uuid.equals(ldp.uuid);
		else return false;//TODO
	}

	public void addMail(Mail mail){
		if(unowned) return;
		if(player){
			LDPlayer ldp = ResManager.getPlayer(uuid, true);
			ldp.addMail(mail);
			if(ldp.offline) ldp.save();
		}
		else{
			//TODO
		}
	}

}
