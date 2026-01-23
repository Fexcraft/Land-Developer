package net.fexcraft.mod.landdev.data.prop;

import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.lib.frl.Polyhedron;
import net.fexcraft.lib.frl.gen.Generator.Values;
import net.fexcraft.lib.frl.gen.Generator_Cuboid;
import net.fexcraft.lib.frl.gen.ValueMap;

import java.util.concurrent.ConcurrentHashMap;

import static net.fexcraft.lib.common.Static.sixteenth;
import static net.fexcraft.lib.frl.Renderer.RENDERER;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ClientPropCache {

	public static ConcurrentHashMap<Integer, PropCache> cache = new ConcurrentHashMap<>();
	public static Polyhedron<?> polyx = new Polyhedron<>();
	public static Polyhedron<?> polyy = new Polyhedron<>();
	public static Polyhedron<?> polyz = new Polyhedron<>();
	public static boolean visible;
	public static PropCache space;
	static{
		ValueMap map = new ValueMap();
		map.put(Values.WIDTH, 16f);
		map.put(Values.HEIGHT, 1f);
		map.put(Values.DEPTH, 1f);
		map.put(Values.SCALE, sixteenth);
		Generator_Cuboid.make(polyx, map);
		map.put(Values.WIDTH, 1f);
		map.put(Values.HEIGHT, 16f);
		Generator_Cuboid.make(polyy, map);
		map.put(Values.HEIGHT, 1f);
		map.put(Values.DEPTH, 16f);
		Generator_Cuboid.make(polyz, map);
	}

	public static class PropCache {

		public V3I pos;
		public V3I size;

	}
	
	public static void renderCube(V3I pos, V3I size){
		RENDERER.push();
		RENDERER.translate(pos.x, pos.y, pos.z);
		//
		RENDERER.push();
		RENDERER.scale(size.x, 1, 1);
		ClientPropCache.polyx.render();
		RENDERER.pop();
		//
		RENDERER.push();
		RENDERER.scale(1, size.y, 1);
		ClientPropCache.polyy.render();
		RENDERER.pop();
		//
		RENDERER.push();
		RENDERER.scale(1, 1, size.z);
		ClientPropCache.polyz.render();
		RENDERER.pop();
		//
		RENDERER.translate(size.x, 0, 0);
		//
		RENDERER.push();
		RENDERER.scale(1, size.y, 1);
		ClientPropCache.polyy.render();
		RENDERER.pop();
		//
		RENDERER.push();
		RENDERER.scale(1, 1, size.z);
		ClientPropCache.polyz.render();
		RENDERER.pop();
		//
		RENDERER.translate(0, 0, size.z);
		//
		RENDERER.push();
		RENDERER.scale(1, size.y, 1);
		ClientPropCache.polyy.render();
		RENDERER.pop();
		//
		RENDERER.translate(-size.x, 0, 0);
 		//
		RENDERER.push();
		RENDERER.scale(1, size.y, 1);
		ClientPropCache.polyy.render();
		RENDERER.pop();
		//
		RENDERER.push();
		RENDERER.scale(size.x, 1, 1);
		ClientPropCache.polyx.render();
		RENDERER.pop();
		//
		RENDERER.translate(0, size.y, -size.z);
		//
		RENDERER.push();
		RENDERER.scale(size.x, 1, 1);
		ClientPropCache.polyx.render();
		RENDERER.pop();
		//
		RENDERER.push();
		RENDERER.scale(1, 1, size.z);
		ClientPropCache.polyz.render();
		RENDERER.pop();
		//
		RENDERER.translate(size.x, 0, 0);
		//
		RENDERER.push();
		RENDERER.scale(1, 1, size.z);
		ClientPropCache.polyz.render();
		RENDERER.pop();
		//
		RENDERER.translate(-size.x, 0, size.z);
 		//
		RENDERER.push();
		RENDERER.scale(size.x, 1, 1);
		ClientPropCache.polyx.render();
		RENDERER.pop();
 		//
		RENDERER.pop();
	}

}
