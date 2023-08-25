package net.fexcraft.mod.landdev.data.county;

import static net.fexcraft.mod.landdev.data.PermAction.ACT_CLAIM;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_MANAGE_FINANCES;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_SET_TAX_CHUNK;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_SET_TAX_PLAYER;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_USE_FINANCES;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.PermAction.PermActions;
import net.fexcraft.mod.landdev.data.norm.BoolNorm;
import net.fexcraft.mod.landdev.data.norm.IntegerNorm;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.landdev.util.ResManager;

public class County implements Saveable, Layer {

	public static PermActions mactions = new PermActions(ACT_CLAIM, ACT_SET_TAX_CHUNK, ACT_SET_TAX_PLAYER, ACT_USE_FINANCES, ACT_MANAGE_FINANCES);
	public static PermActions cactions = new PermActions(ACT_CLAIM);
	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail;
	public Manageable manage = new Manageable(true, mactions);
	public Norms norms = new Norms();
	public ArrayList<Integer> districts = new ArrayList<>();
	public ArrayList<Integer> municipalities = new ArrayList<>();
	public Citizens citizens = new Citizens(cactions);
	public Account account;
	public State state;
	
	public County(int id){
		this.id = id;
		mail = new MailData(getLayer(), id);
		account = DataManager.getAccount("county:" + id, false, true);
		norms.add(new StringNorm("name", translate("county.norm.name")));
		norms.add(new BoolNorm("new-municipalities", false));
		norms.add(new IntegerNorm("new-municipality-fee", 100000));
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
		JsonArray darray = new JsonArray();
		districts.forEach(dis -> darray.add(dis));
		map.add("districts", darray);
		JsonArray marray = new JsonArray();
		municipalities.forEach(mun -> marray.add(mun));
		map.add("municipalities", marray);
		map.add("state", state.id);
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
		if(map.has("municipalities")){
			JsonArray array = map.getArray("municipalities");
			municipalities.clear();
			array.value.forEach(elm -> municipalities.add(elm.integer_value()));
		}
		state = ResManager.getState(map.getInteger("state", -1), true);
	}
	
	@Override
	public void gendef(){
		if(id == -1){
			norms.get("name").set(translate("county.wilderness.name"));
			norms.get("new-municipalities").set(true);
			districts.clear();
			state = ResManager.getState(-1, true);
			color.set(0x009900);
		}
		else if(id == 0){
			norms.get("name").set(translate("county.spawnzone.name"));
			districts.clear();
			districts.add(0);
			state = ResManager.getState(0, true);
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

}
