package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UserInterface;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class BaseUI extends UserInterface {

	public BaseUI(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		sync();
	}

	private void sync(){
		TagCW com = TagCW.create();
		com.set("sync", true);
		container.SEND_TO_SERVER.accept(com);
	}

}
