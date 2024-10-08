package net.fexcraft.mod.landdev.data.player;

import java.util.ArrayList;
import java.util.UUID;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.Mail;
import net.fexcraft.mod.landdev.data.MailData;
import net.fexcraft.mod.landdev.data.PermAction;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.hooks.ExternalData;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.fexcraft.mod.uni.Appendable;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;
import net.fexcraft.mod.uni.world.EntityW;
import net.fexcraft.mod.uni.world.WorldW;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class LDPlayer implements Saveable, Layer, LDUIModule, Appendable<UniEntity> {

	public UUID uuid;
	public boolean offline, adm;
	public EntityW entity;
	public long joined;
	public long login;
	public long last_login;
	public long last_logout;
	public long last_pos_update;
	public long last_tax;
	public MailData mail;
	public String nickname, colorcode = "2";
	public Account account;
	public ArrayList<Permit> permits = new ArrayList<>();
	public Municipality municipality;
	public County county;
	public Chunk_ chunk_current;
	public Chunk_ chunk_last;
	public ExternalData external = new ExternalData(this);
	
	public LDPlayer(UUID uuid){
		offline = true;
		this.uuid = uuid;
		mail = new MailData(Layers.PLAYER, uuid);
		account = DataManager.getAccount("player:" + uuid.toString(), false, true);
	}

	@Override
	public void save(JsonMap map){
		map.add("uuid", uuid.toString());
		map.add("joined", joined);
		map.add("last_login", login);
		map.add("last_logout", Time.getDate());
		mail.save(map);
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
		if(municipality.id >= 0 && county != municipality.county) county = municipality.county;
		map.add("municipality", municipality.id);
		map.add("county", county.id);
		map.add("last_tax", last_tax);
		external.save(map);
		DataManager.save(account);
	}

	@Override
	public void load(JsonMap map){
		joined = map.getLong("joined", Time.getDate());
		last_login = map.getLong("last_login", 0);
		last_logout = map.getLong("last_logout", 0);
		mail.load(map);
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
		if(municipality.id >= 0 && county != municipality.county) county = municipality.county;
		last_tax = map.getLong("last_tax", 0);
		external.load(map);
	}
	
	@Override
	public void gendef(){
		joined = Time.getDate();
		municipality = ResManager.getMunicipality(-1, true);
		county = ResManager.getCounty(-1, true);
		external.gendef();
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

	public WorldW world(){
		return entity.getWorld();
	}

	public void openGui(int ID, int x, int y, int z){
		//TODO uent.openGui(LandDev.INSTANCE, ID, uent.world, x, y, z);
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

	public void sendLocationUpdate(boolean moved, boolean label, int time){
		TagCW com = TagCW.create();
		com.set("task", "location_update");
		boolean mun = chunk_current.district.municipality() != null;
		TagLW icons = TagLW.create();
		icons.add(chunk_current.district.icon.getnn());
		if(mun) icons.add(chunk_current.district.municipality().icon.getnn());
		icons.add(chunk_current.district.county().icon.getnn());
		icons.add(chunk_current.district.state().icon.getnn());
		com.set("icons", icons);
		TagLW lines = TagLW.create();
		if(moved){
			lines.add(chunk_current.district.state().name());
			lines.add(chunk_current.district.county().name());
			if(mun) lines.add(chunk_current.district.municipality().name());
			lines.add(chunk_current.district.name());
		}
		if(label) lines.add(chunk_current.label.label);
		com.set("lines", lines);
		if(time > 0){ com.set("time", time); }
		LandDev.sendLocationPacket(entity, com);
	}

	public String name(){
		return "&" + colorcode + (nickname == null ? entity == null ? "<PEN>" : entity.getName() : nickname);
	}

	public String name_raw(){
		return (nickname == null ? entity == null ? "<PEN>" : entity.getName() : nickname);
	}

	@Override
	public Layers getLayer(){
		return Layers.PLAYER;
	}

	@Override
	public Layers getParentLayer(){
		return null;
	}

	@Override
	public void sync_packet(BaseCon container, ModuleResponse resp){
		//
	}

	@Override
	public void on_interact(BaseCon container, ModuleRequest req){
		//
	}

	public boolean isCurrentlyInDistrict(int id){
		return chunk_current != null && chunk_current.district.id == id;
	}

	public boolean isCurrentlyInMunicipality(int id){
		return chunk_current != null && !chunk_current.district.owner.is_county && chunk_current.district.municipality().id == id;
	}

	public boolean isCurrentlyInCounty(int id){
		return chunk_current != null && chunk_current.district.county().id == id;
	}

	public boolean isCurrentlyInState(int id){
		return chunk_current != null && chunk_current.district.state().id == id;
	}

	public void addMail(Mail newmail){
		mail.add(newmail);
		if(entity != null && mail.unread() > 0){
			entity.send(TranslationUtil.translate("mail.player.new"));
		}
	}

	public void addMailAndSave(Mail newmail){
		addMail(newmail);
		save();
	}

	public boolean isMunicipalityManager(){
		return municipality.manage.isManager(uuid);
	}

	public boolean isCountyManager(){
		return county.manage.isManager(uuid);
	}

	public void setCitizenOf(Municipality mun){
		municipality.manage.removeStaff(uuid);
		municipality.citizens.remove(this);
		if(mun.county.id != county.id){
			county.manage.removeStaff(uuid);
			county.citizens.remove(this);
		}
		municipality = mun;
		municipality.citizens.add(this);
		if(mun.county.id != county.id){
			county = mun.county;
			county.citizens.add(this);
		}
	}

	public void leaveMunicipality(){
		municipality.manage.removeStaff(uuid);
		municipality.citizens.remove(this);
		municipality = ResManager.getMunicipality(-1, true);
	}

	public void leaveCounty(){
		county.manage.removeStaff(uuid);
		county.citizens.remove(this);
		county = ResManager.getCounty(-1, true);
	}

	@Override
	public Appendable<UniEntity> create(UniEntity type){
		return new LDPlayer(type.entity.local());
	}

	@Override
	public String id(){
		return "landdev:player";
	}

}
