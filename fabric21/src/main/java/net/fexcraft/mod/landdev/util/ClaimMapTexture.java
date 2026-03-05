package net.fexcraft.mod.landdev.util;

import net.fexcraft.mod.fcl.UniFCL;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.world.WorldW;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ClaimMapTexture {

	private static final int grid = 15 * 16;

	public static void gen(ChunkKey key, WorldW world){
		File file = new File(UniFCL.SF_FOLDER,"landdev/claim_view/" + key.x + "_" + key.z + ".png");
		if(file.exists()) return;
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		ServerLevel level = world.local();
		MutableBlockPos pos = new MutableBlockPos();
		BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
		BlockState state;
		int sx = (key.x - 7) * 16;
		int sz = (key.z - 7) * 16;
		for(int i = 0; i < grid; i++){
			for(int j = 0; j < grid; j++){
				pos.set(i + sx, level.getHeight() - 1, j + sz);
				state = level.getBlockState(pos);
				img.setRGB(i, j, state.getMapColor(level, pos).calculateARGBColor(MapColor.Brightness.NORMAL));
			}
		}
		try{
			if(EnvInfo.DEV) LandDev.log("Writing: " + file);
			ImageIO.write(img, "PNG", file);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

}
