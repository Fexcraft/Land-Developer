package net.fexcraft.mod.landdev.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fexcraft.mod.landdev.ui.ChunkClaimUI;
import net.fexcraft.mod.uni.IDL;
import net.fexcraft.mod.uni.IDLManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import java.io.IOException;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ClaimMapTexture {

	protected static IDL temptexid = IDLManager.getIDLCached("landdev:claimtemptex.png");
	private static TempTex temptex;

	public static void bind(ChunkClaimUI ui, int x, int z){
		if(temptex == null){
			Minecraft.getInstance().textureManager.register(temptexid.local(), temptex = new TempTex(temptexid.local(), x, z));
		}
		ui.drawer.bind(temptexid);
	}

	public static class TempTex extends SimpleTexture {

		private static MutableBlockPos pos = new MutableBlockPos();
		private static final int grid = 15 * 16;
		private NativeImage image = new NativeImage(256, 256, true);
		private Level world;
		private int sx, sz;

		public TempTex(ResourceLocation rs, int x, int z){
			super(rs);
			world = Minecraft.getInstance().level;
			sx = (x - 7) * 16;
			sz = (z - 7) * 16;
		}

		@Override
		public void load(ResourceManager resman) throws IOException {
			for(int i = 0; i < grid; i++){
				for(int j = 0; j < grid; j++){
					checkPos(i + sx, j + sz);
					BlockState state = world.getBlockState(pos);
					image.setPixelRGBA(i, j, state.getMapColor(world, pos).calculateRGBColor(MapColor.Brightness.NORMAL));
				}
			}
			if(!RenderSystem.isOnRenderThreadOrInit()){
				RenderSystem.recordRenderCall(() -> {
					TextureUtil.prepareImage(getId(), 0, image.getWidth(), image.getHeight());
					image.upload(0, 0, 0, true);
				});
			}
			else{
				TextureUtil.prepareImage(getId(), 0, image.getWidth(), image.getHeight());
				image.upload(0, 0, 0, true);
			}
		}

		private BlockPos checkPos(int x, int z){
			for(int i = world.getHeight(); i > 0; i--){
				if(!world.getBlockState(pos.set(x, i, z)).canBeReplaced()) return pos;
			}
			return new BlockPos(x, 0, z);
		}

	}

	public static void delete(){
		Minecraft.getInstance().textureManager.release(temptexid.local());
		temptex = null;
	}

}
