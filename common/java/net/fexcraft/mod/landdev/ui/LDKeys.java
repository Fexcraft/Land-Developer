package net.fexcraft.mod.landdev.ui;

import net.fexcraft.mod.uni.ui.UIKey;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class LDKeys {

	public static final int ID_MAIN = -1;
	public static final int ID_PROPERTY = 0;
	public static final int ID_CHUNK = 1;
	public static final int ID_COMPANY = 2;
	public static final int ID_DISTRICT = 3;
	public static final int ID_MUNICIPALITY = 4;
	public static final int ID_COUNTY = 5;
	public static final int ID_STATE = 6;
	public static final int ID_PLAYER = 8;
	public static final int ID_POLL = 9;
	public static final int ID_MAILBOX = 10;
	public static final int ID_MAIL = 11;
	public static final int ID_CHUNK_CLAIM = 100;
	public static final int ID_CHUNK_TRANSFER = 101;
	public static final int ID_CHUNK_SELL = 102;
	public static final int ID_CHUNK_BUY = 103;
	public static final int ID_IMG_PREVIEW = 200;

	public static final UIKey MAIN = new UIKey(ID_MAIN, "landdev:main");
	public static final UIKey PROPERTY = new UIKey(ID_PROPERTY, "landdev:property");
	public static final UIKey CHUNK = new UIKey(ID_CHUNK, "landdev:chunk");
	public static final UIKey COMPANY = new UIKey(ID_COMPANY, "landdev:company");
	public static final UIKey DISTRICT = new UIKey(ID_DISTRICT, "landdev:district");
	public static final UIKey MUNICIPALITY = new UIKey(ID_MUNICIPALITY, "landdev:municipality");
	public static final UIKey COUNTY = new UIKey(ID_COUNTY, "landdev:county");
	public static final UIKey STATE = new UIKey(ID_STATE, "landdev:state");
	public static final UIKey PLAYER = new UIKey(ID_PLAYER, "landdev:player");
	public static final UIKey POLL = new UIKey(ID_POLL, "landdev:poll");
	public static final UIKey MAILBOX = new UIKey(ID_MAILBOX, "landdev:mailbox");
	public static final UIKey MAIL = new UIKey(ID_MAIL, "landdev:mail");
	public static final UIKey CHUNK_CLAIM = new UIKey(ID_CHUNK_CLAIM, "landdev:chunk_claim");
	public static final UIKey CHUNK_TRANSFER = new UIKey(ID_CHUNK_TRANSFER, "landdev:chunk_transfer");
	public static final UIKey CHUNK_SELL = new UIKey(ID_CHUNK_SELL, "landdev:chunk_sell");
	public static final UIKey CHUNK_BUY = new UIKey(ID_CHUNK_BUY, "landdev:chunk_buy");
	public static final UIKey IMG_VIEW = new UIKey(ID_IMG_PREVIEW, "landdev:img_view");

	public static final HashMap<UIKey, Class<? extends BaseCon>> CONS = new LinkedHashMap<>();
	static{
		CONS.put(MAIN, BaseCon.class);
		CONS.put(PROPERTY, BaseCon.PropBaseCon.class);
		CONS.put(CHUNK, BaseCon.ChunkBaseCon.class);
		CONS.put(DISTRICT, BaseCon.DisBaseCon.class);
		CONS.put(MUNICIPALITY, BaseCon.MunBaseCon.class);
		CONS.put(COUNTY, BaseCon.CouBaseCon.class);
		CONS.put(STATE, BaseCon.StaBaseCon.class);
		CONS.put(PLAYER, BaseCon.PlayerBaseCon.class);
		CONS.put(POLL, BaseCon.PollBaseCon.class);
		//CONS.put(KEY_MAILBOX, BaseCon.MBBaseCon.class);
		CONS.put(MAIL, BaseCon.MailBaseCon.class);
	}

}
