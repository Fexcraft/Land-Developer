package net.fexcraft.mod.landdev.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fexcraft.mod.fcl.util.FCLRenderTypes;
import net.fexcraft.mod.fcl.util.Renderer20;
import net.fexcraft.mod.landdev.data.prop.ClientPropCache;
import net.fexcraft.mod.uni.IDL;
import net.fexcraft.mod.uni.IDLManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = "landdev", bus = Mod.EventBusSubscriber.Bus.FORGE, value = { Dist.CLIENT })
public class RenderEvents {

	private static IDL loc0 = IDLManager.getIDLCached("minecraft:textures/block/glowstone.png");
	private static IDL loc1 = IDLManager.getIDLCached("minecraft:textures/block/diamond_block.png");

	@SubscribeEvent
	public static void renderProps(RenderLevelStageEvent event){
		if(!ClientPropCache.visible && ClientPropCache.space == null) return;
		if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS) return;
		Camera camera = event.getCamera();
		double cx = camera.getPosition().x;
		double cy = camera.getPosition().y;
		double cz = camera.getPosition().z;
		PoseStack pose = event.getPoseStack();
		Renderer20.set(pose, Minecraft.getInstance().renderBuffers().bufferSource(), 0);
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
