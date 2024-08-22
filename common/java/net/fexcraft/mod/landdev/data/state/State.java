package net.fexcraft.mod.landdev.data.state;

import static net.fexcraft.mod.landdev.data.PermAction.STATE_STAFF;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.hooks.ExternalData;
import net.fexcraft.mod.landdev.data.norm.BoolNorm;
import net.fexcraft.mod.landdev.data.norm.FloatNorm;
import net.fexcraft.mod.landdev.data.norm.IntegerNorm;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.modules.LDGuiModule;
import net.fexcraft.mod.landdev.gui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.gui.modules.ModuleResponse;

public class State implements Saveable, Layer, LDGuiModule {

	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail;
	public Manageable manage = new Manageable(true, STATE_STAFF);
	public Norms norms = new Norms();
	public ArrayList<Integer> counties = new ArrayList<>();
	public ExternalData external = new ExternalData(this);
	public long tax_collected;
	public Account account;
	
	public State(int id){
		this.id = id;
		mail = new MailData(getLayer(), id);
		account = DataManager.getAccount("state:" + id, false, true);
		norms.add(new StringNorm("name", translate("state.norm.name")));
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
		external.load(map);
	}
	
	@Override
	public void gendef(){
		if(id == -1){
			norms.get("name").set(translate("state.wilderness.name"));
			norms.get("new-counties").set(true);
			counties.clear();
			counties.add(-1);
			color.set(0x009900);
		}
		else if(id == 0){
			norms.get("name").set(translate("state.spawnzone.name"));
			counties.clear();
			counties.add(0);
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
		return "states";
	}

	@Override
	public Layers getLayer(){
		return Layers.STATE;
	}

	@Override
	public Layers getParentLayer(){
		return Layers.UNION;
	}

	@Override
	public int lid(){
		return id;
	}

	public String name(){
		return norms.get("name").string();
	}

	@Override
	public void sync_packet(LDGuiContainer container, ModuleResponse resp){
		//
	}

	@Override
	public void on_interact(LDGuiContainer container, ModuleRequest req){
		//
	}

}
