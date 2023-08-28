package net.fexcraft.mod.landdev.data.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.gui.modules.LDGuiModule;
import net.fexcraft.mod.landdev.gui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.gui.modules.ModuleResponse;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ExternalData<L extends Layer> implements Saveable, LDGuiModule {

	public static final HashMap<Layers, ArrayList<Class<? extends Saveable>>> REGISTRY = new HashMap<>();
	static{
		for(Layers layer : Layers.values()) REGISTRY.put(layer, new ArrayList<>());
	}
	public List<Saveable> saveables = new ArrayList<>();
	public List<LDGuiSubModule> modules = new ArrayList<>();
	private L layer;

	public ExternalData(Layer lay){
		layer = (L)lay;
		for(Class<? extends Saveable> clazz : REGISTRY.get(lay.getLayer())){
			try{
				Saveable save = clazz.newInstance();
				saveables.add(save);
				if(save instanceof LDGuiSubModule) modules.add((LDGuiSubModule)save);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public void save(JsonMap map){
		JsonMap exdata = new JsonMap();
		for(Saveable saveable : saveables){
			saveable.save(exdata);
		}
		if(!exdata.value.isEmpty()) map.add("external", map);
	}

	@Override
	public void load(JsonMap map){
		if(!map.has("external")) return;
		JsonMap exdata = map.getMap("external");
		for(Saveable saveable : saveables){
			saveable.load(exdata);
		}
	}

	@Override
	public void gendef(){
		for(Saveable saveable : saveables){
			saveable.gendef();
		}
	}

	/**
	 * Does get called only if any default behaviour didn't return true already.
	 */
	@Override
	public void sync_packet(LDGuiContainer container, ModuleResponse resp){
		for(LDGuiSubModule module : modules) if(module.sync_packet(container, resp)) return ;
	}

	/**
	 * Does get called only if any default behaviour didn't return true already.
	 */
	@Override
	public void on_interact(LDGuiContainer container, ModuleRequest req){
		for(LDGuiSubModule module : modules) if(module.on_interact(container, req)) return;
	}

}
