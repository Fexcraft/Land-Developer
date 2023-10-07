package net.fexcraft.mod.landdev.data.municipality;

import static net.fexcraft.mod.landdev.data.PermAction.CREATE_MUNICIPALITY;
import static net.fexcraft.mod.landdev.data.PermAction.MANAGE_MUNICIPALITY;
import static net.fexcraft.mod.landdev.data.PermAction.MUNICIPALITY_CITIZEN;
import static net.fexcraft.mod.landdev.data.PermAction.MUNICIPALITY_STAFF;
import static net.fexcraft.mod.landdev.gui.GuiHandler.MAILBOX;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.*;
import static net.fexcraft.mod.landdev.util.ResManager.SERVER_ACCOUNT;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translateCmd;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.Bank.Action;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.Citizens.Citizen;
import net.fexcraft.mod.landdev.data.Manageable.Staff;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.hooks.ExternalData;
import net.fexcraft.mod.landdev.data.norm.BoolNorm;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.data.player.Permit;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.modules.AppearModule;
import net.fexcraft.mod.landdev.gui.modules.LDGuiModule;
import net.fexcraft.mod.landdev.gui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.gui.modules.ModuleResponse;
import net.fexcraft.mod.landdev.gui.modules.NormModule;
import net.fexcraft.mod.landdev.util.Announcer;
import net.fexcraft.mod.landdev.util.Announcer.Target;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;
import net.minecraft.nbt.NBTTagList;

public class Municipality implements Saveable, Layer, LDGuiModule {

	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail;
	public Manageable manage = new Manageable(true, MUNICIPALITY_STAFF);
	public Norms norms = new Norms();
	public ArrayList<Integer> districts = new ArrayList<>();
	public Citizens citizens = new Citizens(MUNICIPALITY_CITIZEN);
	public Joinable requests = new Joinable();
	public ExternalData external = new ExternalData(this);
	public Account account;
	public County county;
	
	public Municipality(int id){
		this.id = id;
		mail = new MailData(getLayer(), id);
		account = DataManager.getAccount("municipality:" + id, false, true);
		norms.add(new StringNorm("name", ""));
		norms.add(new StringNorm("title", ""));
		norms.add(new BoolNorm("open-to-join", true));
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
		JsonArray array = new JsonArray();
		districts.forEach(dis -> array.add(dis));
		map.add("districts", array);
		map.add("county", county.id);
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
		if(map.has("districts")){
			JsonArray array = map.getArray("districts");
			districts.clear();
			array.value.forEach(elm -> districts.add(elm.integer_value()));
		}
		county = ResManager.getCounty(map.getInteger("county", -1), true);
		external.load(map);
	}
	
	@Override
	public void gendef(){
		if(id == -1){
			norms.get("name").set(translate("municipality.wilderness.name"));
			norms.get("title").set(translate("municipality.wilderness.title"));
			districts.clear();
			county = ResManager.getCounty(-1, true);
			color.set(0x009900);
		}
		else if(id == 0){
			norms.get("name").set(translate("municipality.spawnzone.name"));
			norms.get("title").set(translate("municipality.spawnzone.title"));
			districts.clear();
			districts.add(0);
			county = ResManager.getCounty(0, true);
			color.set(0xff9900);
		}
		else{
			norms.get("name").set(translate("municipality.unnamed.name"));
			norms.get("title").set(translate("municipality.unnamed.title"));
		};
		external.gendef();
	}
	
	@Override
	public String saveId(){
		return id + "";
	}
	
	@Override
	public String saveTable(){
		return "municipalities";
	}

	@Override
	public Layers getLayer(){
		return Layers.MUNICIPALITY;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.COUNTY;
	}

	public String name(){
		return norms.get("name").string();
	}

	public String title(){
		return norms.get("title").string();
	}

	public boolean opentojoin(){
		return norms.get("open-to-join").bool();
	}
	
	public static final int UI_CREATE = -1;
	public static final int UI_STAFF_LIST = 1;
	public static final int UI_STAFF_EDIT = 2;
	public static final int UI_STAFF_ADD = 3;
	public static final int UI_CITIZEN_LIST = 4;
	public static final int UI_CITIZEN_EDIT = 5;
	public static final int UI_CITIZEN_INVITE = 6;
	public static final int UI_DISTRICTS = 7;
	public static final int UI_PRICE = 8;
	public static final int UI_SET_PRICE = 9;
	public static final int UI_NORMS = 10;
	public static final int UI_NORM_EDIT = 11;
	public static final int UI_APPREARANCE = 12;


