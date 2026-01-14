package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIButton;
import net.fexcraft.mod.uni.ui.UserInterface;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class SpaceDefUI extends UserInterface {

	private SpaceDefCon con;

	public SpaceDefUI(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		con = (SpaceDefCon)container;
	}

	@Override
	public void predraw(float ticks, int mx, int my){
		if(!con.refresh) return;
		fields.get("pos_x").text(con.pos.x);
		fields.get("pos_y").text(con.pos.y);
		fields.get("pos_z").text(con.pos.z);
		fields.get("size_x").text(con.size.x);
		fields.get("size_y").text(con.size.y);
		fields.get("size_z").text(con.size.z);
		con.refresh = false;
	}

	@Override
	public boolean onAction(UIButton button, String id, int x, int y, int b){
		TagCW com = TagCW.create();
		switch(id){
			case "confirm":{
				com.set("confirm", true);
				break;
			}
			case "cancel":{
				com.set("cancel", true);
				break;
			}
			case "copy":{
				V3I pos = container.player.entity.getV3I();
				com.set("x", pos.x);
				com.set("y", pos.y);
				com.set("z", pos.z);
				break;
			}
			case "reset":{
				com.set("w", 1);
				com.set("h", 1);
				com.set("d", 1);
				break;
			}
			case "send":{
				com.set("x", fields.get("pos_x").number());
				com.set("y", fields.get("pos_y").number());
				com.set("z", fields.get("pos_z").number());
				com.set("w", fields.get("size_x").number());
				com.set("h", fields.get("size_y").number());
				com.set("d", fields.get("size_z").number());
				break;
			}
			case "x+":{
				com.set("x", con.pos.x + 1);
				break;
			}
			case "x-":{
				com.set("x", con.pos.x - 1);
				break;
			}
			case "y+":{
				com.set("y", con.pos.y + 1);
				break;
			}
			case "y-":{
				com.set("y", con.pos.y - 1);
				break;
			}
			case "z+":{
				com.set("z", con.pos.z + 1);
				break;
			}
			case "z-":{
				com.set("z", con.pos.z - 1);
				break;
			}
			case "w+":{
				com.set("w", con.size.x + 1);
				break;
			}
			case "w-":{
				com.set("w", con.size.x - 1);
				break;
			}
			case "h+":{
				com.set("h", con.size.y + 1);
				break;
			}
			case "h-":{
				com.set("h", con.size.y - 1);
				break;
			}
			case "d+":{
				com.set("d", con.size.z + 1);
				break;
			}
			case "d-":{
				com.set("d", con.size.z - 1);
				break;
			}
		}
		if(!com.empty()){
			ContainerInterface.SEND_TO_SERVER.accept(com);
			return true;
		}
		return false;
	}

	@Override
	public boolean onScroll(UIButton button, String id, int mx, int my, int am){
		return false;
	}

}
