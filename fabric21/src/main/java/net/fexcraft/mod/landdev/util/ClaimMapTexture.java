package net.fexcraft.mod.landdev.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.fexcraft.mod.landdev.ui.ChunkClaimUI;
import net.fexcraft.mod.uni.IDL;
import net.fexcraft.mod.uni.IDLManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ClaimMapTexture {

	protected static IDL temptexid = IDLManager.getIDLCached("landdev:claimtemptex.png");
	private static TempTex temptex;

	public static void bind(ChunkClaimUI ui, int x, int z){
		if(temptex == null){
			Minecraft.getInstance().getTextureManager().register(temptexid.local(), temptex = new TempTex(temptexid.local(), x, z));
		}
		ui.drawer.bind(temptexid);
	}

	public static class TempTex extends DynamicTexture {

		private static MutableBlockPos pos = new MutableBlockPos();
		private static final int grid = 15 * 16;
		private int sx, sz;

		public TempTex(ResourceLocation rs, int x, int z){
			super(rs::toString, new NativeImage(256, 256, true));
			sx = (x - 7) * 16;
			sz = (z - 7) * 16;
		}

		@Override
		public void upload(){
			for(int i = 0; i < grid; i++){
				for(int j = 0; j < grid; j++){
					checkPos(i + sx, j + sz);
					BlockState state = Minecraft.getInstance().level.getBlockState(pos);
					getPixels().setPixelABGR(i, j, state.getMapColor(Minecraft.getInstance().level, pos).calculateARGBColor(MapColor.Brightness.NORMAL));
				}
			}
			super.upload();
		}

		private BlockPos checkPos(int x, int z){
			for(int i = Minecraft.getInstance().level.getHeight(); i > 0; i--){
				if(!Minecraft.getInstance().level.getBlockState(pos.set(x, i, z)).canBeReplaced()) return pos;
			}
			return new BlockPos(x, 0, z);
		}

	}

	public static void delete(){
		Minecraft.getInstance().getTextureManager().release(temptexid.local());
		temptex = null;
	}

}
