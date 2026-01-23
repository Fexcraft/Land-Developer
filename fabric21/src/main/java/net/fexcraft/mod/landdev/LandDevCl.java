package net.fexcraft.mod.landdev;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.util.CTagListener;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.fexcraft.mod.landdev.util.LocationUpdate;
import net.fexcraft.mod.landdev.util.PropRenderer;
import net.fexcraft.mod.uni.tag.TagLW;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static net.fexcraft.mod.uni.ui.ContainerInterface.transformat;

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
			TagLW list = packet.getList("msg");
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
		});
		CTagListener.TASKS.put("img_preview_url", (packet, player) -> {
			//
		});
		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, LocationUpdate.ID, new LocationUpdate());
		WorldRenderEvents.AFTER_TRANSLUCENT.register(PropRenderer::renderProps);
	}

}