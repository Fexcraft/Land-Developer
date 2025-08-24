package net.fexcraft.mod.landdev.data.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.ui.BaseCon;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.modules.ModuleRequest;
import net.fexcraft.mod.landdev.ui.modules.ModuleResponse;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class ExternalData implements ExternalSaveable, LDUIModule {

	public static final HashMap<Layers, ArrayList<Class<? extends ExternalSaveable>>> REGISTRY = new HashMap<>();
	static{ for(Layers layer : Layers.values()) REGISTRY.put(layer, new ArrayList<>()); }
	public List<ExternalSaveable> saveables = new ArrayList<>();
	public List<LDUISubModule> modules = new ArrayList<>();
	public final LDUIModule module;

	public ExternalData(LDUIModule module){
		this.module = module;
		setup((Layer)module);
	}

	@Override
	public void save(Layer layer, JsonMap map){
		JsonMap exdata = new JsonMap();
		for(ExternalSaveable saveable : saveables){
			saveable.save(layer, exdata);
		}
		if(!exdata.value.isEmpty()) map.add("external", exdata);
	}

	@Override
	public void load(Layer layer, JsonMap map){
		if(!map.has("external")) return;
		JsonMap exdata = map.getMap("external");
		for(ExternalSaveable saveable : saveables){
			saveable.load(layer, exdata);
		}
	}

	@Override
	public String id(){
		return "root";
	}

	@Override
	public void setup(Layer layer){
		for(Class<? extends ExternalSaveable> clazz : REGISTRY.get(layer.getLayer())){
			try{
				ExternalSaveable save = clazz.newInstance();
				saveables.add(save);
				if(save instanceof LDUISubModule) modules.add((LDUISubModule)save);
				save.setup((Layer)module);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public void gendef(Layer layer){
		for(ExternalSaveable saveable : saveables){
			saveable.gendef(layer);
		}
	}

	/**
	 * Does get called only if any default behaviour didn't return true already.
	 */
	@Override
	public void sync_packet(BaseCon container, ModuleResponse resp){
		for(LDUISubModule submod : modules) if(submod.sync_packet(module, container, resp)) return ;
	}

	/**
	 * Does get called only if any default behaviour didn't return true already.
	 */
	@Override
	public void on_interact(BaseCon container, ModuleRequest req){
		for(LDUISubModule submod : modules) if(submod.on_interact(module, container, req)) return;
	}

	public ExternalSaveable get(String id){
		for(ExternalSaveable save : saveables) if(save.id().equals(id)) return save;
		return null;
	}

	public <ES> ES getCasted(String id){
		for(ExternalSaveable save : saveables) if(save.id().equals(id)) return (ES)save;
		return null;
	}

}
