package net.fexcraft.mod.landdev.data.player;

import java.util.ArrayList;
import java.util.UUID;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class Player implements Saveable {

	public UUID uuid;
	public boolean offline, adm;
	public EntityPlayer entity;
	public long joined, login, last_login, last_logout;
	public String nickname, colorcode;
	public Account account;
	public ArrayList<Permit> permits = new ArrayList<>();
	public Municipality municipality;
	public County county;
	
	public Player(UUID uuid){
		offline = true;
		this.uuid = uuid;
		account = DataManager.getAccount("player:" + uuid.toString(), false, true);
	}

	@Override
	public void save(JsonMap map){
		map.add("uuid", uuid.toString());
		map.add("joined", joined);
		map.add("last_login", login);
		map.add("last_logout", Time.getDate());
		if(nickname != null) map.add("nick-name", nickname);
		if(colorcode != null) map.add("color-code", colorcode);
		if(permits.size() > 0){
			JsonArray array = new JsonArray();
			permits.forEach(perm -> {
				JsonMap pmap = new JsonMap();
				perm.save(pmap);
				array.add(pmap);
			});
			map.add("permits", array);
		}
		map.add("municipality", municipality.id);
		map.add("county", county.id);
		DataManager.save(account);
	}

	@Override
	public void load(JsonMap map){
		joined = map.getLong("joined", Time.getDate());
		last_login = map.getLong("last_login", 0);
		last_logout = map.getLong("last_logout", 0);
		nickname = map.getString("nick-name", nickname);
		colorcode = map.getString("color-code", colorcode);
		if(map.has("permits")){
			map.get("permits").asArray().value.forEach(elm -> {
				Permit perm = new Permit();
				perm.load(elm.asMap());
				if(!perm.expired()) permits.add(perm);
			});
		}
		municipality = ResManager.getMunicipality(map.getInteger("municipality", -1), true);
		county = ResManager.getCounty(map.getInteger("county", -1), true);
	}
	
	@Override
	public void gendef(){
		joined = Time.getDate();
		municipality = ResManager.getMunicipality(-1, true);
		county = ResManager.getCounty(-1, true);
	}
	
	public String saveId(){
		return uuid.toString();
	}
	
	public String saveTable(){
		return "players";
	}

	public boolean hasPermit(PermAction act, Layers layer, int id){
		for(Permit perm : permits){
			if(perm.action == act && perm.origin_layer == layer && perm.origin_id == id && !perm.expired()) return true;
		}
		return false;
	}

	public Permit getPermit(PermAction act, Layers layer, int id){
		for(Permit perm : permits){
			if(perm.action == act && perm.origin_layer == layer && perm.origin_id == id && !perm.expired()) return perm;
		}
		return null;
	}

	public World world(){
		return entity.world;
	}

	public void openGui(int ID, int x, int y, int z){
		entity.openGui(LandDev.INSTANCE, ID, entity.world, x, y, z);
	}

	public boolean isInManagement(Layers layer){
		if(layer == Layers.MUNICIPALITY){
			return municipality.manage.isStaff(uuid) || municipality.manage.isManager(uuid);
		}
		else if(layer == Layers.COUNTY){
			return county.manage.isStaff(uuid) || county.manage.isManager(uuid);
		}
		else if(layer == Layers.STATE){
			return county.state.manage.isStaff(uuid) || county.state.manage.isManager(uuid);
		}
		return false;
	}

}
