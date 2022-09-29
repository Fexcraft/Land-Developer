package net.fexcraft.mod.landdev.data.municipality;

import static net.fexcraft.mod.landdev.data.PermAction.ACT_CLAIM;
import static net.fexcraft.mod.landdev.data.PermAction.ACT_CREATE_LAYER;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ELM_GENERIC;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.ICON_BLANK;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.checkbox;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.landdev.data.*;
import net.fexcraft.mod.landdev.data.PermAction.PermActions;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.norm.StringNorm;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.modules.LDGuiModule;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class Municipality implements Saveable, Layer, LDGuiModule {

	public static PermActions actions = new PermActions(ACT_CLAIM);
	public final int id;
	public Createable created = new Createable();
	public Sellable sell = new Sellable(this);
	public IconHolder icon = new IconHolder();
	public ColorData color = new ColorData();
	public NeighborData neighbors = new NeighborData();
	public MailData mail = new MailData();
	public Manageable manage = new Manageable(true, actions);
	public Norms norms = new Norms();
	public ArrayList<Integer> districts = new ArrayList<>();
	public Account account;
	public County county;
	
	public Municipality(int id){
		this.id = id;
		account = DataManager.getAccount("municipality:" + id, false, true);
		norms.add(new StringNorm("name", translate("municipality.norm.name")));
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
				Chunk_ chunk = ResManager.getChunk(container.player().player);
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
				com.setBoolean("form", true);
				break;
			}
		}
		com.setTag("elements", list);
	}

	@Override
	public void on_interact(NBTTagCompound packet, String index, EntityPlayer player, Chunk_ chunk){
		// TODO Auto-generated method stub
		
	}

}
