package net.fexcraft.mod.landdev.dynmap;

import net.fexcraft.lib.common.math.RGB;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.util.math.Vec3i;
import org.dynmap.*;
import org.dynmap.common.DynmapCommandSender;
import org.dynmap.exporter.OBJExport;
import org.dynmap.hdmap.*;
import org.dynmap.json.simple.JSONObject;
import org.dynmap.renderer.DynmapBlockState;
import org.dynmap.utils.BlockStep;
import org.dynmap.utils.DynLongHashMap;
import org.dynmap.utils.MapChunkCache;
import org.dynmap.utils.MapIterator;

import java.io.IOException;

/**
 * General Dynmap Shader for Land-Developer Layers
 *
 * @author Ferdinand Calo' (FEX___96)
 */
public class LandDevShader implements HDShader {

	private String name;
	private Layers layer;

	public LandDevShader(DynmapCore core, ConfigurationNode configuration){
		name = (String)configuration.get("name");
		layer = Layers.valueOf(((String)configuration.get("layer")).toUpperCase());
	}

	@Override
	public HDShaderState getStateInstance(HDMap map, MapChunkCache cache, MapIterator mapiter, int scale){
		return new ShaderState(this, mapiter, map, cache, scale);
	}

	@Override
	public boolean isBiomeDataNeeded(){
		return false;
	}

	@Override
	public boolean isRawBiomeDataNeeded(){
		return false;
	}

	@Override
	public boolean isHightestBlockYDataNeeded(){
		return false;
	}

	@Override
	public boolean isBlockTypeDataNeeded(){
		return true;
	}

	@Override
	public boolean isSkyLightLevelNeeded(){
		return false;
	}

	@Override
	public boolean isEmittedLightLevelNeeded(){
		return false;
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
    public void addClientConfiguration(JSONObject mapObject){
        JSONUtils.s(mapObject, "shader", name);
    }

	@Override
	public void exportAsMaterialLibrary(DynmapCommandSender dynmapCommandSender, OBJExport objExport) throws IOException{
		//
	}

	@Override
	public String[] getCurrentBlockMaterials(DynmapBlockState dynmapBlockState, MapIterator mapIterator, int[] ints, BlockStep[] blockSteps){
		return new String[0];
	}

	private static class ShaderState implements HDShaderState {

		private Color[] colors = new Color[]{ new Color() };
		private Color color = new Color();
		protected HDMap map;
		private HDLighting lighting;
		final int[] table;
		private RGB rgb = new RGB();
		private LandDevShader shader;
		private MapIterator mapi;

		private ShaderState(LandDevShader ldshader, MapIterator mapiter, HDMap hdmap, MapChunkCache cache, int scale){
			map = hdmap;
			mapi = mapiter;
			shader = ldshader;
			lighting = map.getLighting();
			table = MapManager.mapman.useBrightnessTable() ? cache.getWorld().getBrightnessTable() : null;
		}

		@Override
		public HDShader getShader(){
			return shader;
		}

		@Override
		public HDMap getMap(){
			return map;
		}

		@Override
		public HDLighting getLighting(){
			return lighting;
		}

		@Override
		public void reset(HDPerspectiveState ps){
			for(int i = 0; i < this.colors.length; ++i){
				colors[i].setTransparent();
			}
		}

		@Override
		public boolean processBlock(HDPerspectiveState ps){
			Chunk_ ck = ResManager.getChunk(new Vec3i(mapi.getX(), mapi.getY(), mapi.getZ()));
			switch(shader.layer){
				case COMPANY:
					rgb.packed = RGB.BLUE.packed;
					break;
				case DISTRICT:
					rgb.packed = ck.district.color.getInteger();
					break;
				case MUNICIPALITY:
					if(ck.district.municipality() == null){
						rgb.packed = ResManager.getMunicipality(-1, true).color.getInteger();
					}
					else rgb.packed = ck.district.municipality().color.getInteger();
					break;
				case COUNTY:
					rgb.packed = ck.district.county().color.getInteger();
					break;
				case STATE:
					rgb.packed = ck.district.state().color.getInteger();
					break;
				case UNION:
					rgb.packed = RGB.RED.packed;
					break;
				case NONE:
					rgb.packed = RGB.WHITE.packed;
					break;
			}
			byte[] arr = rgb.toByteArray();
			color.setRGBA(arr[0] + 128, arr[1] + 128, arr[2] + 128, 255);
			lighting.applyLighting(ps, this, this.color, this.colors);
			return true;
		}

		@Override
		public void rayFinished(HDPerspectiveState ps){
			//
		}

		@Override
		public void getRayColor(Color color, int index){
			color.setColor(colors[index]);
		}

		@Override
		public void cleanup(){
			//
		}

		@Override
		public DynLongHashMap getCTMTextureCache(){
			return null;
		}

		@Override
		public int[] getLightingTable(){
			return table;
		}

		@Override
		public void setLastBlockState(DynmapBlockState dbs){

		}

	}

}
