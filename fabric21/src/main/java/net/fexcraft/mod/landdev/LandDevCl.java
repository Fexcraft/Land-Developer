package net.fexcraft.mod.landdev;

import net.fabricmc.api.ClientModInitializer;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fcl.FCL;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.uni.tag.TagLW;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;

import static net.fexcraft.mod.uni.ui.ContainerInterface.transformat;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class LandDevCl implements ClientModInitializer {

	@Override
	public void onInitializeClient(){
		FCL.addListener("landdev", true, (com, player) -> {
			switch(com.getString("task")){
				case "location_update":{
					int time = com.has("time") ? com.getInteger("time") : 10;
					/*LocationUpdate.clear(Time.getDate() + (time * 1000));
					LocationUpdate.loadIcons(com.getList("icons").local());
					LocationUpdate.loadLines(com.getList("lines").local());*/
					return;
				}
				case "chat_message":{
					TagLW list = com.getList("msg");
					String c = list.size() > 3 ? list.getString(3) : "\u00A7a";
					Component text = null;
					switch(list.getString(0)){
						case "chat_img":
							text = Component.literal(list.getString(2));
							//TODO
							break;
						case "chat":
						default:
							text = Component.literal(transformat(LDConfig.CHAT_OVERRIDE_LANG, c, list.getString(1), list.getString(2)));
							break;
					}
					Minecraft.getInstance().gui.getChat().addMessage(text);
					return;
				}
			}
		});
	}

}