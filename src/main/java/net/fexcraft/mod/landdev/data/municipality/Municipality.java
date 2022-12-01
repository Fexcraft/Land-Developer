package net.fexcraft.mod.landdev.data.municipality;

import static net.fexcraft.mod.landdev.data.PermAction.ACT_CLAIM;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_CREATE_LAYER;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_MANAGE_FINANCES;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_SET_TAX_CHUNK;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_SET_TAX_PLAYER;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_USE_FINANCES;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_BLUE;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_GENERIC;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_OPEN;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.checkbox;
import static net.fexcraft.mod.landdev.util.ResManager.SERVER_ACCOUNT;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translateCmd;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.api.Bank.Action;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.PermAction.PermActions;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.norm.BoolNorm;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.data.player.Permit;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.modules.LDGuiModule;
import net.fexcraft.mod.landdev.util.Announcer;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class Municipality implements Saveable, Layer, LDGuiModule {

	public static PermActions mactions = new PermActions(ACT_CLAIM, ACT_SET_TAX_CHUNK, ACT_SET_TAX_PLAYER, ACT_USE_FINANCES, ACT_MANAGE_FINANCES);
	public static PermActions cactions = new PermActions();
	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail = new MailData();
	public Manageable manage = new Manageable(true, mactions);
	public Norms norms = new Norms();
	public ArrayList<Integer> districts = new ArrayList<>();
	public Citizens citizens = new Citizens(cactions);
	public Account account;
	public County county;
	
	public Municipality(int id){
		this.id = id;
		account = DataManager.getAccount("municipality:" + id, false, true);
		norms.add(new StringNorm("name", translate("municipality.norm.name")));
		manage.norms.add(new BoolNorm("claim", false));
		manage.norms.add(new BoolNorm("manage_district", false));
		manage.norms.add(new BoolNorm("set_tax_chunk", false));
		manage.norms.add(new BoolNorm("set_tax_player", false));
		manage.norms.add(new BoolNorm("finances_use", false));
		manage.norms.add(new BoolNorm("finances_manage", false));
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
		JsonArray array = new JsonArray();
		districts.forEach(dis -> array.add(dis));
		map.add("districts", array);
		map.add("county", county.id);
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
		if(map.has("districts")){
			JsonArray array = map.getArray("districts");
			districts.clear();
			array.value.forEach(elm -> districts.add(elm.integer_value()));
		}
		county = ResManager.getCounty(map.getInteger("county", -1), true);
	}
	
	@Override
	public void gendef(){
		if(id == -1){
			norms.get("name").set(translate("municipality.wilderness.name"));
			districts.clear();
			county = ResManager.getCounty(-1, true);
			color.set(0x009900);
		}
		else if(id == 0){
			norms.get("name").set(translate("municipality.spawnzone.name"));
			districts.clear();
			districts.add(0);
			county = ResManager.getCounty(0, true);
			color.set(0xff9900);
		}
		else return;
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
	
	public static final int UI_CREATE = -1;

	@Override
	public void sync_packet(LDGuiContainer container, NBTTagCompound com){
		com.setString("title_lang", "municipality.title");
		NBTTagList list = new NBTTagList();
		switch(container.x){
			case UI_CREATE:{
				com.setString("title_lang", "municipality.create.title");
				Chunk_ chunk = ResManager.getChunk(container.player().entity);
    			County county = chunk.district.county();
    			boolean cn = county.norms.get("new-municipalities").bool();
    			boolean pp = container.player.hasPermit(ACT_CREATE_LAYER, county.getLayer(), county.id);
    			if(!cn && !pp){
    				addToList(list, "create.no_perm", ELM_GENERIC, ICON_BLANK, false, false, null);
    				break;
    			}
				addToList(list, "create.name", ELM_GENERIC, ICON_BLANK, false, false, null);
				addToList(list, "create.name_field", ELM_BLANK, ICON_BLANK, false, true, null);
				addToList(list, "create.county_funded", ELM_GENERIC, checkbox(pp), true, false, null);
				addToList(list, "create.claim_district", ELM_GENERIC, checkbox(pp), true, false, null);
				addToList(list, "create.submit", ELM_BLUE, ICON_OPEN, true, false, null);
				com.setBoolean("form", true);
				break;
			}
		}
		com.setTag("elements", list);
	}

	@Override
	public void on_interact(LDGuiContainer container, Player player, NBTTagCompound packet, String index){
		switch(index){
			case "create.submit":{
				Chunk_ chunk = ResManager.getChunk(container.player().entity);
				County county = chunk.district.county();
				long sum = Settings.MUNICIPALITY_CREATION_FEE;
    			boolean cn = county.norms.get("new-municipalities").bool();
    			boolean pp = player.hasPermit(ACT_CREATE_LAYER, county.getLayer(), county.id);
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
    			String name = packet.getCompoundTag("fields").getString("create.name_field");
    			if(name.length() < 1){
					container.sendMsg("create.name_too_short");
					return;
    			}
    			if(name.length() > 32){
					container.sendMsg("create.name_too_long");
					return;
    			}
				boolean uca = packet.getCompoundTag("checkboxes").getBoolean("create.county_funded");
				if(!pp && !uca) sum += county.norms.get("new-municipality-fee").integer(); 
				Permit perm = pp ? player.getPermit(ACT_CREATE_LAYER, county.getLayer(), county.id) : null;
				if(!pp && uca){
					container.sendMsg("create.no_fund_permit");
					return;
				}
				Account acc = pp && uca ? perm.getAccount() : player.account;
				if(acc.getBalance() < sum){
					container.sendMsg("create.not_enough_money");
					return;
				}
				boolean claim = packet.getCompoundTag("checkboxes").getBoolean("create.claim_district");
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
				mold.citizens.remove(player);
				cold.citizens.remove(player);
				Municipality mnew = new Municipality(newid);
				ResManager.MUNICIPALITIES.put(mnew.id, mnew);
				mnew.norms.get("name").set(name);
				mnew.citizens.add(player);
				county.citizens.add(player);
				player.municipality = mnew;
				player.county = county;
				mnew.manage.add(player);
				mnew.manage.setManager(player);
				mnew.county = county;
				county.municipalities.add(mnew.id);
				if(claim){
					mnew.districts.add(chunk.district.id);
					chunk.district.owner.set(mnew);
					chunk.district.manage.clear();
					chunk.district.save();
				}
				else{
					District dis = new District(ndid);
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
    			Announcer.announce(Announcer.Target.GLOBAL, 0, "announce.new_municipality", name, newid);
				return;
			}
		}
	}

}
