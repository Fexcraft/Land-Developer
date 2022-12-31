package net.fexcraft.mod.landdev.events;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.render.ExternalTextureHelper;
import net.fexcraft.lib.mc.utils.Formatter;
import net.fexcraft.lib.tmt.ModelBase;
import net.fexcraft.mod.landdev.util.Settings;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LocationUpdate extends GuiScreen {

	public static final ResourceLocation texture = new ResourceLocation("landdev:textures/gui/location.png");
	private static final LocationUpdate INST = new LocationUpdate();
	public static ArrayList<String> lines = new ArrayList<>();
	public static ArrayList<ResourceLocation> icons = new ArrayList<>();
	public static int[] linelength = new int[4];
	public static long till = Time.getDate();
	private static Minecraft client;
	private static int reswid;
	private static boolean quadcon;
	
	public static boolean shown(){
		return till >= Time.getDate();
	}
	
	@SubscribeEvent
	public void displayLocationUpdate(RenderGameOverlayEvent event){
		if(event.getType() != ElementType.HOTBAR || !shown()) return;
		if(client == null) client = Minecraft.getMinecraft();
		ModelBase.bindTexture(texture);
		GL11.glEnable(GL11.GL_BLEND);
		reswid = event.getResolution().getScaledWidth();
		if(Settings.LOCUP_SIDE){
			INST.drawTexturedModalRect(0, 0, 0, 0, 81, 37);
			if(quadcon){
				INST.drawTexturedModalRect(81, 0, 81, 0, 28, 29);
				INST.drawTexturedModalRect(110, 2, 110, 2, 22, 22);
			}
			else{
				INST.drawTexturedModalRect(81, 0, 105, 0, 4, 29);
				INST.drawTexturedModalRect(86, 2, 110, 2, 22, 22);
			}
			for(int i = 0; i < icons.size(); i++){
				if(i == 0) draw(icons.get(i), 1, 1, 32, 32);
				else  draw(icons.get(i), 33 + ((i - 1) * 24), 1, 24, 24);
			}
		}
		else{
			INST.drawTexturedModalRect(reswid - 81, 0, 175, 0, 81, 37);
			if(quadcon){
				INST.drawTexturedModalRect(reswid - 109, 0, 147, 0, 28, 29);
				INST.drawTexturedModalRect(reswid - 132, 2, 110, 2, 22, 22);
			}
			else{
				INST.drawTexturedModalRect(reswid - 85, 0, 147, 0, 4, 29);
				INST.drawTexturedModalRect(reswid - 108, 2, 110, 2, 22, 22);
			}
			for(int i = 0; i < icons.size(); i++){
				if(i == 0) draw(icons.get(i), reswid - 33, 1, 32, 32);
				else  draw(icons.get(i), reswid - 33 - (i * 24), 1, 24, 24);
			}
		}
		//
		RGB.glColorReset();
		ModelBase.bindTexture(texture);
		for(int i = 0; i < lines.size(); i++){
			linelength[i] = client.fontRenderer.getStringWidth(lines.get(i));
			if(lines.get(i).length() > 0) INST.drawTexturedModalRect(Settings.LOCUP_SIDE ? 0 : reswid - linelength[i] - 4, 40 + (12 * i), 0, 39, linelength[i] + 4, 10);
		}
		for(int i = 0; i < lines.size(); i++){
			if(lines.get(i).length() > 0){
				if(Settings.LOCUP_SIDE) client.fontRenderer.drawString(lines.get(i), 2, 41 + (12 * i), MapColor.SNOW.colorValue);
				else  client.fontRenderer.drawString(lines.get(i), reswid - linelength[i] - 2, 41 + (12 * i), MapColor.SNOW.colorValue);
			}
		}
	}

	public static void clear(long time){
		till = time;
		lines.clear();
		icons.clear();
	}

	public static void loadIcons(NBTTagList tag){
		for(NBTBase base : tag){
			icons.add(ExternalTextureHelper.get(((NBTTagString)base).getString()));
		}
		quadcon = icons.size() > 3;
	}

	public static void loadLines(NBTTagList tag){
		for(NBTBase base : tag){
			lines.add(Formatter.format(((NBTTagString)base).getString()));
		}
	}

    public static void draw(ResourceLocation loc, int x, int y, int width, int height){
    	ModelBase.bindTexture(loc);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, (y + height), 0).tex(0, 1).endVertex();
        bufferbuilder.pos((x + width), (y + height), 0).tex(1, 1).endVertex();
        bufferbuilder.pos((x + width), y, 0).tex(1, 0).endVertex();
        bufferbuilder.pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();
    }
	
}