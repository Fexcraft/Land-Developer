package net.fexcraft.mod.landdev.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fexcraft.mod.fcl.util.FCLRenderTypes;
import net.fexcraft.mod.fcl.util.Renderer21;
import net.fexcraft.mod.landdev.data.prop.ClientPropCache;
import net.fexcraft.mod.uni.IDL;
import net.fexcraft.mod.uni.IDLManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class PropRenderer {

	private static IDL loc0 = IDLManager.getIDLCached("minecraft:textures/block/glowstone.png");
	private static IDL loc1 = IDLManager.getIDLCached("minecraft:textures/block/diamond_block.png");

	public static void renderProps(WorldRenderContext event){
		if(!ClientPropCache.visible && ClientPropCache.space == null) return;
		Camera camera = event.camera();
		double cx = camera.getPosition().x;
		double cy = camera.getPosition().y;
		double cz = camera.getPosition().z;
		PoseStack pose = event.matrixStack();
		Renderer21.set(pose, Minecraft.getInstance().renderBuffers().bufferSource(), 0);
		pose.pushPose();
		pose.translate(-cx, -cy, -cz);
		if(ClientPropCache.space != null){
			FCLRenderTypes.setCutout(loc1);
			ClientPropCache.renderCube(ClientPropCache.space.pos, ClientPropCache.space.size);
		}
		if(ClientPropCache.visible){
			FCLRenderTypes.setCutout(loc0);
			for(ClientPropCache.PropCache prop : ClientPropCache.cache.values()){
				ClientPropCache.renderCube(prop.pos, prop.size);
			}
		}
		pose.popPose();
	}

}
