package net.fexcraft.mod.landdev.gui;

import net.fexcraft.lib.mc.gui.GenericContainer;
import net.fexcraft.lib.mc.gui.GenericGui;
import net.fexcraft.lib.mc.registry.UCResourceLocation;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class LDGuiImgPreview extends GenericGui<GenericContainer> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft:textures/blocks/stone.png");
	public static UCResourceLocation IMG_URL;
	private int imgw, imgh;

	public LDGuiImgPreview(EntityPlayer player, int x, int y, int z){
		super(TEXTURE, new GenericContainer.DefImpl(player), player);
		this.deftexrect = false;
		xSize = 256;
		ySize = 256;
		imgw = x;
		imgh = y;
	}
	
	@Override
	public void init(){
		if(imgw > width){
			float r =  width / imgw;
			imgw *= r;
			imgh *= r;
		}
		if(imgh > height){
			float r = height / imgh;
			imgw *= r;
			imgh *= r;
		}
	}
	
	@Override
	public void drawbackground(float ticks, int mx, int my){
		//
	}
	
	@Override
	protected void drawlast(float pticks, int mouseX, int mouseY){
		mc.getRenderManager().renderEngine.bindTexture(IMG_URL);
		float x = (width - imgw) / 2, y = (height - imgh) / 2;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + imgh, 0).tex(0, 1).endVertex();
        bufferbuilder.pos(x + imgw, y + imgh, 0).tex(1, 1).endVertex();
        bufferbuilder.pos((x + imgw), y, 0).tex(1, 0).endVertex();
        bufferbuilder.pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();
	}
	
}