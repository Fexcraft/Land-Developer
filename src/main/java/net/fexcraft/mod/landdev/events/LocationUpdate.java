package net.fexcraft.mod.landdev.events;

import org.lwjgl.opengl.GL11;

import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.tmt.ModelBase;
import net.fexcraft.mod.landdev.util.Settings;
import net.minecraft.block.material.MapColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class LocationUpdate extends GuiScreen {

	public static final ResourceLocation texture = new ResourceLocation("landdev:textures/gui/location.png");
	private static final LocationUpdate INST = new LocationUpdate();
	public static String[] lines = new String[]{ "line 0", "line 1", "line 2", "line 3" };
	public static ResourceLocation[] icon = new ResourceLocation[4];
	public static int[] linelength = new int[4];
	public static long till = Time.getDate();
	private static Minecraft client;
	private static int reswid;
	
	public static boolean shown(){
		return true;//till >= Time.getDate();
	}
	
	@SubscribeEvent
	public static void displayLocationUpdate(RenderGameOverlayEvent event){
		if(event.getType() != ElementType.HOTBAR || !shown()) return;
		if(client == null) client = Minecraft.getMinecraft();
		ModelBase.bindTexture(texture);
		GL11.glEnable(GL11.GL_BLEND);
		reswid = event.getResolution().getScaledWidth();
		if(Settings.LOCUP_SIDE){
			INST.drawTexturedModalRect(0, 0, 0, 0, 109, 37);
			INST.drawTexturedModalRect(110, 2, 110, 2, 22, 22);
		}
		else{
			INST.drawTexturedModalRect(reswid - 109, 0, 147, 0, 109, 37);
			INST.drawTexturedModalRect(reswid - 132, 2, 110, 2, 22, 22);
		}
		//
		RGB.glColorReset();
		ModelBase.bindTexture(texture);
		for(int i = 0; i < 4; i++){
			linelength[i] = client.fontRenderer.getStringWidth(lines[i]);
			if(lines[i].length() > 0) INST.drawTexturedModalRect(Settings.LOCUP_SIDE ? 0 : reswid - linelength[i] - 4, 40 + (12 * i), 0, 39, linelength[i] + 4, 10);
		}
		for(int i = 0; i < 4; i++){
			if(lines[i].length() > 0){
				if(Settings.LOCUP_SIDE) client.fontRenderer.drawString(lines[i], 2, 41 + (12 * i), MapColor.SNOW.colorValue);
				else  client.fontRenderer.drawString(lines[i], reswid - linelength[i] - 2, 41 + (12 * i), MapColor.SNOW.colorValue);
			}
		}
	}
	
}