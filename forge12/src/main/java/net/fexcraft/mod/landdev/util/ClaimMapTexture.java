package net.fexcraft.mod.landdev.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import net.fexcraft.mod.fcl.UniFCL;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.ChunkKey;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.world.WorldW;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

import javax.imageio.ImageIO;

/**
 * @author Ferdinand Calo'
 */
public class ClaimMapTexture {

	private static final int grid = 15 * 16;

	public static void gen(ChunkKey key, WorldW world){
		File file = new File(UniFCL.SF_FOLDER,"landdev/claim_view/" + key.x + "_" + key.z + ".png");
		if(file.exists()) return;
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		World level = world.local();
		MutableBlockPos pos = new MutableBlockPos();
		BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		IBlockState state;
		int sx = (key.x - 7) * 16;
		int sz = (key.z - 7) * 16;
		for(int i = 0; i < grid; i++){
			for(int j = 0; j < grid; j++){
				checkPos(level, pos, i + sx, j + sz);
				state = level.getBlockState(pos);
				img.setRGB(i, j, new Color(state.getMapColor(level, pos).colorValue).getRGB());
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

	private static BlockPos checkPos(World world, MutableBlockPos pos, int x, int z){
		Block block;
		for(int i = world.getHeight() - 1; i > 0; i--){
			pos.setPos(x, i, z);
			block = world.getBlockState(pos).getBlock();
			if(block == Blocks.WATER || block == Blocks.FLOWING_WATER) return pos;
			if(!block.isReplaceable(world, pos)) return pos;
		}
		return new BlockPos(x, 0, z);
	}

}
