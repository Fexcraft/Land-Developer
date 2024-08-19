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
public class ExternalData implements Saveable, LDGuiModule {

	public static final HashMap<Layers, ArrayList<Class<? extends ExternalSaveable>>> REGISTRY = new HashMap<>();
	static{ for(Layers layer : Layers.values()) REGISTRY.put(layer, new ArrayList<>()); }
	public List<ExternalSaveable> saveables = new ArrayList<>();
	public List<LDGuiSubModule> modules = new ArrayList<>();
	public final LDGuiModule module;

	public ExternalData(LDGuiModule module){
		this.module = module;
		for(Class<? extends ExternalSaveable> clazz : REGISTRY.get(((Layer)module).getLayer())){
			try{
				ExternalSaveable save = clazz.newInstance();
				saveables.add(save);
				if(save instanceof LDGuiSubModule) modules.add((LDGuiSubModule)save);
				save.setup((Layer)module);
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
		for(LDGuiSubModule submod : modules) if(submod.sync_packet(module, container, resp)) return ;
	}

	/**
	 * Does get called only if any default behaviour didn't return true already.
	 */
	@Override
	public void on_interact(LDGuiContainer container, ModuleRequest req){
		for(LDGuiSubModule submod : modules) if(submod.on_interact(module, container, req)) return;
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
