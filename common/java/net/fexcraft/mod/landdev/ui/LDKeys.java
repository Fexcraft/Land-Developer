package net.fexcraft.mod.landdev.ui;

import net.fexcraft.mod.uni.ui.UIKey;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class LDKeys {

	public static final int MAIN = -1;
	public static final int PROPERTY = 0;
	public static final int CHUNK = 1;
	public static final int COMPANY = 2;
	public static final int DISTRICT = 3;
	public static final int MUNICIPALITY = 4;
	public static final int COUNTY = 5;
	public static final int STATE = 6;
	public static final int PLAYER = 8;
	public static final int POLL = 9;
	public static final int MAILBOX = 10;
	public static final int MAIL = 11;
	public static final int CLAIM = 100;
	public static final int IMG_PREVIEW = 200;

	public static final UIKey KEY_MAIN = new UIKey(MAIN, "landdev:main");
	public static final UIKey KEY_PROPERTY = new UIKey(PROPERTY, "landdev:property");
	public static final UIKey KEY_CHUNK = new UIKey(CHUNK, "landdev:chunk");
	public static final UIKey KEY_COMPANY = new UIKey(COMPANY, "landdev:company");
	public static final UIKey KEY_DISTRICT = new UIKey(DISTRICT, "landdev:district");
	public static final UIKey KEY_MUNICIPALITY = new UIKey(MUNICIPALITY, "landdev:municipality");
	public static final UIKey KEY_COUNTY = new UIKey(COUNTY, "landdev:county");
	public static final UIKey KEY_STATE = new UIKey(STATE, "landdev:state");
	public static final UIKey KEY_PLAYER = new UIKey(PLAYER, "landdev:player");
	public static final UIKey KEY_POLL = new UIKey(POLL, "landdev:poll");
	public static final UIKey KEY_MAILBOX = new UIKey(MAILBOX, "landdev:mailbox");
	public static final UIKey KEY_MAIL = new UIKey(MAIL, "landdev:mail");
	public static final UIKey KEY_CLAIM = new UIKey(CLAIM, "landdev:claim");
	public static final UIKey KEY_IMG_VIEW = new UIKey(IMG_PREVIEW, "landdev:img_view");

	public static final HashMap<UIKey, Class<? extends BaseCon>> CONS = new LinkedHashMap<>();
	static{
		CONS.put(KEY_MAIN, BaseCon.class);
		CONS.put(KEY_PROPERTY, BaseCon.PropBaseCon.class);
		CONS.put(KEY_CHUNK, BaseCon.ChunkBaseCon.class);
		CONS.put(KEY_DISTRICT, BaseCon.DisBaseCon.class);
		CONS.put(KEY_MUNICIPALITY, BaseCon.MunBaseCon.class);
		CONS.put(KEY_COUNTY, BaseCon.CouBaseCon.class);
		CONS.put(KEY_STATE, BaseCon.StaBaseCon.class);
		CONS.put(KEY_PLAYER, BaseCon.PlayerBaseCon.class);
		CONS.put(KEY_POLL, BaseCon.PollBaseCon.class);
		//CONS.put(KEY_MAILBOX, BaseCon.MBBaseCon.class);
		CONS.put(KEY_MAIL, BaseCon.MailBaseCon.class);
	}

}
