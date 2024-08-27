package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.ui.ContainerInterface;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class BaseCon extends ContainerInterface {

	protected Chunk_ chunk;
	protected Player ldp;

	public BaseCon(JsonMap map, UniEntity ply, V3I pos){
		super(map, ply, pos);
		ldp = ResManager.getPlayer(ply);
		chunk = ldp.chunk_current;
	}

}
