package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.landdev.data.ColorData;
import net.fexcraft.mod.landdev.data.IconHolder;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.ui.modules.MailModule;
import net.fexcraft.mod.landdev.ui.modules.MainModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static net.fexcraft.mod.landdev.ui.LDKeys.*;
import static net.fexcraft.mod.landdev.ui.LDKeys.COUNTY;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class BaseCon extends ContainerInterface {

	protected Chunk_ chunk;
	public LDPlayer ldp;
	protected UIKey type;
	protected String prefix;
	protected int backto;
	//
	protected LinkedHashMap<String, String> sfields = new LinkedHashMap<>();
	protected HashMap<String, Boolean> checkboxes = new HashMap<>();
	protected ArrayList<String> radioboxes = new ArrayList<>();
	protected String radiobox;
	public boolean nosubmit;
	public boolean form;

	public BaseCon(JsonMap map, UniEntity ply, V3I pos){
		super(map, ply, pos);
		ldp = ResManager.getPlayer(ply);
		chunk = ldp.chunk_current;
		type = KEY_MAIN;
		prefix = "main";
	}

	public void packet(TagCW com, boolean client){
		if(client){

			return;
		}
		if(com.has("sync")){
			sendResp();
		}
		if(com.has("interact")){
			ModuleRequest req = new ModuleRequest(com);
			switch(type.id){
				case MAIN:{
					MainModule.INST.on_interact(this, req);
					break;
				}
				case MAIL:{
					MailModule.INST.on_interact(this, req);
					break;
				}
				case CHUNK:{
					Chunk_ chunk = ResManager.getChunk(pos.y, pos.z);
					chunk.on_interact(this, req);
					break;
				}
				case DISTRICT:{
					District dis = ResManager.getDistrict(pos.y);
					if(dis != null){
						dis.on_interact(this, req);
						break;
					}
					break;
				}
				case MUNICIPALITY:{
					if(pos.x < 0){
						ResManager.getMunicipality(-1, true).on_interact(this, req);
						break;
					}
					Municipality mun = ResManager.getMunicipality(pos.y, pos.y > -2);
					if(mun != null){
						mun.on_interact(this, req);
						break;
					}
					break;
				}
				case COUNTY:{
					if(pos.x < 0){
						ResManager.getCounty(-1, true).on_interact(this, req);
						break;
					}
					County ct = ResManager.getCounty(pos.y, pos.y > -1);
					if(ct != null){
						ct.on_interact(this, req);
						break;
					}
					break;
				}
				default: LDUIModule.Missing.INST.on_interact(this, req); break;
			}
		}
		if(com.has("go_back")){
			if(pos.x != 0) open(backto);
			else{
				Chunk_ chunk = ResManager.getChunk(player.entity);
				open(KEY_MAIN, 0, chunk.key.x, chunk.key.z);
			}
		}
	}

	public void sendResp(){
		ModuleResponse resp = new ModuleResponse();
		IconHolder holder = null;
		ColorData color = null;
		switch(type.id){
			case MAIN:{
				MainModule.INST.sync_packet(this, resp);
				break;
			}
			case MAIL:{
				MailModule.INST.sync_packet(this, resp);
				break;
			}
			case CHUNK:{
				chunk.sync_packet(this, resp);
				break;
			}
			case DISTRICT:{
				District dis = ResManager.getDistrict(pos.y);
				if(dis != null){
					dis.sync_packet(this, resp);
					holder = dis.icon;
					color = dis.color;
					break;
				}
				break;
			}
			case MUNICIPALITY:{
				if(pos.x < 0){
					ResManager.getMunicipality(-1, true).sync_packet(this, resp);
					holder = chunk.district.county().icon;
					color = chunk.district.county().color;
					break;
				}
				Municipality mun = ResManager.getMunicipality(pos.y, pos.y > -2);
				if(mun != null){
					mun.sync_packet(this, resp);
					holder = mun.icon;
					color = mun.color;
					break;
				}
				break;
			}
			case COUNTY:{
				if(pos.x < 0){
					ResManager.getCounty(-1, true).sync_packet(this, resp);
					holder = chunk.district.county().icon;
					color = chunk.district.county().color;
					break;
				}
				County ct = ResManager.getCounty(pos.y, pos.y > -1);
				if(ct != null){
					ct.sync_packet(this, resp);
					holder = ct.icon;
					color = ct.color;
					break;
				}
				break;
			}
			default: LDUIModule.Missing.INST.sync_packet(this, resp); break;
		}
		if(holder != null){
			resp.getCompound().set("gui_icon", holder.getnn());
			resp.getCompound().set("gui_color", color.getInteger());
		}
		SEND_TO_CLIENT.accept(resp.build(), player);
	}

	public static class MainCon extends BaseCon {

		public MainCon(JsonMap map, UniEntity ply, V3I pos){
			super(map, ply, pos);
		}

	}

	public void open(int x){
		player.entity.openUI(type, x, pos.y, pos.z);
	}

	public void open(int x, int y, int z){
		player.entity.openUI(type, x, y, z);
	}

	public void open(UIKey type, int x, int y, int z){
		player.entity.openUI(type, x, y, z);
	}

	public void msg(String string, boolean addprefix){
		TagCW com = TagCW.create();
		com.set("msg", addprefix ? "landdev.gui." + prefix + "." + string : string);
		SEND_TO_CLIENT.accept(com, player);
	}

	public void msg(String string){
		msg(string, true);
	}

}
