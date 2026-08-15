package net.fexcraft.mod.landdev;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.util.CTagListener;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.LocationUpdate;
import net.fexcraft.mod.landdev.util.PropRenderer;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.tag.TagLW;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static net.fexcraft.lib.common.utils.Formatter.format;
import static net.fexcraft.mod.uni.ui.ContainerInterface.TRANSFORMAT;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class LandDevCl implements ClientModInitializer {

	@Override
	public void onInitializeClient(){
		LDN.client_init();
		CTagListener.TASKS.put("location_update", (packet, player) -> {
			int time = packet.has("time") ? packet.getInteger("time") : 10;
			LocationUpdate.clear(Time.getDate() + (time * 1000));
			LocationUpdate.loadIcons(packet.getList("icons").local());
			LocationUpdate.loadLines(packet.getList("lines").local());
		});
		CTagListener.TASKS.put("chat_message", (packet, player) -> {
			TagCW msg = packet.getCompound("msg");
			TagLW lis = msg.getList("a");
			String c = msg.has("t") ? msg.getString("t") : "&a";
			Component text = null;
			if(msg.has("i")){
				text = Component.literal(lis.getString(1));
				//TODO
			}
			else{
				String str = TRANSFORMAT.apply(msg.getString("m"), lis.toArray());
				text = Component.literal(format(LDConfig.CHAT_OVERRIDE_LANG, c, msg.getString("s"), str));
			}
			Minecraft.getInstance().gui.getChat().addClientSystemMessage(text);
		});
		CTagListener.TASKS.put("img_preview_url", (packet, player) -> {
			//
		});
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, LocationUpdate.ID, new LocationUpdate());
		LevelRenderEvents.COLLECT_SUBMITS.register(PropRenderer::renderProps);
	}

}