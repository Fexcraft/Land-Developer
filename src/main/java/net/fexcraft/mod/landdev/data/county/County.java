package net.fexcraft.mod.landdev.data.county;

import static net.fexcraft.mod.landdev.data.PermAction.*;
import static net.fexcraft.mod.landdev.data.PermAction.CREATE_MUNICIPALITY;
import static net.fexcraft.mod.landdev.gui.GuiHandler.MAILBOX;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.*;
import static net.fexcraft.mod.landdev.util.ResManager.SERVER_ACCOUNT;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translateCmd;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
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
import net.fexcraft.mod.landdev.data.player.Permit;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.modules.*;
import net.fexcraft.mod.landdev.util.Announcer;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;
import net.minecraft.nbt.NBTTagList;

public class County implements Saveable, Layer, LDGuiModule {

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
	public ExternalData external = new ExternalData(this);
	public long tax_collected;
	public Municipality main;
	public Account account;
	public State state;
	
	public County(int id){
		this.id = id;
		mail = new MailData(getLayer(), id);
		account = DataManager.getAccount("county:" + id, false, true);
		norms.add(new StringNorm("name", translate("county.norm.name")));
		norms.add(new BoolNorm("new-municipalities", false));
		norms.add(new IntegerNorm("new-municipality-fee", 100000));
		norms.add(new BoolNorm("open-to-join", true));
		norms.add(new FloatNorm("municipality-tax-percent", 10));
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
		norms.save(map);
		JsonArray darray = new JsonArray();
		districts.forEach(dis -> darray.add(dis));
		map.add("districts", darray);
		JsonArray marray = new JsonArray();
		municipalities.forEach(mun -> marray.add(mun));
		map.add("municipalities", marray);
		map.add("state", state.id);
		map.add("tax_collected", tax_collected);
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
		state = ResManager.getState(map.getInteger("state", -1), true);
		tax_collected = map.getLong("tax_collected", 0);
		external.load(map);
	}
	
	@Override
	public void gendef(){
		state = ResManager.getState(-1, true);
		if(id == -1){
			norms.get("name").set(translate("county.wilderness.name"));
			norms.get("new-municipalities").set(true);
			districts.clear();
			if(!state.counties.contains(id)) state.counties.add(id);
			color.set(0x009900);
		}
		else if(id == 0){
			norms.get("name").set(translate("county.spawnzone.name"));
			districts.clear();
			districts.add(0);
			state = ResManager.getState(0, true);
			if(!state.counties.contains(id)) state.counties.add(id);
			color.set(0xff9900);
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
		return Layers.STATE;
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
	public static final int UI_MUNICIPALITIES = 8;
	public static final int UI_PRICE = 9;
	public static final int UI_SET_PRICE = 10;
	public static final int UI_NORMS = 11;
	public static final int UI_NORM_EDIT = 12;
	public static final int UI_APPREARANCE = 13;

	@Override
	public void sync_packet(LDGuiContainer container, ModuleResponse resp){
		resp.setTitle("county.title");
		NBTTagList list = new NBTTagList();
		boolean canman = manage.can(MANAGE_COUNTY, container.player.uuid) || container.player.adm;
		switch(container.x){
			case UI_MAIN:{
				resp.addRow("id", ELM_GENERIC, id);
				resp.addRow("name", ELM_GENERIC, canman ? ICON_OPEN : ICON_EMPTY, canman, name());
				if(main != null) resp.addButton("seat", ELM_GENERIC, ICON_OPEN, main.name());
				resp.addButton("state", ELM_GENERIC, ICON_OPEN, state.name());
				resp.addButton("municipalities", ELM_GENERIC, ICON_LIST, municipalities.size());
				resp.addButton("districts", ELM_GENERIC, ICON_LIST, districts.size());
				resp.addButton("citizen", ELM_GENERIC, ICON_LIST, citizens.size());
				resp.addRow("manager", ELM_GENERIC, manage.getManagerName());
				resp.addButton("staff", ELM_GENERIC, ICON_LIST, manage.staff.size());
				if(canman){
					resp.addRow("balance", ELM_GENERIC, Config.getWorthAsString(account.getBalance()));
				}
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
			//
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
			case "seat": if(main != null) container.open(GuiHandler.MUNICIPALITY, 0, main.id, 0);return;
			case "state": container.open(GuiHandler.STATE, 0, state.id, 0); return;
			case "citizen": container.open(UI_CITIZEN_LIST); return;
			case "municipalities": container.open(UI_MUNICIPALITIES); return;
			case "districts": container.open(UI_DISTRICTS); return;
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
			case "create.submit":{
				//
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
		//
		//
		external.on_interact(container, req);
	}

}
