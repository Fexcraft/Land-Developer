package net.fexcraft.mod.landdev.data.prop;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.data.Account;
import net.fexcraft.mod.fsmm.data.Bank;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Mail;
import net.fexcraft.mod.landdev.data.MailType;
import net.fexcraft.mod.landdev.data.Saveable;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.util.ResManager;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class PropRent implements Saveable {

	public boolean rentable;
	public boolean renewable;
	public boolean autorenew;
	public PropOwner renter = new PropOwner();
	public long duration = Time.DAY_MS * 7;
	public long amount = 10000;
	public long until;

	@Override
	public void save(JsonMap map){
		map.add("rentable", rentable);
		map.add("duration", duration);
		map.add("renewable", renewable);
		map.add("amount", amount);
		if(!renter.unowned){
			JsonMap r = new JsonMap();
			renter.save(r);
			r.add("autorenew", autorenew);
			r.add("until", until);
			map.add("rented", r);
		}
	}

	@Override
	public void load(JsonMap map){
		rentable = map.getBoolean("rentable", rentable);
		renewable = map.getBoolean("renewable", renewable);
		duration = map.getLong("duration", duration);
		amount = map.getLong("amount", amount);
		if(map.has("rented")){
			JsonMap r = map.getMap("rented");
			renter.load(r);
			until = r.get("until", 0l);
			autorenew = r.getBoolean("autorenew", autorenew);
		}
	}

	public void checkRentStatus(Property prop, long time){
		if(renter.unowned) return;
		if(until > time) return;
		boolean cancel = !rentable || !renewable;
		if(rentable && renewable){
			if(!autorenew) cancel = true;
			else{
				collect(prop, ResManager.getChunk(prop.start));
				cancel = false;
			}
		}
		if(cancel){
			Mail mail = new Mail(MailType.SYSTEM, Layers.PROPERTY, prop.id);
			mail.setTitle("landdev.mail.property.rent");
			if(prop.label.present) mail.addMessage(prop.label.label);
			mail.addMessage("landdev.mail.property.rent_ended");
			mail.expireInDays(7);
			renter.addMail(mail);
			renter.set(null, -1);
			until = 0;
			prop.save();
		}
	}

	public void collect(Property prop, Chunk_ ck){
		if(until > Time.getDate()) return;
		Account oacc = prop.owner.getAccount(ck);
		Account racc = renter.getAccount(ck);
		if(!oacc.getBank().processAction(Bank.Action.TRANSFER, null, racc, amount, oacc)){
			Mail mail = new Mail(MailType.SYSTEM, Layers.PROPERTY, prop.id);
			mail.setTitle("landdev.mail.property.rent_collection");
			if(prop.label.present) mail.addMessage(prop.label.label);
			mail.addMessage("landdev.mail.property.rent_collection_failed");
			mail.expireInDays(7);
			renter.addMail(mail);
			renter.set(null, -1);
			until = 0;
		}
		else{
			Mail mail = new Mail(MailType.SYSTEM, Layers.PROPERTY, prop.id);
			mail.setTitle("landdev.mail.property.rent_collection");
			if(prop.label.present) mail.addMessage(prop.label.label);
			mail.addMessage("landdev.mail.property.rent_collection_success");
			mail.expireInDays(7);
			renter.addMail(mail);
			until = Time.getDate() + duration;
		}
		prop.save();
	}

	public String duration_string(){
		return toDHM(duration);
	}

	public String until_string(){
		return toDHM(Time.getDate() - until);
	}

	public static String toDHM(long dura){
		long dur = dura - (dura % Time.MIN_MS);
		long min = dur % Time.HOUR_MS / Time.MIN_MS;
		long hor = dur % Time.DAY_MS / Time.HOUR_MS;
		long day = dur / Time.DAY_MS;
		if(min < 0) min = -min;
		if(hor < 0) hor = -hor;
		if(day < 0) day = -day;
		return day + "d, " + hor + "h, " + min + "m";
	}

	public static long parse_duration(String dur){
		String[] str = dur.split(",");
		String e;
		long res = 0;
		for(String s : str){
			s = s.trim();
			e = s.substring(s.length() -1);
			s = s.substring(0, s.length() - 1).trim();
			switch(e){
				case "d":
					res += Time.DAY_MS * Long.parseLong(s);
					break;
				case "h":
					res += Time.HOUR_MS * Long.parseLong(s);
					break;
				case "m":
					res += Time.MIN_MS * Long.parseLong(s);
					break;
			}
		}
		if(res < Time.MIN_MS) res = Time.MIN_MS;
		return res;
	}

}
