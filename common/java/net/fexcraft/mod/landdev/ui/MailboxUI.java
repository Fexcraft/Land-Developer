package net.fexcraft.mod.landdev.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.mod.landdev.data.MailType;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.*;

import java.util.List;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class MailboxUI extends UserInterface {

	protected MailboxCon mon;
	protected int page;

	public MailboxUI(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		mon = (MailboxCon)container;
	}

	@Override
	public void init(){
		TagCW com = TagCW.create();
		com.set("sync", true);
		container.SEND_TO_SERVER.accept(com);
		page(0);
	}

	@Override
	public void predraw(float ticks, int mx, int my){
		if(mon.mailbox == null) return;
		for(int i = 0; i < 10; i++){
			boolean bool = i + page * 10 < mon.mailbox.mails.size();
			buttons.get("open_" + i).visible(bool);
			buttons.get("del_" + i).visible(bool);
			texts.get("mail_" + i).visible(bool);
			if(bool) texts.get("mail_" + i).value(mon.mailbox.mails.get(i).title);
		}
	}

	@Override
	public void drawbackground(float ticks, int mx, int my){
		if(mon.mailbox == null) return;
		for(int i = 0; i < 10; i++){
			int j = i + page * 10;
			if(j >= mon.mailbox.mails.size()) break;
			MailType type = mon.mailbox.mails.get(j).type;
			if(type == MailType.INVITE && mon.mailbox.mails.get(j).expired()) type = MailType.EXPIRED;
			if(mon.mailbox.mails.get(j).unread){
				drawer.draw(gLeft + 6, gTop + 21 + i * 14, type.u_unread, type.v_unread, 16, 10);
			}
			else{
				drawer.draw(gLeft + 6, gTop + 21 + i * 14, type.u_read, type.v_read, 16, 10);
			}
		}
	}

	@Override
	public boolean onAction(UIButton button, String id, int x, int y, int b){
		if(id.startsWith("open")){
			int idx = Integer.parseInt(id.substring(5));
			TagCW com = TagCW.create();
			com.set("read", idx + page * 10);
			container.SEND_TO_SERVER.accept(com);
			return true;
		}
		if(id.startsWith("del")){
			int idx = Integer.parseInt(id.substring(4));
			TagCW com = TagCW.create();
			com.set("delete", idx + page * 10);
			container.SEND_TO_SERVER.accept(com);
			return true;
		}
		switch(id){
			case "prev":{
				page(-1);
				return true;
			}
			case "next":{
				page(1);
				return true;
			}
		}
		return false;
	}

	private void page(int i){
		page += i;
		if(page < 0) page = 0;
		texts.get("title").value("landdev.gui.mailbox.title");
		texts.get("title").translate(page + 1);
	}

	@Override
	public boolean onScroll(UIButton button, String id, int mx, int my, int am){
		page(am > 0 ? 1 : -1);
		return true;
	}

	@Override
	public void getTooltip(int mx, int my, List<String> list){
		if(mon.mailbox == null) return;
		if(buttons.get("prev").hovered()) list.add("landdev.gui.mailbox.prev");
		if(buttons.get("next").hovered()) list.add("landdev.gui.mailbox.next");
		for(int i = 0; i < 10; i++){
			if(i + page * 10 >= mon.mailbox.mails.size()) break;
			if(buttons.get("open_" + i).hovered()) list.add("landdev.gui.mailbox.read");
			if(buttons.get("del_" + i).hovered()) list.add("landdev.gui.mailbox.delete");
			if(texts.get("mail_" + 1).hovered()) list.add(texts.get("mail_" + i).value());
		}
	}

}
