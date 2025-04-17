package net.fexcraft.mod.landdev.data.county;

import static net.fexcraft.mod.landdev.data.PermAction.*;
import static net.fexcraft.mod.landdev.ui.LDKeys.MAILBOX;
import static net.fexcraft.mod.landdev.ui.LDUIButton.*;
import static net.fexcraft.mod.landdev.ui.LDUIRow.*;
import static net.fexcraft.mod.landdev.util.ResManager.*;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translateCmd;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.hooks.ExternalData;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.norm.BoolNorm;
import net.fexcraft.mod.landdev.data.norm.FloatNorm;
import net.fexcraft.mod.landdev.data.norm.IntegerNorm;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.player.Permit;
import net.fexcraft.mod.landdev.data.region.Region;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.ui.modules.AppearModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;
import net.fexcraft.mod.landdev.ui.modules.NormModule;
import net.fexcraft.mod.landdev.util.Announcer;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.ResManager;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class County implements Saveable, Layer, LDUIModule {

	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail;
	public Manageable manage = new Manageable(true, COUNTY_STAFF);
	public Norms norms = new Norms();
	public ArrayList<Integer> districts = new ArrayList<>();
	public ArrayList<Integer> municipalities = new ArrayList<>();
	public Citizens citizens = new Citizens(COUNTY_CITIZEN);
	public Joinable requests = new Joinable();
	public ExternalData external = new ExternalData(this);
	public long tax_collected;
	public Account account;
	public Region region;
	public int seat = -1;
	
	public County(int id){
		this.id = id;
		mail = new MailData(getLayer(), id);
		account = DataManager.getAccount("county:" + id, false, true);
		norms.add(new StringNorm("name", "Unnamed County"));
		norms.add(new BoolNorm("new-municipalities", false));
		norms.add(new IntegerNorm("new-municipality-fee", 100000));
		norms.add(new BoolNorm("open-to-join", true));
		norms.add(new FloatNorm("municipality-tax-percent", 10));
		norms.add(new IntegerNorm("citizen-tax", 1000));
		norms.add(new BoolNorm("kick-bankrupt", false));
		norms.add(new IntegerNorm("min-municipality-distance", LDConfig.MIN_MUN_DIS));
	}

	@Override
	public void save(JsonMap map){
		map.add("id", id);
		created.save(map);
		sell.save(map);
		icon.save(map);
		color.save(map);
		neighbors.save(map);
		mail.save(map);
		manage.save(map);
		citizens.save(map);
		requests.save(map);
		norms.save(map);
		JsonArray darray = new JsonArray();
		districts.forEach(dis -> darray.add(dis));
		map.add("districts", darray);
		JsonArray marray = new JsonArray();
		municipalities.forEach(mun -> marray.add(mun));
		map.add("municipalities", marray);
		map.add("region", region.id);
		map.add("tax_collected", tax_collected);
		if(seat >= 0) map.add("seat", seat);
		external.save(map);
		DataManager.save(account);
	}

	@Override
	public void load(JsonMap map){
		created.load(map);
		sell.load(map);
		icon.load(map);
		color.load(map);
		neighbors.load(map);
		mail.load(map);
		manage.load(map);
		citizens.load(map);
		requests.load(map);
		norms.load(map);
		account.setName(name());
		if(map.has("districts")){
			JsonArray array = map.getArray("districts");
			districts.clear();
			array.value.forEach(elm -> districts.add(elm.integer_value()));
		}
		if(map.has("municipalities")){
			JsonArray array = map.getArray("municipalities");
			municipalities.clear();
			array.value.forEach(elm -> municipalities.add(elm.integer_value()));
		}
		region = ResManager.getRegion(map.getInteger("region", -1), true);
		tax_collected = map.getLong("tax_collected", 0);
		seat = map.getInteger("seat", seat);
		external.load(map);
	}
	
	@Override
	public void gendef(){
		region = ResManager.getRegion(-1, true);
		if(id == -1){
			norms.get("name").set("Wilderness");
			norms.get("new-municipalities").set(true);
			districts.clear();
			if(!region.counties.contains(id)) region.counties.add(id);
			color.set(0x009900);
		}
		else if(id == 0){
			norms.get("name").set("Spawn County");
			districts.clear();
			districts.add(0);
			region = ResManager.getRegion(0, true);
			if(!region.counties.contains(id)) region.counties.add(id);
			color.set(0xff9900);
			seat = 0;
		}
		external.gendef();
	}
	
	@Override
	public String saveId(){
		return id + "";
	}
	
	@Override
	public String saveTable(){
		return "counties";
	}

	@Override
	public Layers getLayer(){
		return Layers.COUNTY;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.REGION;
	}

	@Override
	public int lid(){
		return id;
	}

	public String name(){
		return norms.get("name").string();
	}

	public static final int UI_CREATE = -1;
	public static final int UI_STAFF_LIST = 1;
	public static final int UI_STAFF_EDIT = 2;
	public static final int UI_STAFF_ADD = 3;
	public static final int UI_CITIZEN_LIST = 4;
	public static final int UI_CITIZEN_EDIT = 5;
	public static final int UI_CITIZEN_INVITE = 6;
	public static final int UI_DISTRICTS = 7;
	public static final int UI_MUNICIPALITY_LIST = 8;
	public static final int UI_MUNICIPALITY_EDIT = 9;
	public static final int UI_MUNICIPALITY_INVITE = 10;
	public static final int UI_PRICE = 11;
	public static final int UI_SET_PRICE = 12;
	public static final int UI_NORMS = 13;
	public static final int UI_NORM_EDIT = 14;
	public static final int UI_APPREARANCE = 15;

	@Override
	public void sync_packet(BaseCon container, ModuleResponse resp){
		resp.setTitle("county.title");
		LDPlayer player = container.ldp;
		boolean canman = manage.can(MANAGE_COUNTY, container.ldp.uuid) || player.adm;
		switch(container.pos.x){
			case UI_MAIN:{
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("name", ELM_GENERIC, canman ? OPEN : EMPTY, canman, name());
				if(seat >= 0) resp.addButton("seat", ELM_GENERIC, OPEN, ResManager.getMunicipalityName(seat));
				resp.addButton("region", ELM_GENERIC, OPEN, region.name());
				resp.addButton("municipalities", ELM_GENERIC, LIST, municipalities.size());
				resp.addButton("districts", ELM_GENERIC, LIST, districts.size());
				resp.addButton("citizen", ELM_GENERIC, LIST, citizens.size());
				resp.addRow("manager", ELM_GENERIC, manage.getManagerName());
				resp.addButton("staff", ELM_GENERIC, LIST, manage.staff.size());
				if(canman){
					resp.addRow("balance", ELM_GENERIC, Config.getWorthAsString(account.getBalance()));
				}
				resp.addBlank();
				if(sell.price > 0){
					resp.addButton("price", ELM_GENERIC, OPEN, sell.price_formatted());
				}
				if(canman){
					resp.addButton("set_price", ELM_GENERIC, OPEN);
				}
				if(sell.price > 0) resp.addBlank();
				if(canman){
					resp.addButton("mailbox", ELM_GENERIC, OPEN, mail.unread());
				}
				resp.addButton("norms", ELM_GREEN, OPEN);
				resp.addButton("appearance", ELM_YELLOW, OPEN);
				return;
			}
			//
			case UI_DISTRICTS:{
				resp.setTitle("county.districts.title");
				for(int id : districts){
					resp.addButton("district." + id, ELM_GENERIC, OPEN, VALONLY + ResManager.getDistrictName(id));
				}
				return;
			}
			case UI_MUNICIPALITY_LIST:{
				resp.setTitle("county.municipality.title");
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("seat", ELM_GENERIC, seat >= 0 ? ResManager.getMunicipalityName(seat) : "");
				resp.addBlank();
				resp.addRow("municipality.list", ELM_YELLOW);
				for(int id : municipalities){
					resp.addButton("municipality.edit." + id, ELM_GENERIC, OPEN, VALONLY + getMunicipalityName(id));
				}
				return;
			}
			case UI_MUNICIPALITY_EDIT:{
				resp.setTitle("county.municipality.edit.title");
				Municipality mun = getMunicipality(municipalities.get(container.pos.z), true);
				resp.addRow("municipality.name", ELM_GENERIC, mun.name());
				resp.addRow("municipality.id", ELM_GENERIC, mun.id);
				resp.addButton("municipality.goto", ELM_GENERIC, OPEN, mun.id);
				resp.addHiddenField("mun-id", mun.id);
				if(canman){
					resp.addBlank();
					resp.addButton("municipality.setmain", ELM_GREEN, OPEN);
					resp.addBlank();
					resp.addButton("municipality.remove", ELM_RED, REM);
				}
				resp.setNoSubmit();
				return;
			}
			case UI_CITIZEN_LIST:{
				resp.setTitle("county.citizen.title");
				if(opentojoin()){
					resp.addRow("citizen.open", ELM_GREEN);
				}
				else{
					resp.addRow("citizen.closed", ELM_RED);
				}
				if(manage.can(PermAction.PLAYER_INVITE, container.ldp.uuid) || player.adm){
					resp.addButton("citizen.invite", ELM_BLUE, ADD);
				}
				if(container.ldp.county.id != id){
					long to = requests.get(container.ldp);
					if(to > -1){
						if(to > 0){
							resp.addRow("citizen.rejected", ELM_RED);
							resp.addRow("citizen.timeout", ELM_GREEN, Time.getAsString(to));
						}
						else{
							resp.addRow("citizen.pending", ELM_GREEN);
						}
					}
					else{
						if(opentojoin()){
							resp.addButton("citizen.join", ELM_BLUE, ADD);
						}
						else{
							resp.addButton("citizen.request", ELM_BLUE, ADD);
						}
					}
				}
				else{
					resp.addButton("citizen.leave", ELM_RED, REM);
				}
				resp.addBlank();
				resp.addRow("citizen.list", ELM_YELLOW);
				for(UUID uuid : citizens.map().keySet()){
					resp.addButton("citizen.edit." + uuid, ELM_GENERIC, OPEN, VALONLY + "- " + ResManager.getPlayerName(uuid));
				}
				return;
			}
			case UI_CITIZEN_EDIT:{
				resp.setTitle("county.citizen.edit.title");
				Citizens.Citizen cit = citizens.get(container.pos.z);
				resp.addRow("citizen.name", ELM_GENERIC, cit.getPlayerName());
				resp.addRow("citizen.uuid", ELM_GENERIC, cit.uuid);
				if(player.adm){
					resp.addButton("citizen.remove", ELM_RED, REM);
				}
				resp.addHiddenField("uuid", cit.uuid);
				resp.addBlank();
				resp.addRow("citizen.permissions", ELM_YELLOW);
				for(Map.Entry<PermAction, Boolean> entry : cit.actions.entrySet()){
					resp.addButton("citizen.permission." + entry.getKey().name().toLowerCase(), ELM_GENERIC, enabled(entry.getValue()));
				}
				resp.setNoSubmit();
				return;
			}
			case UI_CITIZEN_INVITE:{
				resp.setTitle("county.citizen.invite.title");
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("citizen.invite.info", ELM_YELLOW);
				resp.addField("citizen.invite.field");
				resp.addButton("citizen.invite.submit", ELM_GENERIC, OPEN);
				resp.setFormular();
				return;
			}
			case UI_STAFF_EDIT:{
				resp.setTitle("county.staff.edit.title");
				Manageable.Staff staff = manage.staff.get(container.pos.z);
				resp.addRow("staff.name", ELM_GENERIC, staff.getPlayerName());
				resp.addRow("staff.uuid", ELM_GENERIC, staff.uuid);
				if(player.adm || !manage.isManager(staff)){
					resp.addButton("staff.remove", ELM_RED, REM);
					resp.addButton("staff.setmanager", ELM_BLUE, ADD);
				}
				resp.addHiddenField("uuid", staff.uuid);
				resp.addBlank();
				resp.addRow("staff.permissions", ELM_YELLOW);
				for(Map.Entry<PermAction, Boolean> entry : staff.actions.entrySet()){
					resp.addButton("staff.permission." + entry.getKey().name().toLowerCase(), ELM_GENERIC, enabled(entry.getValue()));
				}
				resp.setNoSubmit();
				return;
			}
			case UI_STAFF_LIST:{
				resp.setTitle("county.staff.title");
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("manager", ELM_GENERIC, manage.getManagerName());
				resp.addButton("staff.add", ELM_BLUE, ADD);
				resp.addBlank();
				resp.addRow("staff.list", ELM_YELLOW);
				for(Manageable.Staff staff : manage.staff){
					resp.addButton("staff.edit." + staff.uuid, ELM_GENERIC, OPEN, VALONLY + "- " + staff.getPlayerName());
				}
				return;
			}
			case UI_STAFF_ADD:{
				resp.setTitle("county.staff.add.title");
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("staff.add.info", ELM_YELLOW);
				resp.addField("staff.add.field");
				resp.addButton("staff.add.submit", ELM_GENERIC, OPEN);
				resp.setFormular();
				return;
			}
			case UI_APPREARANCE:
				AppearModule.resp(container, resp, "county", icon, color, canman);
				return;
			case UI_NORMS:
				NormModule.respNormList(norms, container, resp, "county", canman);
				return;
			case UI_NORM_EDIT:{
				NormModule.respNormEdit(norms, container, resp, "county", canman);
				return;
			}
			case UI_CREATE:
				resp.setTitle("county.create.title");
				Chunk_ chunk = ResManager.getChunk(container.ldp.entity);
				Region region = chunk.district.region();
				boolean rn = region.norms.get("new-counties").bool();
				boolean pp = container.ldp.hasPermit(CREATE_COUNTY, region.getLayer(), region.id);
				if(!rn && !pp){
					resp.addRow("create.no_perm", ELM_GENERIC, BLANK);
					return;
				}
				resp.addRow("create.name", ELM_GENERIC);
				resp.addField("create.name_field");
				resp.addCheck("create.region_funded", ELM_GENERIC, pp);
				if(chunk.district.id >= 0){
					resp.addCheck("create.integrate_district", ELM_GENERIC, pp);
				}
				if(chunk.district.municipality() != null){
					resp.addCheck("create.municipality_funded", ELM_GENERIC, pp);
					resp.addCheck("create.integrate_municipality", ELM_GENERIC, pp);
				}
				resp.addButton("create.submit", ELM_BLUE, OPEN);
				resp.setFormular();
				resp.setNoBack();
				return;
		}
		external.sync_packet(container, resp);
	}

	@Override
	public void on_interact(BaseCon container, ModuleRequest req){
		LDPlayer player = container.ldp;
		boolean canman = manage.can(MANAGE_MUNICIPALITY, container.ldp.uuid) || player.adm;
		switch(req.event()){
			case "name":{
				if(!canman) return;
				container.open(UI_NORM_EDIT, id, norms.index(norms.get("name")));
				return;
			}
			case "seat": if(seat >= 0) container.open(LDKeys.MUNICIPALITY, 0, seat, 0);return;
			case "region": container.open(LDKeys.REGION, 0, region.id, 0); return;
			case "citizen": container.open(UI_CITIZEN_LIST); return;
			case "municipalities": container.open(UI_MUNICIPALITY_LIST); return;
			case "districts": container.open(UI_DISTRICTS); return;
			case "staff": container.open(UI_STAFF_LIST); return;
			case "price": container.open(UI_PRICE); return;
			case "set_price": if(canman) container.open(UI_SET_PRICE); return;
			case "mailbox": if(canman) container.open(MAILBOX, getLayer().ordinal(), id, 0); return;
			case "norms": container.open(UI_NORMS); return;
			case "appearance": container.open(UI_APPREARANCE); return;
			//
			case "municipality.goto":{
				int mun = req.getFieldInt("mun-id");
				if(!municipalities.contains(mun)) return;
				container.open(LDKeys.MUNICIPALITY, 0, mun, 0);
				return;
			}
			case "municipality.setmain":{
				if(!canman) return;
				int mun = req.getFieldInt("mun-id");
				if(!municipalities.contains(mun)) return;
				Municipality muni = ResManager.getMunicipality(mun, true);
				if(muni.county.id != id) return;
				seat = muni.id;
				container.open(UI_MUNICIPALITY_LIST);
				return;
			}
			case "municipality.remove":{
				if(!canman) return;
				int mun = req.getFieldInt("mun-id");
				if(!municipalities.contains(mun)) return;
				if(municipalities.size() < 2){
					container.msg("municipality.only_one");
					return;
				}
				if(seat == mun){
					container.msg("municipality.setnew");
					return;
				}
				Municipality muni = ResManager.getMunicipality(mun, true);
				muni.county = ResManager.getCounty(-1, true);
				for(UUID uuid : muni.citizens.map().keySet()){
					manage.removeStaff(uuid);
					citizens.remove(uuid);
				}
				muni.save();
				save();
				Announcer.announce(Announcer.Target.MUNICIPALITY, id, "announce.county.municipality.removed", muni.name(), name(), id);
				container.open(UI_MUNICIPALITY_LIST);
				return;
			}
			case "citizen.invite":{
				container.open(UI_CITIZEN_INVITE);
				return;
			}
			case "citizen.join":{
				if(!opentojoin()) return;
				if(player.isMunicipalityManager() && player.municipality.county.id != id){
					container.msg("landdev.mail.municipality.citizen.ismanager", false);
					return;
				}
				if(player.isCountyManager() && id != player.county.id){
					container.msg("landdev.mail.county.citizen.ismanager", false);
					return;
				}
				player.setCitizenOf(this);
				container.open(UI_MAIN);
				return;
			}
			case "citizen.leave":{
				if(player.isCountyManager() && !player.adm){
					container.msg("landdev.mail.county.citizen.ismanager", false);
					return;
				}
				if(player.municipality.county.id == id){
					container.msg("landdev.mail.county.citizen.isinsame", false);
					return;
				}
				if(player.isCountyManager()){
					manage.setManager((UUID)null);
				}
				player.leaveCounty();
				container.open(UI_MAIN);
				return;
			}
			case "citizen.request":{
				if(opentojoin()) return;
				Mail mail = new Mail(MailType.REQUEST, Layers.PLAYER, player.uuid, Layers.MUNICIPALITY, id);
				mail.setTitle(player.name_raw()).expireInDays(7);
				mail.addMessage(translate("mail.player.county.join_request0", player.name_raw()));
				mail.addMessage(translate("mail.player.county.join_request1"));
				this.mail.mails.add(mail);
				requests.timeouts.put(player.uuid, 0l);
				container.open(UI_CITIZEN_LIST);
				return;
			}
			case "citizen.invite.submit":{
				if(!manage.can(PermAction.PLAYER_INVITE, player.uuid) && !player.adm) return;
				LDPlayer ply = req.getPlayerField("citizen.invite.field");
				if(ply == null){
					container.msg("citizen.invite.notfound");
					return;
				}
				Mail mail = new Mail(MailType.INVITE, Layers.MUNICIPALITY, id, Layers.PLAYER, ply.uuid);
				mail.setTitle(name()).expireInDays(7);
				mail.addMessage(translate("mail.county.citizen.invite0"));
				mail.addMessage(translate("mail.county.citizen.invite1", name()));
				ply.addMailAndSave(mail);
				player.entity.send(translate("gui.county.citizen.invite.success"));
				player.entity.closeUI();
				return;
			}
			case "citizen.remove":{
				if(!canman) return;
				Citizens.Citizen cit = citizens.get(req.getUUIDField());
				if(cit != null && !manage.isManager(cit.uuid)){
					LDPlayer ply = ResManager.getPlayer(cit.uuid, true);
					if(ply.municipality.county.id == id){
						container.msg("citizen.remove.isinsame");
						return;
					}
					ply.setCitizenOf(ResManager.getCounty(-1, true));
					Mail mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.PLAYER, ply.uuid).expireInDays(7);
					mail.setTitle(name()).addMessage(translate("mail.county.citizen.nolonger"));
					ply.addMailAndSave(mail);
					mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.MUNICIPALITY, id).expireInDays(7);
					mail.setTitle(name()).addMessage(translate("mail.county.citizen.removed", cit.getPlayerName()));
					this.mail.mails.add(mail);
					Announcer.announce(Announcer.Target.MUNICIPALITY, id, "announce.county.citizen.removed", cit.getPlayerName(), name(), id);
				}
				container.open(UI_CITIZEN_LIST);
				return;
			}
			case "staff.add":{
				container.open(UI_STAFF_ADD);
				return;
			}
			case "staff.add.submit":{
				if(!canman) return;
				LDPlayer ply = req.getPlayerField("staff.add.field");
				if(ply == null){
					container.msg("staff.add.notfound");
					return;
				}
				if(!citizens.isCitizen(ply.uuid)){
					container.msg("staff.add.notmember");
					return;
				}
				Mail mail = new Mail(MailType.INVITE, Layers.MUNICIPALITY, id, Layers.PLAYER, ply.uuid).expireInDays(7);
				mail.setTitle(name()).setStaffInvite();
				mail.addMessage(translate("mail.county.staff.invite0"));
				mail.addMessage(translate("mail.county.staff.invite1"));
				ply.addMailAndSave(mail);
				player.entity.send(translate("gui.county.staff.add.success"));
				player.entity.closeUI();
				return;
			}
			case "staff.remove":{
				if(!canman) return;
				Manageable.Staff staff = manage.getStaff(req.getUUIDField());
				if(staff != null && !manage.isManager(staff)){
					manage.removeStaff(staff.uuid);
					LDPlayer ply = ResManager.getPlayer(staff.uuid, true);
					Mail mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.PLAYER, ply.uuid).expireInDays(7);
					mail.setTitle(name()).addMessage(translate("mail.county.staff.nolonger"));
					ply.addMailAndSave(mail);
					for(Manageable.Staff stf : manage.staff){
						LDPlayer stp = ResManager.getPlayer(stf.uuid, true);
						mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.PLAYER, stp.uuid).expireInDays(7);
						mail.setTitle(name()).addMessage(translate("mail.county.staff.removed", staff.getPlayerName()));
						stp.addMailAndSave(mail);
					}
					Announcer.announce(Announcer.Target.MUNICIPALITY, id, "announce.county.staff.removed", staff.getPlayerName(), name(), id);
				}
				container.open(UI_STAFF_LIST);
				return;
			}
			case "staff.setmanager":{
				if(!player.adm && !canman) return;
				Manageable.Staff staff = manage.getStaff(req.getUUIDField());
				if(staff != null){
					manage.setManager(staff.uuid);
					LDPlayer ply = ResManager.getPlayer(staff.uuid, true);
					Mail mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.PLAYER, ply.uuid).expireInDays(7);
					mail.setTitle(name()).addMessage(translate("mail.county.manager_now"));
					ply.addMailAndSave(mail);
					save();
					for(Manageable.Staff stf : manage.staff){
						LDPlayer stp = ResManager.getPlayer(stf.uuid, true);
						mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.PLAYER, stp.uuid).expireInDays(7);
						mail.setTitle(name()).addMessage(translate("mail.county.manager_set", staff.getPlayerName()));
						stp.addMailAndSave(mail);
					}
					Announcer.announce(Announcer.Target.MUNICIPALITY, id, "announce.county.manager_set", staff.getPlayerName(), name(), id);
				}
				container.open(UI_MAIN);
				return;
			}
			//
			case "norm_submit":{
				if(!canman) return;
				NormModule.processNorm(norms, container, req, UI_NORM_EDIT);
				return;
			}
			case "norm_bool":{
				if(!canman) return;
				NormModule.processBool(norms, container, req, UI_NORM_EDIT);
				return;
			}
			case "create.submit":{
				Chunk_ chunk = ResManager.getChunk(container.ldp.entity);
				Region region = chunk.district.region();
				long sum = LDConfig.COUNTY_CREATION_FEE;
				boolean rn = region.norms.get("new-counties").bool();
				boolean pp = player.hasPermit(CREATE_COUNTY, region.getLayer(), region.id);
				if(!rn && !pp){
					player.entity.send(translateCmd("ct.no_new_counties"));
					player.entity.send(translateCmd("ct.no_create_permit"));
					player.entity.closeUI();
					return;
				}
				boolean cd = req.getCheck("create.integrate_district");
				boolean cm = req.getCheck("create.integrate_municipality");
				boolean fm = req.getCheck("create.municipality_funded");
				Municipality mun = cm ? chunk.district.municipality() : null;
				if(cm && mun == null) return;
				if(!cm && player.isInManagement(Layers.MUNICIPALITY)){
					container.msg("create.leave_municipality_management");
					return;
				}
				if(player.isInManagement(Layers.COUNTY)){
					container.msg("create.leave_county_management");
					return;
				}
				if(player.isInManagement(Layers.REGION) && player.county.region.id != region.id){
					container.msg("create.leave_region_management");
					return;
				}
				String name = req.getField("create.name_field");
				if(!validateName(container, name)) return;
				boolean ura = req.getCheck("create.region_funded");
				if(!pp && !ura) sum += region.norms.get("new-county-fee").integer();
				Permit perm = pp ? player.getPermit(CREATE_COUNTY_FUND, region.getLayer(), region.id) : null;
				if(!pp && ura){
					container.msg("create.no_fund_permit");
					return;
				}
				Account acc = pp && ura ? perm.getAccount() : cm && fm ? mun.account : player.account;
				if(acc.getBalance() < sum){
					container.msg("create.not_enough_money");
					return;
				}
				if(cd && !chunk.district.norms.get("county-can-form").bool()){
					container.msg("create.district_no_forming");
					return;
				}
				if(cd && chunk.district.municipality() != null && chunk.district.municipality().districts.size() == 1){
					container.msg("create.only_district");
					return;
				}
				if(!cd && !cm && chunk.district.id >= 0){
					container.msg("create.chunk_is_claimed");
					return;
				}
				if(cm && !mun.manage.isManager(player.uuid)){
					container.msg("create.municipality_not_manager");
					return;
				}
				//todo notifications
				int newid = ResManager.getNewIdFor(saveTable()), ndid = -2;
				if(newid < 0){
					player.entity.send("DB ERROR, INVALID NEW ID '" + newid + "'!");
					return;
				}
				if(!cd){
					ndid = ResManager.getNewIdFor(chunk.district.saveTable());
					if(ndid < 0){
						player.entity.send("DB ERROR, INVALID NEW DISTRICT ID '" + newid + "'!");
						return;
					}
				}
				if(!acc.getBank().processAction(Bank.Action.TRANSFER, player.entity, acc, sum, SERVER_ACCOUNT)){
					return;
				}
				if(!ura) SERVER_ACCOUNT.getBank().processAction(Bank.Action.TRANSFER, null, SERVER_ACCOUNT, region.norms.get("new-county-fee").integer(), region.account);
				County nct = new County(newid);
				nct.created.create(player.uuid);
				ResManager.CT_CENTERS.put(nct.id, chunk.key);
				nct.gendef();
				ResManager.COUNTIES.put(nct.id, nct);
				nct.norms.get("name").set(name);
				nct.region = region;
				region.counties.add(nct.id);
				nct.manage.add(player);
				nct.manage.setManager(player);
				nct.citizens.add(player);
				if(!cm){
					if(cd){
						nct.districts.add(chunk.district.id);
						chunk.district.owner.set(nct);
						chunk.district.manage.clear();
						chunk.district.save();
					}
					else{
						District dis = new District(ndid);
						dis.created.create(player.uuid);
						ResManager.DISTRICTS.put(dis.id, dis);
						nct.districts.add(dis.id);
						chunk.district = dis;
						chunk.owner.set(Layers.COUNTY, null, nct.id);
						chunk.sell.price = 0;
						chunk.save();
						dis.owner.set(nct);
						dis.save();
					}
				}
				else{
					for(UUID uuid : mun.county.citizens.get().keySet()){
						mun.county.citizens.remove(uuid);
						nct.citizens.add(uuid);
					}
					mun.county = nct;
					nct.municipalities.add(mun.id);
					nct.seat = mun.id;
					mun.save();
				}
				SERVER_ACCOUNT.getBank().processAction(Bank.Action.TRANSFER, null, SERVER_ACCOUNT, LDConfig.COUNTY_CREATION_FEE / 2, nct.account);
				ResManager.bulkSave(nct, region, player);
				player.entity.closeUI();
				player.entity.send(translate("gui.county.create.complete"));
				Announcer.announce(Announcer.Target.GLOBAL, 0, "announce.county.created", name, newid);
				return;
			}
			case "appearance.submit":{
				if(!canman) return;
				AppearModule.req(container, req, icon, color);
				container.open(UI_MAIN);
				return;
			}
		}
		if(NormModule.isNormReq(norms, container, req, UI_NORM_EDIT, id)) return;
		if(req.event().startsWith("district.")){
			int id = Integer.parseInt(req.event().substring("district.".length()));
			container.open(LDKeys.DISTRICT, 0, id, 0);
			return;
		}
		if(req.event().startsWith("staff.edit.")){
			Manageable.Staff staff = manage.getStaff(UUID.fromString(req.event().substring("staff.edit.".length())));
			if(staff == null) return;
			container.open(UI_STAFF_EDIT, id, manage.staff.indexOf(staff));
			return;
		}
		if(req.event().startsWith("staff.permission.")){
			if(!canman) return;
			Manageable.Staff staff = manage.getStaff(req.getUUIDField());
			if(manage.isManager(staff)){
				container.msg("staff.permissions.ismanager");
				return;
			}
			PermAction action = PermAction.get(req.event().substring("staff.permission.".length()).toUpperCase());
			if(action == null) return;
			staff.actions.put(action, !staff.actions.get(action));
			container.open(UI_STAFF_EDIT);
			return;
		}
		if(req.event().startsWith("citizen.edit.")){
			UUID uuid = UUID.fromString(req.event().substring("citizen.edit.".length()));
			if(!citizens.isCitizen(uuid)) return;
			container.open(UI_CITIZEN_EDIT, id, citizens.indexOf(uuid));
			return;
		}
		if(req.event().startsWith("citizen.permission.")){
			if(!canman) return;
			Citizens.Citizen cit = citizens.get(req.getUUIDField());
			PermAction action = PermAction.get(req.event().substring("citizen.permission.".length()).toUpperCase());
			if(action == null) return;
			cit.actions.put(action, !cit.actions.get(action));
			container.open(UI_CITIZEN_EDIT);
			return;
		}
		if(req.event().startsWith("municipality.edit.")){
			int mun = Integer.parseInt(req.event().substring("municipality.edit.".length()));
			if(!municipalities.contains(mun)) return;
			container.open(UI_MUNICIPALITY_EDIT, id, municipalities.indexOf(mun));
			return;
		}
		//
		external.on_interact(container, req);
	}

	public boolean opentojoin(){
		return norms.get("open-to-join").bool();
	}

}
