package net.fexcraft.mod.landdev.data;

import java.util.ArrayList;
import java.util.UUID;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class Mail implements Saveable {

	public boolean unread;
	public Layers from;
	public String fromid;
	public Layers receiver;
	public String recid;
	public String title;
	public ArrayList<String> message = new ArrayList<>();
	public MailType type;
	public long expiry;
	public long sent;
	public boolean staff;

	public Mail(){
		sent = Time.getDate();
		unread = true;
		type = MailType.SYSTEM;
	}

	public Mail(MailType mtype, Layers frl, Object id, Layers rec, Object rec_id){
		sent = Time.getDate();
		type = mtype;
		from = frl;
		fromid = id.toString();
		receiver = rec;
		recid = rec_id.toString();
		unread = true;
	}

	public Mail(MailType mtype, Chunk_ chunk){
		sent = Time.getDate();
		type = mtype;
		from = chunk.district.owner.layer();
		fromid = chunk.district.owner.owid + "";
		receiver = chunk.owner.playerchunk ? Layers.PLAYER : chunk.owner.layer();
		recid = chunk.owner.playerchunk ? chunk.owner.player.toString() : chunk.owner.owid + "";
		unread = true;
	}

	public Mail(MailType mtype, Layers lay, Object id){
		sent = Time.getDate();
		type = mtype;
		from = lay;
		fromid = id.toString();
		receiver = lay;
		recid = id.toString();
		unread = true;
	}

	public Mail expiry(long time){
		expiry = Time.getDate() + time;
		return this;
	}

	public Mail expireInDays(int days){
		expiry = Time.getDate() + Time.DAY_MS * days;
		return this;
	}

	public Mail setTitle(String ntitle){
		title = ntitle;
		return this;
	}

	public Mail addMessage(String msg){
		message.add(msg);
		return this;
	}

	public Mail setStaffInvite(){
		staff = true;
		return this;
	}

	@Override
	public void save(JsonMap map){
		map.add("read", !unread);
		map.add("from", from.name());
		map.add("from_id", fromid);
		map.add("receiver", receiver.name());
		map.add("rec_id", recid);
		map.add("title", title);
		map.add("message", new JsonArray(message.toArray()));
		map.add("type", type.name());
		map.add("sent", sent);
		if(expiry > 0) map.add("expiry", expiry);
		if(staff) map.add("staffinv", staff);
	}

	@Override
	public void load(JsonMap map){
		unread = !map.getBoolean("read", false);
		from = Layers.get(map.getString("from", Layers.NONE.name()));
		fromid = map.getString("from_id", "ERROR");
		receiver = Layers.get(map.getString("receiver", Layers.NONE.name()));
		recid = map.getString("rec_id", "ERROR");
		title = map.getString("title", "No Title");
		message = map.getArray("message").toStringList();
		type = MailType.get(map.getString("type", "EXPIRED"));
		expiry = map.getLong("expiry", 0);
		staff = map.getBoolean("staffinv", false);
		sent = map.getLong("sent", 0);
	}

	public int fromInt(){
		return Integer.parseInt(fromid);
	}

	public UUID fromUUID(){
		return UUID.fromString(fromid);
	}

	public int recInt(){
		return Integer.parseInt(recid);
	}

	public UUID recUUID(){
		return UUID.fromString(recid);
	}

	public void expire(){
		expiry = Time.getDate();
	}

	public boolean expired(){
		return expiry > 0 && Time.getDate() > expiry;
	}

	public boolean invite(){
		return type == MailType.INVITE;
	}

	public boolean request(){
		return type == MailType.REQUEST;
	}

}
