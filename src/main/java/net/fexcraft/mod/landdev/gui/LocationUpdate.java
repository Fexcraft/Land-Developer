package net.fexcraft.mod.landdev.gui;

import org.lwjgl.opengl.GL11;

import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.tmt.ModelBase;
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
	public static long till = Time.getDate();
	private static Minecraft client;
	
	public static boolean shown(){
		return true;//till >= Time.getDate();
	}
	
	@SubscribeEvent
	public static void displayLocationUpdate(RenderGameOverlayEvent event){
		if(event.getType() != ElementType.HOTBAR || !shown()) return;
		if(client == null) client = Minecraft.getMinecraft();
		ModelBase.bindTexture(texture);
		GL11.glEnable(GL11.GL_BLEND);
		INST.drawTexturedModalRect(0, 0, 0, 0, 136, 40);
		//
		for(int i = 0; i < 4; i++){
			if(lines[i].length() == 0) continue;
			RGB.glColorReset();
			ModelBase.bindTexture(texture);
			INST.drawTexturedModalRect(0, 42 + (12 * i), 0, 84, client.fontRenderer.getStringWidth(lines[i]) + 4, 10);
			client.fontRenderer.drawString(lines[i], 2, 43 + (12 * i), MapColor.SNOW.colorValue);
		}
	}
	
}