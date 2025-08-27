package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.landdev.util.ClaimMapTexture;
import net.fexcraft.mod.uni.IDL;
import net.fexcraft.mod.uni.IDLManager;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIButton;
import net.fexcraft.mod.uni.ui.UIElement;
import net.fexcraft.mod.uni.ui.UserInterface;

import java.util.List;

import static net.fexcraft.mod.uni.ui.ContainerInterface.*;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ChunkClaimUI extends UserInterface {

	protected ChunkClaimCon con;
	private static final IDL TEXTURE = IDLManager.getIDLCached("landdev:textures/ui/claim.png");
	private static UIButton[][] ckbuttons = new UIButton[15][15];
	private static Integer lx, lz;
	private boolean gridview = true;
	private boolean mapgrid = true;
	private boolean deltex;

	public ChunkClaimUI(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		con = (ChunkClaimCon)container;
		ChunkKey key = new ChunkKey(con.player.entity.getPos());
		if(lx != null && (key.x != lx || key.z != lz)) deltex = true;
		lx = key.x;
		lz = key.z;
	}

	@Override
	public void init(){
		TagCW com = TagCW.create();
		com.set("sync", true);
		SEND_TO_SERVER.accept(com);
		JsonMap empty = new JsonMap();
		try{
			for(int i = 0; i < 15; i++){
				for(int k = 0; k < 15; k++){
					UIButton button = UIElement.create(UIButton.IMPLEMENTATION, this, empty);
					button.x = button.tx = button.htx = 6 + i * 14;
					button.y = button.ty = button.hty = 20 + k * 14;
					button.width = button.height = 12;
					tabs.get("main").buttons.put("ck_" + i + "_" + k, button);
					buttons.put("ck_" + i + "_" + k, button);
					ckbuttons[i][k] = button;
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		texts.get("title").translate();
	}

	@Override
	public void predraw(float ticks, int mx, int my){
		//
	}

	@Override
	public void postdraw(float ticks, int mx, int my){
		if(!gridview){
			if(mapgrid){
				for(int i = 0; i < con.chunks.length; i++){
					for(int k = 0; k < con.chunks[i].length; k++){
						drawer.apply(con.chunks[i][k].color);
						drawer.draw(gLeft + 5 + (i * 14), gTop + 19 + (k * 14), 240, 240, 14, 14);
						drawer.applyWhite();
					}
				}
			}
			ClaimMapTexture.bind(this, lx, lz);
			for(int i = 0; i < con.chunks.length; i++){
				for(int k = 0; k < con.chunks[i].length; k++){
					drawer.draw(gLeft + 6 + (i * 14), gTop + 20 + (k * 14), i * 16, k * 16, 12, 12);
				}
			}
		}
		else{
			for(int i = 0; i < con.chunks.length; i++){
				for(int k = 0; k < con.chunks[i].length; k++){
					drawer.apply(con.chunks[i][k].color);
					drawer.draw(gLeft + 6 + (i * 14), gTop + 20 + (k * 14), 6, 20, 12, 12);
					drawer.applyWhite();
				}
			}
		}
		//drawer.bind(TEXTURE);
	}

	@Override
	public boolean onAction(UIButton button, String id, int x, int y, int b){
		if(id.startsWith("ck_")){
			String[] arr = id.split("_");
			int cx = Integer.parseInt(arr[1]);
			int cz = Integer.parseInt(arr[2]);
			TagCW compound = TagCW.create();
			compound.set("claim", new int[]{ cx, cz });
			SEND_TO_SERVER.accept(compound);
			return true;
		}
		switch(id){
			case "map":{
				if(deltex){
					ClaimMapTexture.delete();
					deltex = false;
				}
				gridview = false;
				return true;
			}
			case "grid":{
				gridview = true;
				return true;
			}
			case "map_grid":{
				mapgrid = !mapgrid;
				return true;
			}
		}
		return false;
	}

	@Override
	public void getTooltip(int mx, int my, List<String> list){
		for(int i = 0; i < 15; i++){
			for(int k = 0; k < 15; k++){
				if(ckbuttons[i][k].hovered()){
					ChunkClaimCon.ChunkData ck = con.chunks[i][k];
					list.add(transformat("landdev.gui.claim.chunk_coord", ck.x, ck.z));
					if(ck.price > 0) list.add(transformat("landdev.gui.claim.chunk_price", Config.getWorthAsString(ck.price)));
					ChunkClaimCon.DisData dis = con.dists.get(ck.dis);
					if(dis == null) return;
					list.add(transformat("landdev.gui.claim.district", dis.name + " (" + dis.id + ")"));
					list.add(transformat("landdev.gui.claim." + (dis.county ? "county" : "municipality"), dis.cname + " (" + dis.cid + ")"));
					if(ck.locked){
						list.add(transformat("landdev.gui.claim.locked"));
					}
				}
			}
		}
		if(texts.get("title").hovered()) list.add(texts.get("title").value());
		if(buttons.get("grid").hovered()) list.add(translate("landdev.gui.claim.gridmode"));
		if(buttons.get("map").hovered()) list.add(translate("landdev.gui.claim.mapmode"));
		if(buttons.get("map_grid").hovered()) list.add(translate("landdev.gui.claim.mapgrid"));
	}

}
