package net.fexcraft.mod.landdev.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class LDEvent {

	private static HashMap<Class<? extends LDEvent>, ArrayList<Consumer<LDEvent>>> listeners = new HashMap<>();

	public static <FE extends LDEvent> void addListener(Class<FE> clazz, Consumer<FE> cons){
		if(!listeners.containsKey(clazz)) listeners.put(clazz, new ArrayList<>());
		listeners.get(clazz).add((Consumer<LDEvent>)cons);
	}

	public static void run(LDEvent event){
		ArrayList<Consumer<LDEvent>> list = listeners.get(event.getClass());
		if(list == null) return;
		for(Consumer<LDEvent> cons : list){
			try{
				cons.accept(event);
			}
			catch(Throwable e){
				e.printStackTrace();
			}
		}
	}

}
