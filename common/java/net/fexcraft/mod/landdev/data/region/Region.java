package net.fexcraft.mod.landdev.data.region;

import static net.fexcraft.mod.landdev.data.PermAction.*;
import static net.fexcraft.mod.landdev.ui.LDKeys.MAILBOX;
import static net.fexcraft.mod.landdev.ui.LDUIButton.*;
import static net.fexcraft.mod.landdev.ui.LDUIButton.OPEN;
import static net.fexcraft.mod.landdev.ui.LDUIRow.*;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.hooks.ExternalData;
import net.fexcraft.mod.landdev.data.norm.BoolNorm;
import net.fexcraft.mod.landdev.data.norm.FloatNorm;
import net.fexcraft.mod.landdev.data.norm.IntegerNorm;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.modules.AppearModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;
import net.fexcraft.mod.landdev.ui.modules.NormModule;
import net.fexcraft.mod.landdev.util.ResManager;

public class Region implements Saveable, Layer, LDUIModule {

	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail;
	public Manageable manage = new Manageable(true, REGION_STAFF);
	public Norms norms = new Norms();
	public ArrayList<Integer> counties = new ArrayList<>();
	public ExternalData external = new ExternalData(this);
	public long tax_collected;
	public Account account;
	public int seat;
	
	public Region(int id){
		this.id = id;
		mail = new MailData(getLayer(), id);
		account = DataManager.getAccount("region:" + id, false, true);
		norms.add(new StringNorm("name", "Unnamed Region"));
		norms.add(new BoolNorm("new-counties", false));
		norms.add(new IntegerNorm("new-county-fee", 1000000));
		norms.add(new FloatNorm("county-tax-percent", 10));
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
		norms.save(map);
		account.setName(name());
		JsonArray array = new JsonArray();
		counties.forEach(mun -> array.add(mun));
		map.add("counties", array);
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
		norms.load(map);
		if(map.has("counties")){
			JsonArray array = map.getArray("counties");
			counties.clear();
			array.value.forEach(elm -> counties.add(elm.integer_value()));
		}
		tax_collected = map.getLong("tax_collected", 0);
		seat = map.getInteger("seat", -1);
		external.load(map);
	}
	
	@Override
	public void gendef(){
		if(id == -1){
			norms.get("name").set("Wilderness");
			norms.get("new-counties").set(true);
			counties.clear();
			counties.add(-1);
			color.set(0x009900);
			seat = -1;
		}
		else if(id == 0){
			norms.get("name").set("Spawn Region");
			counties.clear();
			counties.add(0);
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
		return "regions";
	}

	@Override
	public Layers getLayer(){
		return Layers.REGION;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.INTER;
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
	public static final int UI_COUNTY_LIST = 4;
	public static final int UI_COUNTY_EDIT = 5;
	public static final int UI_COUNTY_INVITE = 6;
	public static final int UI_PRICE = 7;
	public static final int UI_SET_PRICE = 8;
	public static final int UI_NORMS = 9;
	public static final int UI_NORM_EDIT = 10;
	public static final int UI_APPREARANCE = 11;

	@Override
	public void sync_packet(BaseCon container, ModuleResponse resp){
		resp.setTitle("region.title");
		LDPlayer player = container.ldp;
		boolean canman = manage.can(MANAGE_REGION, container.ldp.uuid) || player.adm;
		switch(container.pos.x){
			case UI_MAIN:{
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("name", ELM_GENERIC, canman ? OPEN : EMPTY, canman, name());
				if(seat >= 0) resp.addButton("seat", ELM_GENERIC, OPEN, ResManager.getCountyName(seat));
				resp.addButton("counties", ELM_GENERIC, LIST, counties.size());
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
			case UI_APPREARANCE:
				AppearModule.resp(container, resp, "region", icon, color, canman);
				return;
			case UI_NORMS:
				NormModule.respNormList(norms, container, resp, "region", canman);
				return;
			case UI_NORM_EDIT:{
				NormModule.respNormEdit(norms, container, resp, "region", canman);
				return;
			}
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
			case "seat": if(seat >= 0) container.open(LDKeys.COUNTY, 0, seat, 0); return;
			case "counties": container.open(UI_COUNTY_LIST); return;
			case "staff": container.open(UI_STAFF_LIST); return;
			case "price": container.open(UI_PRICE); return;
			case "set_price": if(canman) container.open(UI_SET_PRICE); return;
			case "mailbox": if(canman) container.open(MAILBOX, getLayer().ordinal(), id, 0); return;
			case "norms": container.open(UI_NORMS); return;
			case "appearance": container.open(UI_APPREARANCE); return;
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
		}
		if(NormModule.isNormReq(norms, container, req, UI_NORM_EDIT, id)) return;
		external.on_interact(container, req);
	}

}
