package net.fexcraft.mod.landdev.util;

import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.common.utils.Formatter;
import net.fexcraft.mod.fcl.util.ExternalTextures;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;

public class LocationUpdate implements IdentifiedLayer {
	
	public static final ResourceLocation ID = ResourceLocation.parse("landdev:locupd");
	public static final ResourceLocation texture = ResourceLocation.parse("landdev:textures/ui/location.png");
	public static ArrayList<String> lines = new ArrayList<>();
	public static ArrayList<ResourceLocation> icons = new ArrayList<>();
	public static int[] linelength = new int[4];
	public static long till = Time.getDate();
	private static int reswid;
	private static boolean quadcon;
	
	@Override
	public ResourceLocation id(){
		return ID;
	}

	@Override
	public void render(GuiGraphics gg, DeltaTracker delta){
		if(!shown()) return;
		reswid = gg.guiWidth();
		if(LDConfig.LOCUP_SIDE){
			gg.blit(RenderType::guiTextured, texture, 0, 0, 0, 0, 81, 37, 256, 256, 0xffffffff);
			if(quadcon){
				gg.blit(RenderType::guiTextured, texture, 81, 0, 81, 0, 28, 29, 256, 256, 0xffffffff);
				gg.blit(RenderType::guiTextured, texture, 110, 2, 110, 2, 22, 22, 256, 256, 0xffffffff);
			}
			else{
				gg.blit(RenderType::guiTextured, texture, 81, 0, 105, 0, 4, 29, 256, 256, 0xffffffff);
				gg.blit(RenderType::guiTextured, texture, 86, 2, 110, 2, 22, 22, 256, 256, 0xffffffff);
			}
			for(int i = 0; i < icons.size(); i++){
				if(i == 0) gg.blit(RenderType::guiTextured, icons.get(i), 1, 1, 0, 0, 32, 32, 32, 32);
				else  gg.blit(RenderType::guiTextured, icons.get(i), 33 + ((i - 1) * 24), 1, 0, 0, 24, 24, 24, 24);
			}
		}
		else{
			gg.blit(RenderType::guiTextured, texture, reswid - 81, 0, 175, 0, 81, 37, 256, 256, 0xffffffff);
			if(quadcon){
				gg.blit(RenderType::guiTextured, texture, reswid - 109, 0, 147, 0, 28, 29, 256, 256, 0xffffffff);
				gg.blit(RenderType::guiTextured, texture, reswid - 132, 2, 110, 2, 22, 22, 256, 256, 0xffffffff);
			}
			else{
				gg.blit(RenderType::guiTextured, texture, reswid - 85, 0, 147, 0, 4, 29, 256, 256, 0xffffffff);
				gg.blit(RenderType::guiTextured, texture, reswid - 108, 2, 110, 2, 22, 22, 256, 256, 0xffffffff);
			}
			for(int i = 0; i < icons.size(); i++){
				if(i == 0) gg.blit(RenderType::guiTextured, icons.get(i), reswid - 33, 1, 0, 0, 32, 32, 32, 32);
				else gg.blit(RenderType::guiTextured, icons.get(i), reswid - 33 - (i * 24), 1, 0, 0, 24, 24, 24, 24);
			}
		}
		//
		RenderType.guiTextured(texture);
		for(int i = 0; i < lines.size(); i++){
			linelength[i] = Minecraft.getInstance().font.width(lines.get(i));
			if(lines.get(i).length() > 0) gg.blit(RenderType::guiTextured, texture, LDConfig.LOCUP_SIDE ? 0 : reswid - linelength[i] - 4, 40 + (12 * i), 0, 39, linelength[i] + 4, 10, 256, 256, 0xffffffff);
		}
		for(int i = 0; i < lines.size(); i++){
			if(lines.get(i).length() > 0){
				if(LDConfig.LOCUP_SIDE){
					gg.drawString(Minecraft.getInstance().font, lines.get(i), 2, 41 + (12 * i), 0xffffff);
				}
				else{
					gg.drawString(Minecraft.getInstance().font, lines.get(i), reswid - linelength[i] - 2, 41 + (12 * i), 0xffffff);
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
			icons.add(ExternalTextures.get("landdev", tag.asString().get()).local());
		});
		quadcon = icons.size() > 3;
	}

	public static void loadLines(ListTag list){
		list.forEach(tag -> {
			lines.add(Formatter.format(tag.asString().get()));
		});
	}
	
}
