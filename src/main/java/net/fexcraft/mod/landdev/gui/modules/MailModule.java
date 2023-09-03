package net.fexcraft.mod.landdev.gui.modules;

import static net.fexcraft.mod.landdev.data.PermAction.MAIL_DELETE;
import static net.fexcraft.mod.landdev.data.PermAction.MAIL_READ;
import static net.fexcraft.mod.landdev.data.PermAction.MUNICIPALITY_STAFF;
import static net.fexcraft.mod.landdev.gui.LDGuiElementType.*;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translate;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.utils.Print;
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
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.gui.LDGuiContainer;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class MailModule implements LDGuiModule {
	
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
				resp.addButton("request.timeout", ELM_YELLOW, ICON_REM, Settings.REQUEST_TIMEOUT_DAYS);
			}
		}
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
									Print.chat(player.entity, translate("mail.municipality.staff.notmember"));
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
							}
							else{
								if(player.isMunicipalityManager()){
									Print.chat(player.entity, translate("mail.municipality.citizen.ismanager"));
									return;
								}
								if(player.isCountyManager() && mun.county.id != player.county.id){
									Print.chat(player.entity, translate("mail.county.citizen.ismanager"));
									return;
								}
								player.setCitizenOf(mun);
								//TODO announce
							}
						}
						else if(mail.receiver.is(Layers.MUNICIPALITY)){
							//invites into a county
						}
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
				mail.expiry(Time.getDate());
				container.open(GuiHandler.MAILBOX, container.x, container.y, container.z);
				return;
			}
			case "request.accept":{
				if(mail.type != MailType.REQUEST) return;
				return;
			}
			case "request.reject":{
				if(mail.type != MailType.REQUEST) return;

				return;
			}
			case "request.timeout":{
				if(mail.type != MailType.REQUEST) return;

				return;
			}
		}
	}

	public static MailData getMailbox(Player player, int x, int y){
		Layers lay = Layers.values()[x];
		switch(lay){
			case PLAYER: return player.mail;
			case DISTRICT: {
				District dis = ResManager.getDistrict(y, false);
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
				District dis = ResManager.getDistrict(y, false);
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
