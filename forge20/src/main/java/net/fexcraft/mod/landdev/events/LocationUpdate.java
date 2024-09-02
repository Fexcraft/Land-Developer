package net.fexcraft.mod.landdev.events;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.mod.fcl.util.ExternalTextures;
import net.fexcraft.mod.landdev.util.LDConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "landdev", bus = Mod.EventBusSubscriber.Bus.FORGE, value = { Dist.CLIENT })
public class LocationUpdate {

	public static final ResourceLocation texture = new ResourceLocation("landdev:textures/ui/location.png");
	public static ArrayList<String> lines = new ArrayList<>();
	public static ArrayList<ResourceLocation> icons = new ArrayList<>();
	public static int[] linelength = new int[4];
	public static long till = Time.getDate();
	private static int reswid;
	private static boolean quadcon;

	@SubscribeEvent
	public static void onLevelRender(RenderGuiOverlayEvent event){
		if(!event.getOverlay().id().getPath().equals("hotbar") || !shown()) return;
		reswid = event.getWindow().getGuiScaledWidth();
		if(LDConfig.LOCUP_SIDE){
			event.getGuiGraphics().blit(texture, 0, 0, 0, 0, 81, 37);
			if(quadcon){
				event.getGuiGraphics().blit(texture, 81, 0, 81, 0, 28, 29);
				event.getGuiGraphics().blit(texture, 110, 2, 110, 2, 22, 22);
			}
			else{
				event.getGuiGraphics().blit(texture, 81, 0, 105, 0, 4, 29);
				event.getGuiGraphics().blit(texture, 86, 2, 110, 2, 22, 22);
			}
			for(int i = 0; i < icons.size(); i++){
				if(i == 0) event.getGuiGraphics().blit(icons.get(i), 1, 1, 0, 0, 32, 32, 32, 32);
				else  event.getGuiGraphics().blit(icons.get(i), 33 + ((i - 1) * 24), 1, 0, 0, 24, 24, 24, 24);
			}
		}
		else{
			event.getGuiGraphics().blit(texture, reswid - 81, 0, 175, 0, 81, 37);
			if(quadcon){
				event.getGuiGraphics().blit(texture, reswid - 109, 0, 147, 0, 28, 29);
				event.getGuiGraphics().blit(texture, reswid - 132, 2, 110, 2, 22, 22);
			}
			else{
				event.getGuiGraphics().blit(texture, reswid - 85, 0, 147, 0, 4, 29);
				event.getGuiGraphics().blit(texture, reswid - 108, 2, 110, 2, 22, 22);
			}
			for(int i = 0; i < icons.size(); i++){
				if(i == 0) event.getGuiGraphics().blit(icons.get(i), reswid - 33, 1, 0, 0, 32, 32, 32, 32);
				else event.getGuiGraphics().blit(icons.get(i), reswid - 33 - (i * 24), 1, 0, 0, 24, 24, 24, 24);
			}
		}
		//
		event.getGuiGraphics().setColor(1, 1, 1, 1);
		Minecraft.getInstance().textureManager.bindForSetup(texture);
		for(int i = 0; i < lines.size(); i++){
			linelength[i] = Minecraft.getInstance().font.width(lines.get(i));
			if(lines.get(i).length() > 0) event.getGuiGraphics().blit(texture, LDConfig.LOCUP_SIDE ? 0 : reswid - linelength[i] - 4, 40 + (12 * i), 0, 39, linelength[i] + 4, 10);
		}
		for(int i = 0; i < lines.size(); i++){
			if(lines.get(i).length() > 0){
				if(LDConfig.LOCUP_SIDE){
					event.getGuiGraphics().drawString(Minecraft.getInstance().font, lines.get(i), 2, 41 + (12 * i), 0xffffff);
				}
				else{
					event.getGuiGraphics().drawString(Minecraft.getInstance().font, lines.get(i), reswid - linelength[i] - 2, 41 + (12 * i), 0xffffff);
				}
			}
		}
	}

	public static boolean shown(){
		return till >= Time.getDate();
	}

	public static void clear(long time){
		till = time;
		lines.clear();
		icons.clear();
	}

	public static void loadIcons(ListTag list){
		list.forEach(tag -> {
			icons.add(ExternalTextures.get("landdev", tag.getAsString()));
		});
		quadcon = icons.size() > 3;
	}

	public static void loadLines(ListTag list){
		list.forEach(tag -> {
			lines.add(Formatter.format(tag.getAsString()));
		});
	}

}
