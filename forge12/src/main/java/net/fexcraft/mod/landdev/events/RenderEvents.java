package net.fexcraft.mod.landdev.events;

import net.fexcraft.lib.common.math.V3I;
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
			renderCube(ClientPropCache.space.pos, ClientPropCache.space.size);
		}
		if(ClientPropCache.visible){
			Minecraft.getMinecraft().getTextureManager().bindTexture(loc0);
			for(ClientPropCache.PropCache prop : ClientPropCache.cache.values()){
				renderCube(prop.pos, prop.size);
			}
		}
		GL11.glPopMatrix();
	}

	private void renderCube(V3I pos, V3I size){
		GL11.glPushMatrix();
		GL11.glTranslated(pos.x, pos.y, pos.z);
		//
		GL11.glPushMatrix();
		GL11.glScalef(size.x, 1, 1);
		ClientPropCache.polyx.render();
		GL11.glPopMatrix();
		//
		GL11.glPushMatrix();
		GL11.glScalef(1, size.y, 1);
		ClientPropCache.polyy.render();
		GL11.glPopMatrix();
		//
		GL11.glPushMatrix();
		GL11.glScalef(1, 1, size.z);
		ClientPropCache.polyz.render();
		GL11.glPopMatrix();
		//
		GL11.glTranslated(size.x, 0, 0);
		//
		GL11.glPushMatrix();
		GL11.glScalef(1, size.y, 1);
		ClientPropCache.polyy.render();
		GL11.glPopMatrix();
		//
		GL11.glPushMatrix();
		GL11.glScalef(1, 1, size.z);
		ClientPropCache.polyz.render();
		GL11.glPopMatrix();
		//
		GL11.glTranslated(0, 0, size.z);
		//
		GL11.glPushMatrix();
		GL11.glScalef(1, size.y, 1);
		ClientPropCache.polyy.render();
		GL11.glPopMatrix();
		//
		GL11.glTranslated(-size.x, 0, 0);
 		//
		GL11.glPushMatrix();
		GL11.glScalef(1, size.y, 1);
		ClientPropCache.polyy.render();
		GL11.glPopMatrix();
		//
		GL11.glPushMatrix();
		GL11.glScalef(size.x, 1, 1);
		ClientPropCache.polyx.render();
		GL11.glPopMatrix();
		//
		GL11.glTranslated(0, size.y, -size.z);
		//
		GL11.glPushMatrix();
		GL11.glScalef(size.x, 1, 1);
		ClientPropCache.polyx.render();
		GL11.glPopMatrix();
		//
		GL11.glPushMatrix();
		GL11.glScalef(1, 1, size.z);
		ClientPropCache.polyz.render();
		GL11.glPopMatrix();
		//
		GL11.glTranslated(size.x, 0, 0);
		//
		GL11.glPushMatrix();
		GL11.glScalef(1, 1, size.z);
		ClientPropCache.polyz.render();
		GL11.glPopMatrix();
		//
		GL11.glTranslated(-size.x, 0, size.z);
 		//
		GL11.glPushMatrix();
		GL11.glScalef(size.x, 1, 1);
		ClientPropCache.polyx.render();
		GL11.glPopMatrix();
 		//
		GL11.glPopMatrix();
	}

}
