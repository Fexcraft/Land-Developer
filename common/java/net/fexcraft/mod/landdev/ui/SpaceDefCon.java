package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class SpaceDefCon extends ContainerInterface {

	private LDPlayer ldp;
	protected boolean refresh;
	protected V3I pos;
	protected V3I size;

	public SpaceDefCon(JsonMap map, UniEntity ply, V3I pos){
		super(map, ply, pos);
		if(!ply.entity.isOnClient()){
			ldp = ResManager.getPlayer(ply);
		}
		else{
			sendToServer(com -> com.set("sync", true));
		}
	}

	@Override
	public void packet(TagCW com, boolean client){
		if(com.has("sync")){
			if(client){
				pos = com.getV3I("pos");
				size = com.getV3I("size");
				refresh = true;
			}
			else{
				sendSync();
			}
			return;
		}
		if(com.has("cancel")){
			ldp.defcache = null;
			player.entity.closeUI();
			return;
		}
		if(com.has("confirm")){
			ldp.defcache.finish();
			player.entity.closeUI();
			return;
		}
		if(com.has("x")) ldp.defcache.pos.x = com.getInteger("x");
		if(com.has("y")) ldp.defcache.pos.y = com.getInteger("y");
		if(com.has("z")) ldp.defcache.pos.z = com.getInteger("z");
		if(com.has("w")) ldp.defcache.size.x = Math.abs(com.getInteger("w"));
		if(com.has("h")) ldp.defcache.size.y = Math.abs(com.getInteger("h"));
		if(com.has("d")) ldp.defcache.size.z = Math.abs(com.getInteger("d"));
		sendSync();
	}

	private void sendSync(){
		sendToClient(player, com -> {
			com.set("sync", true);
			com.set("pos", ldp.defcache.pos);
			com.set("size", ldp.defcache.size);
		});
	}

}
