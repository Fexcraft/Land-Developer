package net.fexcraft.mod.landdev.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.fexcraft.mod.landdev.ui.ChunkClaimUI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

/**
 * @author Ferdinand Calo'
 */
public class ClaimMapTexture {

	protected static ResourceLocation temptexid = new ResourceLocation("landdev:claimtemptex.png");

	public static void bind(ChunkClaimUI ui, int x, int z){
		if(Minecraft.getMinecraft().renderEngine.getTexture(temptexid) == null){
			ui.texts.get("title").value("landdev.gui.claim.loadingmap");
			ui.texts.get("title").translate();
			Minecraft.getMinecraft().renderEngine.loadTexture(temptexid, new TempTex(Minecraft.getMinecraft().world, temptexid, x, z));
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(temptexid);
	}

	public static class TempTex extends SimpleTexture {

		private static MutableBlockPos pos = new MutableBlockPos();
		private static final int grid = 15 * 16;
		private BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		private World world;
		private int sx, sz;

		public TempTex(World world, ResourceLocation rs, int x, int z){
			super(rs);
			this.world = world;
			sx = (x - 7) * 16;
			sz = (z - 7) * 16;
		}

		@Override
		public void loadTexture(IResourceManager resourceManager) throws IOException {
			if(image == null && textureLocation != null){
				super.loadTexture(resourceManager);
			}
			for(int i = 0; i < grid; i++){
				for(int j = 0; j < grid; j++){
					checkPos(i + sx, j + sz);
					IBlockState state = world.getBlockState(pos);
					image.setRGB(i, j, new Color(state.getMapColor(world, pos).colorValue).getRGB());
				}
			}
			if(image != null){
				if(textureLocation != null) deleteGlTexture();
				TextureUtil.uploadTextureImage(super.getGlTextureId(), image);
			}
		}

		private final BlockPos checkPos(int x, int z){
			for(int i = world.getHeight(); i > 0; i--){
				if(world.getBlockState(pos.setPos(x, i, z)).getBlock() != Blocks.AIR) return pos;
			}
			return new BlockPos(x, 0, z);
		}

	}

	public static void delete(){
		Minecraft.getMinecraft().renderEngine.deleteTexture(temptexid);
	}

}
