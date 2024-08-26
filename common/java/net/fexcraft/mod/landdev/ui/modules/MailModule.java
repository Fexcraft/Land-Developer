package net.fexcraft.mod.landdev.ui.modules;

import static net.fexcraft.mod.landdev.data.PermAction.*;
import static net.fexcraft.mod.landdev.ui.LDUIElmType.*;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.Layers;
import net.fexcraft.mod.landdev.data.Mail;
import net.fexcraft.mod.landdev.data.MailData;
import net.fexcraft.mod.landdev.data.MailType;
import net.fexcraft.mod.landdev.data.Manageable.Staff;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.district.District;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.ui.LDUIModule;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.LDConfig;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class MailModule implements LDUIModule {
	
	public static MailModule INST = new MailModule();

	@Override
	public void sync_packet(LDGuiContainer container, ModuleResponse resp){
		resp.setTitle("mail.title");
		resp.setNoBack();
		MailData mailbox = getMailbox(container.player, container.x, container.y);
		if(mailbox == null){
			resp.addRow("boxnotfound", ELM_GENERIC);
			return;
		}
		Mail mail = container.z >= mailbox.mails.size() ? null : mailbox.mails.get(container.z);
		if(mail == null){
			resp.addRow("notfound", ELM_GENERIC);
			return;
		}
		resp.addRow("type", ELM_BLUE, mail.type.name().toLowerCase());
		resp.addRow("from", ELM_BLUE, mail.from.name().toLowerCase() + " > " + mail.fromid);
		//resp.addRow("to", ELM_BLUE, mail.receiver);
		resp.addRow("at", ELM_BLUE, Time.getAsString(mail.sent));
		resp.addBlank();
		resp.addRow("title", ELM_GREEN, resp.val(mail.title));
		for(int i = 0; i < mail.message.size(); i++){
			resp.addRow("msg" + i, ELM_GENERIC, resp.val(mail.message.get(i)));
		}
		if(mail.invite()){
			if(mail.expired() || mail.type == MailType.EXPIRED){
				resp.addRow("expired", ELM_RED);
			}
			else{
				resp.addButton("invite.accept", ELM_GREEN, ICON_ADD);
				resp.addButton("invite.reject", ELM_RED, ICON_REM);
			}
		}
		if(mail.request()){
			if(mail.expired() || mail.type == MailType.EXPIRED){
				resp.addRow("expired", ELM_RED);
			}
			else{
				resp.addButton("request.accept", ELM_GREEN, ICON_ADD);
				resp.addButton("request.reject", ELM_RED, ICON_REM);
				resp.addButton("request.timeout", ELM_YELLOW, ICON_REM, LDConfig.REQUEST_TIMEOUT_DAYS);
			}
		}
		resp.addButton("goback", ELM_GENERIC, ICON_LIST);
	}

	public void on_interact(LDGuiContainer container, ModuleRequest req){
		MailData mailbox = getMailbox(container.player, container.x, container.y);
		if(mailbox == null) return;
		Player player = container.player;
		Mail mail = container.z >= mailbox.mails.size() ? null : mailbox.mails.get(container.z);
		switch(req.event()){
			case "invite.accept":{
				if(mail.type != MailType.INVITE) return;
				switch(mail.from){
					case MUNICIPALITY:{
						if(mail.receiver.is(Layers.PLAYER)){
							player = ResManager.getPlayer(mail.recUUID(), true);
							Municipality mun = ResManager.getMunicipality(mail.fromInt(), true);
							if(mail.staff){
								if(!mun.citizens.isCitizen(player.uuid)){
									player.entity.send(translate("mail.municipality.staff.notmember"));
									return;
								}
								mun.manage.staff.add(new Staff(player.uuid, MUNICIPALITY_STAFF));
								mun.save();
								String pln = ResManager.getPlayerName(player.uuid);
								for(Staff stf : mun.manage.staff){
									Player stp = ResManager.getPlayer(stf.uuid, true);
									mail = new Mail(MailType.SYSTEM, Layers.MUNICIPALITY, mun.id, Layers.PLAYER, stp.uuid).expireInDays(7);
									mail.setTitle(mun.name()).addMessage(translate("mail.municipality.staff.added", pln));
									stp.addMailAndSave(mail);
								}
								mail.expire();
							}
							else{
								if(player.isMunicipalityManager()){
									player.entity.send(translate("mail.municipality.citizen.ismanager"));
									return;
								}
								if(player.isCountyManager() && mun.county.id != player.county.id){
									player.entity.send(translate("mail.county.citizen.ismanager"));
									return;
								}
								player.setCitizenOf(mun);
								mail.expire();
								//TODO announce
							}
						}
						else if(mail.receiver.is(Layers.MUNICIPALITY)){
							//invites into a county
						}
						goback(container);
						return;
					}
					case COUNTY:{

						return;
					}
					case STATE:{

						return;
					}
					case COMPANY:{
						//
						return;
					}
				}
				return;
			}
			case "invite.reject":{
				if(mail.type != MailType.INVITE) return;
				mail.expire();
				goback(container);
				return;
			}
			case "request.accept":{
				if(mail.type != MailType.REQUEST) return;
				switch(mail.from){
					case PLAYER:{
						if(mail.receiver.is(Layers.MUNICIPALITY)){
							player = ResManager.getPlayer(mail.fromUUID(), true);
							Municipality mun = ResManager.getMunicipality(mail.recInt(), true);
							if(player.isMunicipalityManager()){
								player.entity.send(translate("mail.municipality.request.ismanager"));
								return;
							}
							if(player.isCountyManager() && mun.county.id != player.county.id){
								player.entity.send(translate("mail.county.request.ismanager"));
								return;
							}
							player.setCitizenOf(mun);
							mail.expire();
							//TODO announce
						}
						else if(mail.receiver.is(Layers.COUNTY)){
							//
						}
						else if(mail.receiver.is(Layers.COMPANY)){
							//
						}
						goback(container);
						return;
					}
				}
				return;
			}
			case "request.reject":
			case "request.timeout":{
				if(mail.type != MailType.REQUEST) return;
				int mul = req.event().endsWith("timeout") ? LDConfig.REQUEST_TIMEOUT_DAYS : 1;
				switch(mail.from){
					case PLAYER:{
						if(mail.receiver.is(Layers.MUNICIPALITY)){
							player = ResManager.getPlayer(mail.fromUUID(), true);
							Municipality mun = ResManager.getMunicipality(mail.recInt(), true);
							mun.requests.timeouts.put(player.uuid, Time.getDate() + Time.DAY_MS * mul);
							mail.expire();
							goback(container);
						}
						return;
					}
				}
				return;
			}
			case "goback":{
				goback(container);
				return;
			}
		}
	}

	private void goback(LDGuiContainer container){
		container.open(LDKeys.MAILBOX, container.x, container.y, container.z);
	}

	public static MailData getMailbox(Player player, int x, int y){
		Layers lay = Layers.values()[x];
		switch(lay){
			case PLAYER: return player.mail;
			case DISTRICT: {
				District dis = ResManager.getDistrict(y);
				if(player.adm || dis.can(MAIL_READ, player.uuid)){
					return dis.mail;
				}
				break;
			}
			case MUNICIPALITY:{
				Municipality mun = ResManager.getMunicipality(y, false);
				if(player.adm || mun.manage.can(MAIL_READ, player.uuid)){
					return mun.mail;
				}
				break;
			}
			case COUNTY:{
				County con = ResManager.getCounty(y, false);
				if(player.adm || con.manage.can(MAIL_READ, player.uuid)){
					return con.mail;
				}
				break;
			}
			case STATE:{
				State st = ResManager.getState(y, false);
				if(player.adm || st.manage.can(MAIL_READ, player.uuid)){
					return st.mail;
				}
				break;
			}
		}
		return null;
	}

	public static boolean canDelete(Player player, int x, int y){
		Layers lay = Layers.values()[x];
		switch(lay){
			case PLAYER: return true;
			case DISTRICT: {
				District dis = ResManager.getDistrict(y);
				if(player.adm || dis.can(MAIL_DELETE, player.uuid)) return true;
				break;
			}
			case MUNICIPALITY:{
				Municipality mun = ResManager.getMunicipality(y, false);
				if(player.adm || mun.manage.can(MAIL_DELETE, player.uuid)) return true;
				break;
			}
			case COUNTY:{
				County con = ResManager.getCounty(y, false);
				if(player.adm || con.manage.can(MAIL_DELETE, player.uuid)) return true;
				break;
			}
			case STATE:{
				State st = ResManager.getState(y, false);
				if(player.adm || st.manage.can(MAIL_DELETE, player.uuid)) return true;
				break;
			}
		}
		return false;
	}

}
