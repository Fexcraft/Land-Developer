package net.fexcraft.mod.landdev.util;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.ArrayList;

public abstract class ProtectorInstance {

	public static ProtectorInstance get(boolean abs, JsonMap map){
		return abs ? new Absolute() : new Default(map.getArray("blocks"));
	}

	public abstract boolean isProtected(IBlockState state);

	public static class Absolute extends ProtectorInstance {

		@Override
		public boolean isProtected(IBlockState state){
			return true;
		}
	}

	public static class Default extends ProtectorInstance {

		private ArrayList<Block> blocks = new ArrayList<>();

		public Default(JsonArray array){
			array.value.forEach(elm -> {
				Block block = Block.getBlockFromName(elm.string_value());
				if(block != null) blocks.add(block);
			});
		}

		@Override
		public boolean isProtected(IBlockState state){
			return blocks.contains(state.getBlock());
		}

	}

}
