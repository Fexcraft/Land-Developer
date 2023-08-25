package net.fexcraft.mod.landdev.data;

import java.util.ArrayList;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.mod.landdev.util.ResManager;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class MailData implements Saveable {

	public ArrayList<Mail> mails = new ArrayList<>();
	private String id;

	public MailData(Layers lay, Object name){
		id = lay.name() + "/" + name;
	}

	@Override
	public void load(JsonMap map){
		if(map.has("mailbox")){
			//marker detected, calling separate load instead
			ResManager.load(this);
			return;
		}
		if(map.has("mails")){
			for(JsonValue<?> val : map.getArray("mails").value){
				Mail mail = new Mail();
				mail.load(val.asMap());
				mails.add(mail);
			}
		}
	}

	@Override
	public void save(JsonMap map){
		if(!map.empty()){
			//leaving a marker for load call and saving in separate table instead
			map.add("mailbox", id);
			save();
		}
		else{
			map.add("id", id);
			JsonArray array = new JsonArray();
			for(Mail mail : mails){
				JsonMap json = new JsonMap();
				mail.save(json);
				array.add(json);
			}
			map.add("mails", array);
		}
	}

	public int unread(){
		int unread = 0;
		for(Mail mail : mails) if(mail.unread) unread++;
		return unread;
	}

	@Override
	public String saveId(){
		return id;
	}

	@Override
	public String saveTable(){
		return "mails";
	}

	@Override
	public void gendef(){
		//
	}

}
