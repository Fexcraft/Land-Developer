package net.fexcraft.mod.landdev.events;

import net.fexcraft.mod.landdev.data.prop.ClientPropCache;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class RenderEvents {

	private static ResourceLocation loc0 = new ResourceLocation("minecraft:textures/blocks/glowstone.png");
	private static ResourceLocation loc1 = new ResourceLocation("minecraft:textures/blocks/diamond_block.png");

	@SubscribeEvent
	public void renderProps(RenderWorldLastEvent event){
		if(!ClientPropCache.visible && ClientPropCache.space == null) return;
		Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
		double cx = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * event.getPartialTicks();
		double cy = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * event.getPartialTicks();
		double cz = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * event.getPartialTicks();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPushMatrix();
		GL11.glTranslated(-cx, -cy, -cz);
		if(ClientPropCache.space != null){
			Minecraft.getMinecraft().getTextureManager().bindTexture(loc1);
			ClientPropCache.renderCube(ClientPropCache.space.pos, ClientPropCache.space.size);
		}
		if(ClientPropCache.visible){
			Minecraft.getMinecraft().getTextureManager().bindTexture(loc0);
			for(ClientPropCache.PropCache prop : ClientPropCache.cache.values()){
				ClientPropCache.renderCube(prop.pos, prop.size);
			}
		}
		GL11.glPopMatrix();
	}

}
