package net.fexcraft.mod.landdev.util;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Optional;

public abstract class ProtectorInstance {

	public static ProtectorInstance get(boolean abs, JsonMap map){
		return abs ? new Absolute() : new Default(map.getArray("blocks"));
	}

	public abstract boolean isProtected(BlockState state);

	public static class Absolute extends ProtectorInstance {

		@Override
		public boolean isProtected(BlockState state){
			return true;
		}
	}

	public static class Default extends ProtectorInstance {

		private ArrayList<Block> blocks = new ArrayList<>();

		public Default(JsonArray array){
			array.value.forEach(elm -> {
				try{
					Optional<Holder.Reference<Block>> block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(elm.string_value()));
					if(block.isPresent()) blocks.add(block.get().value());
				}
				catch(Exception e){
					e.printStackTrace();
				}
			});
		}

		@Override
		public boolean isProtected(BlockState state){
			return blocks.contains(state.getBlock());
		}

	}

}
