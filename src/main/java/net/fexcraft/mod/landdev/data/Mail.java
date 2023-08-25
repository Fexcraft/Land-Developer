package net.fexcraft.mod.landdev.data;

import java.util.ArrayList;
import java.util.UUID;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Mail implements Saveable {

	public boolean unread;
	public Layers from;
	public String fromid;
	public String receiver;
	public ArrayList<String> message = new ArrayList<>();
	public MailType type;
	public long expiry;

	@Override
	public void save(JsonMap map){
		map.add("read", !unread);
		map.add("from", from.name());
		map.add("from_id", fromid);
		map.add("receiver", receiver);
		map.add("message", new JsonArray(message.toArray()));
		map.add("type", type.name());
		map.add("expiry", expiry);
	}

	@Override
	public void load(JsonMap map){
		unread = !map.getBoolean("read", false);
		from = Layers.get(map.getString("from", Layers.NONE.name()));
		fromid = map.getString("from_id", "System");
		receiver = map.get("receiver", "error/unknown");
		message = map.getArray("message").toStringList();
		type = MailType.valueOf(map.getString("type", "EXPIRED"));
		expiry = map.getLong("expiry", 0);
	}

	public int fromInt(){
		return Integer.parseInt(fromid);
	}

	public UUID fromUUID(){
		return UUID.fromString(fromid);
	}

	public boolean expired(){
		return Time.getDate() > 0;
	}

}