	@Override
	public void sync_packet(LDGuiContainer container, ModuleResponse resp){
		resp.setTitle("municipality.title");
		NBTTagList list = new NBTTagList();
		boolean canman = manage.can(MANAGE_MUNICIPALITY, container.player.uuid) || container.player.adm;
		switch(container.x){
			case UI_MAIN:{
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("name", ELM_GENERIC, canman ? ICON_OPEN : ICON_EMPTY, canman, name());
				resp.addRow("muntitle", ELM_GENERIC, canman ? ICON_OPEN : ICON_EMPTY, canman, title());
				resp.addButton("county", ELM_GENERIC, ICON_OPEN, county.name());
				resp.addButton("districts", ELM_GENERIC, ICON_LIST, districts.size());
				resp.addButton("citizen", ELM_GENERIC, ICON_LIST, citizens.size());
				resp.addRow("manager", ELM_GENERIC, manage.getManagerName());
				resp.addButton("staff", ELM_GENERIC, ICON_LIST, manage.staff.size());
				resp.addBlank();
				if(sell.price > 0){
					resp.addButton("price", ELM_GENERIC, ICON_OPEN, sell.price_formatted());
				}
				if(canman){
					resp.addButton("set_price", ELM_GENERIC, ICON_OPEN);
				}
				if(sell.price > 0) resp.addBlank();
				if(canman){
					resp.addButton("mailbox", ELM_GENERIC, ICON_OPEN, mail.unread());
				}
				resp.addButton("norms", ELM_GREEN, ICON_OPEN);
				resp.addButton("appearance", ELM_YELLOW, ICON_OPEN);
				return;
			}
			case UI_CITIZEN_LIST:{
				resp.setTitle("municipality.citizen.title");
				if(opentojoin()){
					resp.addRow("citizen.open", ELM_GREEN);
				}
				else{
					resp.addRow("citizen.closed", ELM_RED);
				}
				if(manage.can(PermAction.PLAYER_INVITE, container.player.uuid) || container.player.adm){
					resp.addButton("citizen.invite", ELM_BLUE, ICON_ADD);
				}
				if(container.player.municipality.id != id){
					long to = requests.get(container.player);
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
							resp.addButton("citizen.join", ELM_BLUE, ICON_ADD);
						}
						else{
							resp.addButton("citizen.request", ELM_BLUE, ICON_ADD);
						}
					}
				}
				else{
					resp.addButton("citizen.leave", ELM_RED, ICON_REM);
				}
				resp.addBlank();
				resp.addRow("citizen.list", ELM_YELLOW);
				for(UUID uuid : citizens.map().keySet()){
					resp.addButton("citizen.edit." + uuid, ELM_GENERIC, ICON_OPEN, VALONLY + "- " + ResManager.getPlayerName(uuid));
				}
				return;
			}
			case UI_CITIZEN_EDIT:{
				resp.setTitle("municipality.citizen.edit.title");
				Citizen cit = citizens.get(container.z);
				resp.addRow("citizen.name", ELM_GENERIC, cit.getPlayerName());
				resp.addRow("citizen.uuid", ELM_GENERIC, cit.uuid);
				if(container.player.adm){
					resp.addButton("citizen.remove", ELM_RED, ICON_REM);
				}
				resp.addHiddenField("uuid", cit.uuid);
				resp.addBlank();
				resp.addRow("citizen.permissions", ELM_YELLOW);
				for(Entry<PermAction, Boolean> entry : cit.actions.entrySet()){
					resp.addButton("citizen.permission." + entry.getKey().name().toLowerCase(), ELM_GENERIC, enabled(entry.getValue()));
				}
				resp.setNoSubmit();
				return;
			}
			case UI_CITIZEN_INVITE:{
				resp.setTitle("municipality.citizen.invite.title");
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("citizen.invite.info", ELM_YELLOW);
				resp.addField("citizen.invite.field");
				resp.addButton("citizen.invite.submit", ELM_GENERIC, ICON_OPEN);
				resp.setFormular();
				return;
			}
			case UI_STAFF_LIST:{
				resp.setTitle("municipality.staff.title");
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("manager", ELM_GENERIC, manage.getManagerName());
				resp.addButton("staff.add", ELM_BLUE, ICON_ADD);
				resp.addBlank();
				resp.addRow("staff.list", ELM_YELLOW);
				for(Staff staff : manage.staff){
					resp.addButton("staff.edit." + staff.uuid, ELM_GENERIC, ICON_OPEN, VALONLY + "- " + staff.getPlayerName());
				}
				return;
			}
			case UI_STAFF_EDIT:{
				resp.setTitle("municipality.staff.edit.title");
				Staff staff = manage.staff.get(container.z);
				resp.addRow("staff.name", ELM_GENERIC, staff.getPlayerName());
				resp.addRow("staff.uuid", ELM_GENERIC, staff.uuid);
				if(container.player.adm || !manage.isManager(staff)){
					resp.addButton("staff.remove", ELM_RED, ICON_REM);
					resp.addButton("staff.setmanager", ELM_BLUE, ICON_ADD);
				}
				resp.addHiddenField("uuid", staff.uuid);
				resp.addBlank();
				resp.addRow("staff.permissions", ELM_YELLOW);
				for(Entry<PermAction, Boolean> entry : staff.actions.entrySet()){
					resp.addButton("staff.permission." + entry.getKey().name().toLowerCase(), ELM_GENERIC, enabled(entry.getValue()));
				}
				resp.setNoSubmit();
				return;
			}
			case UI_STAFF_ADD:{
				resp.setTitle("municipality.staff.add.title");
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("staff.add.info", ELM_YELLOW);
				resp.addField("staff.add.field");
				resp.addButton("staff.add.submit", ELM_GENERIC, ICON_OPEN);
				resp.setFormular();
				return;
			}
			case UI_DISTRICTS:{
				resp.setTitle("municipality.districts.title");
				for(int id : districts){
					resp.addButton("district." + id, ELM_GENERIC, ICON_OPEN, VALONLY + ResManager.getDistrict(id, true).name());//TODO name cache
				}
				return;
			}
			case UI_PRICE:
				resp.setTitle("municipality.buy.title");
				resp.addRow("id", ELM_GENERIC, ICON_BLANK, id);
				resp.addRow("buy.info0", ELM_YELLOW, ICON_BLANK, null);
				resp.addRow("buy.info1", ELM_YELLOW, ICON_BLANK, null);
				resp.addButton("buy.county_pays", ELM_GENERIC, ICON_CHECKBOX_UNCHECKED);
				resp.addButton("buy.submit", ELM_GENERIC, ICON_OPEN);
				resp.setFormular();
				return;
			case UI_SET_PRICE:
				resp.setTitle("municipality.set_price.title");
				resp.addRow("id", ELM_GENERIC, ICON_BLANK, id);
				resp.addField("set_price.field");
				resp.addButton("set_price.submit", ELM_GENERIC, ICON_OPEN);
				resp.setFormular();
				return;
			case UI_APPREARANCE:
				AppearModule.resp(container, resp, "municipality", icon, color, canman);
				return;
			case UI_NORMS:
				NormModule.respNormList(norms, container, resp, "municipality", canman);
				return;
			case UI_NORM_EDIT:{
				NormModule.respNormEdit(norms, container, resp, "municipality", canman);
				return;
			}
			case UI_CREATE:
				resp.setTitle("municipality.create.title");
				Chunk_ chunk = ResManager.getChunk(container.player.entity);
				County county = chunk.district.county();
				boolean cn = county.norms.get("new-municipalities").bool();
				boolean pp = container.player.hasPermit(CREATE_MUNICIPALITY, county.getLayer(), county.id);
				if(!cn && !pp){
					resp.addRow("create.no_perm", ELM_GENERIC, ICON_BLANK);
					return;
				}
				resp.addRow("create.name", ELM_GENERIC);
				resp.addField("create.name_field");
				resp.addCheck("create.county_funded", ELM_GENERIC, pp);
				resp.addCheck("create.claim_district", ELM_GENERIC, pp);
				resp.addButton("create.submit", ELM_BLUE, ICON_OPEN);
				resp.setFormular();
				resp.setNoBack();
				return;
		}
		external.sync_packet(container, resp);
	}

	@Override
	public void on_interact(LDGuiContainer container, ModuleRequest req){
		Player player = container.player;
		boolean canman = manage.can(MANAGE_MUNICIPALITY, container.player.uuid) || container.player.adm;
		switch(req.event()){
			case "name":{
				if(!canman) return;
				container.open(UI_NORM_EDIT, id, norms.index(norms.get("name")));
				return;
			}
			case "muntitle":{
				if(!canman) return;
				container.open(UI_NORM_EDIT, id, norms.index(norms.get("title")));
				return;
			}
			case "county": container.open(GuiHandler.COUNTY, 0, county.id, 0);return;
			case "citizen": container.open(UI_CITIZEN_LIST); return;
			case "districts": container.open(UI_DISTRICTS); return;
			case "staff": container.open(UI_STAFF_LIST); return;
			case "price": container.open(UI_PRICE); return;
			case "set_price": if(canman) container.open(UI_SET_PRICE); return;
			case "mailbox": if(canman) container.open(MAILBOX, getLayer().ordinal(), id, 0); return;
			case "norms": container.open(UI_NORMS); return;
			case "appearance": container.open(UI_APPREARANCE); return;
			case "buy.submit":{

				return;
			}
			case "set_price.submit":{
				if(!canman) return;
				String[] err = new String[]{ "" };
				String val = req.getField("set_price.field");
				long value = Settings.format_price(err, val);
				if(err[0].length() > 0){
					container.sendMsg(err[0], false);
				}
				else{
					sell.price = value;
					container.open(UI_MAIN);
				}
				return;
			}
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
			case "citizen.invite":{
				container.open(UI_CITIZEN_INVITE);
				return;
			}
			case "citizen.join":{
				if(!opentojoin()) return;
				if(player.isMunicipalityManager()){
					container.sendMsg("landdev.mail.municipality.citizen.ismanager", false);
					return;
				}
				if(player.isCountyManager() && county.id != player.county.id){
					container.sendMsg("landdev.mail.county.citizen.ismanager", false);
					return;
				}
				player.setCitizenOf(this);
				container.open(UI_MAIN);
				return;
			}
			case "citizen.leave":{
				if(player.isMunicipalityManager()){
					container.sendMsg("landdev.mail.municipality.citizen.ismanager", false);
					return;
				}
				player.leaveMunicipality();
				container.open(UI_MAIN);
				return;
			}
			case "citizen.request":{
				if(opentojoin()) return;
				Mail mail = new Mail(MailType.REQUEST, Layers.PLAYER, player.uuid, Layers.MUNICIPALITY, id);
				mail.setTitle(player.name_raw()).expireInDays(7);
				mail.addMessage(translate("mail.player.municipality.join_request0", player.name_raw()));
				mail.addMessage(translate("mail.player.municipality.join_request1"));
				this.mail.mails.add(mail);
				requests.timeouts.put(player.uuid, 0l);
				container.open(UI_CITIZEN_LIST);
				return;
			}
			case "citizen.invite.submit":{
				if(!manage.can(PermAction.PLAYER_INVITE, player.uuid) && !player.adm) return;
				Player ply = req.getPlayerField("citizen.invite.field");
				if(ply == null){
					container.sendMsg("citizen.invite.notfound");
					return;
				}
				Mail mail = new Mail(MailType.INVITE, Layers.MUNICIPALITY, id, Layers.PLAYER, ply.uuid);
				mail.setTitle(name()).expireInDays(7);
				mail.addMessage(translate("mail.municipality.citizen.invite0"));
				mail.addMessage(translate("mail.municipality.citizen.invite1", name()));
				ply.addMailAndSave(mail);
				Print.chat(player.entity, translate("gui.municipality.citizen.invite.success"));
				player.entity.closeScreen();
				return;
			}
			case "citizen.remove":{
				Citizen cit = citizens.get(req.getUUIDField());
				if(cit != null && !manage.isManager(cit.uuid)){
					Player ply = ResManager.getPlayer(cit.uuid, true);
					ply.setCitizenOf(ResManager.getMunicipality(-1, true));
					Mail mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.PLAYER, ply.uuid).expireInDays(7);
					mail.setTitle(name()).addMessage(translate("mail.municipality.citizen.nolonger"));
					ply.addMailAndSave(mail);
					mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.MUNICIPALITY, id).expireInDays(7);
					mail.setTitle(name()).addMessage(translate("mail.municipality.citizen.removed", cit.getPlayerName()));
					this.mail.mails.add(mail);
					Announcer.announce(Target.MUNICIPALITY, id, "announce.municipality.citizen.removed", cit.getPlayerName(), name(), id);
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
				Player ply = req.getPlayerField("staff.add.field");
				if(ply == null){
					container.sendMsg("staff.add.notfound");
					return;
				}
				if(!citizens.isCitizen(ply.uuid)){
					container.sendMsg("staff.add.notmember");
					return;
				}
				Mail mail = new Mail(MailType.INVITE, Layers.MUNICIPALITY, id, Layers.PLAYER, ply.uuid).expireInDays(7);
				mail.setTitle(name()).setStaffInvite();
				mail.addMessage(translate("mail.municipality.staff.invite0"));
				mail.addMessage(translate("mail.municipality.staff.invite1"));
				ply.addMailAndSave(mail);
				Print.chat(player.entity, translate("gui.municipality.staff.add.success"));
				player.entity.closeScreen();
				return;
			}
			case "staff.remove":{
				Staff staff = manage.getStaff(req.getUUIDField());
				if(staff != null && !manage.isManager(staff)){
					manage.removeStaff(staff.uuid);
					Player ply = ResManager.getPlayer(staff.uuid, true);
					Mail mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.PLAYER, ply.uuid).expireInDays(7);
					mail.setTitle(name()).addMessage(translate("mail.municipality.staff.nolonger"));
					ply.addMailAndSave(mail);
					for(Staff stf : manage.staff){
						Player stp = ResManager.getPlayer(stf.uuid, true);
						mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.PLAYER, stp.uuid).expireInDays(7);
						mail.setTitle(name()).addMessage(translate("mail.municipality.staff.removed", staff.getPlayerName()));
						stp.addMailAndSave(mail);
					}
					Announcer.announce(Target.MUNICIPALITY, id, "announce.municipality.staff.removed", staff.getPlayerName(), name(), id);
				}
				container.open(UI_STAFF_LIST);
				return;
			}
			case "staff.setmanager":{
				Staff staff = manage.getStaff(req.getUUIDField());
				if(staff != null){
					manage.setManager(staff.uuid);
					Player ply = ResManager.getPlayer(staff.uuid, true);
					Mail mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.PLAYER, ply.uuid).expireInDays(7);
					mail.setTitle(name()).addMessage(translate("mail.municipality.manager_now"));
					ply.addMailAndSave(mail);
					save();
					for(Staff stf : manage.staff){
						Player stp = ResManager.getPlayer(stf.uuid, true);
						mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, id, Layers.PLAYER, stp.uuid).expireInDays(7);
						mail.setTitle(name()).addMessage(translate("mail.municipality.manager_set", staff.getPlayerName()));
						stp.addMailAndSave(mail);
					}
					Announcer.announce(Target.MUNICIPALITY, id, "announce.municipality.manager_set", staff.getPlayerName(), name(), id);
				}
				container.open(UI_MAIN);
				return;
			}
			case "create.submit":{
				Chunk_ chunk = ResManager.getChunk(container.player.entity);
				County county = chunk.district.county();
				long sum = Settings.MUNICIPALITY_CREATION_FEE;
    			boolean cn = county.norms.get("new-municipalities").bool();
    			boolean pp = player.hasPermit(CREATE_MUNICIPALITY, county.getLayer(), county.id);
    			if(!cn && !pp){
	    			Print.chat(player.entity, translateCmd("mun.no_new_municipalities"));
	    			Print.chat(player.entity, translateCmd("mun.no_create_permit"));
	    			player.entity.closeScreen();
    				return;
    			}
    			if(player.isInManagement(Layers.MUNICIPALITY)){
					container.sendMsg("create.leave_management");
					return;
    			}
    			if(player.isInManagement(Layers.COUNTY) && player.county.id != county.id){
					container.sendMsg("create.leave_county_management");
					return;
    			}
    			if(player.isInManagement(Layers.STATE) && player.county.state.id != county.state.id){
					container.sendMsg("create.leave_state_management");
					return;
    			}
    			String name = req.getField("create.name_field");
    			if(!validateName(container, name)) return;
				boolean uca = req.getCheck("create.county_funded");
				if(!pp && !uca) sum += county.norms.get("new-municipality-fee").integer(); 
				Permit perm = pp ? player.getPermit(CREATE_MUNICIPALITY, county.getLayer(), county.id) : null;
				if(!pp && uca){
					container.sendMsg("create.no_fund_permit");
					return;
				}
				Account acc = pp && uca ? perm.getAccount() : player.account;
				if(acc.getBalance() < sum){
					container.sendMsg("create.not_enough_money");
					return;
				}
				boolean claim = req.getCheck("create.claim_district");
				if(claim && !chunk.district.norms.get("municipality-can-form").bool()){
					container.sendMsg("create.district_no_forming");
					return;
				}
				//todo notifications
				int newid = ResManager.getNewIdFor(saveTable()), ndid = -2;
				if(newid < 0){
					Print.chat(player.entity, "DB ERROR, INVALID NEW ID '" + newid + "'!");
					return;
				}
				if(!claim){
					ndid = ResManager.getNewIdFor(chunk.district.saveTable());
					if(ndid < 0){
						Print.chat(player.entity, "DB ERROR, INVALID NEW DISTRICT ID '" + newid + "'!");
						return;
					}
				}
				Bank bank = DataManager.getBank(acc.getBankId(), false, true);
				if(!bank.processAction(Action.TRANSFER, player.entity, acc, sum, SERVER_ACCOUNT)){
					return;
				}
				bank = DataManager.getBank(SERVER_ACCOUNT.getBankId(), false, true);
				if(!uca) bank.processAction(Action.TRANSFER, null, SERVER_ACCOUNT, county.norms.get("new-municipality-fee").integer(), county.account);
				Municipality mold = player.municipality;
				County cold = player.county;
				//mold.citizens.remove(player);
				//cold.citizens.remove(player);
				Municipality mnew = new Municipality(newid);
				mnew.created.create(player.uuid);
				mnew.gendef();
				ResManager.MUNICIPALITIES.put(mnew.id, mnew);
				mnew.norms.get("name").set(name);
				//mnew.citizens.add(player);
				//county.citizens.add(player);
				//player.municipality = mnew;
				//player.county = county;
				mnew.county = county;
				county.municipalities.add(mnew.id);
				player.setCitizenOf(mnew);
				mnew.manage.add(player);
				mnew.manage.setManager(player);
				if(claim){
					mnew.districts.add(chunk.district.id);
					chunk.district.owner.set(mnew);
					chunk.district.manage.clear();
					chunk.district.save();
				}
				else{
					District dis = new District(ndid);
					dis.created.create(player.uuid);
					ResManager.DISTRICTS.put(dis.id, dis);
					mnew.districts.add(dis.id);
					chunk.district = dis;
					chunk.save();
					dis.owner.set(mnew);
					dis.save();
				}
				bank.processAction(Action.TRANSFER, null, SERVER_ACCOUNT, Settings.MUNICIPALITY_CREATION_FEE / 2, mnew.account);
				ResManager.bulkSave(mnew, county, player, mold, cold);
				player.entity.closeScreen();
    			Print.chat(player.entity, translate("gui.municipality.create.complete"));
    			Announcer.announce(Announcer.Target.GLOBAL, 0, "announce.municipality.created", name, newid);
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
			container.open(Layers.DISTRICT.ordinal(), 0, id, 0);
			return;
		}
		if(req.event().startsWith("citizen.edit.")){
			UUID uuid = UUID.fromString(req.event().substring("citizen.edit.".length()));
			if(!citizens.isCitizen(uuid)) return;
			container.open(UI_CITIZEN_EDIT, id, citizens.indexOf(uuid));
			return;
		}
		if(req.event().startsWith("staff.edit.")){
			Staff staff = manage.getStaff(UUID.fromString(req.event().substring("staff.edit.".length())));
			if(staff == null) return;
			container.open(UI_STAFF_EDIT, id, manage.staff.indexOf(staff));
			return;
		}
		if(req.event().startsWith("staff.permission.")){
			if(!canman) return;
			Staff staff = manage.getStaff(req.getUUIDField());
			if(manage.isManager(staff)){
				container.sendMsg("staff.permissions.ismanager");
				return;
			}
			PermAction action = PermAction.get(req.event().substring("staff.permission.".length()).toUpperCase());
			if(action == null) return;
			staff.actions.put(action, !staff.actions.get(action));
			container.open(UI_STAFF_EDIT);
			return;
		}
		if(req.event().startsWith("citizen.permission.")){
			if(!canman) return;
			Citizen cit = citizens.get(req.getUUIDField());
			PermAction action = PermAction.get(req.event().substring("citizen.permission.".length()).toUpperCase());
			if(action == null) return;
			cit.actions.put(action, !cit.actions.get(action));
			container.open(UI_CITIZEN_EDIT);
			return;
		}
		external.on_interact(container, req);
	}

}
