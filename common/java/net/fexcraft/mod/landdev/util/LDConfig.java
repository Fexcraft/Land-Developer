package net.fexcraft.mod.landdev.util;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.Static;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.landdev.data.IconHolder;
import net.fexcraft.mod.landdev.data.district.DistrictType;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.fexcraft.mod.uni.ConfigBase;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.ui.ContainerInterface;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class LDConfig extends ConfigBase {

	public static String SERVER_ICON = "http://fexcraft.net/files/mod_data/landdev/default_server_icon.png";
	public static String DEFAULT_ICON = "http://fexcraft.net/files/mod_data/landdev/default_icon.png";
	public static final String BROADCASTER = "http://fexcraft.net/files/mod_data/landdev/broadcaster_icon.png";
	public static IconHolder SERVER_ICONHOLDER = new IconHolder(SERVER_ICON);
	public static boolean SAVE_SPACED_JSON;
	public static boolean PROTECT_WILDERNESS;
	public static boolean EDIT_WILDERNESS;
	public static boolean LOCUP_SIDE;
	public static boolean SAVE_CHUNKS_IN_REGIONS;
	public static boolean RUN_LOCATION_EVENT;
	public static long DEFAULT_CHUNK_PRICE;
	public static long COUNTY_CREATION_FEE;
	public static long MUNICIPALITY_CREATION_FEE;
	public static long DISTRICT_CREATION_FEE;
	public static int CHUNK_LINK_LIMIT;
	public static int REQUEST_TIMEOUT_DAYS;
	//
	public static boolean TAX_ENABLED;
	public static boolean TAX_OFFLINE;
	public static long TAX_INTERVAL;
	//
	public static boolean CHAT_OVERRIDE;
	public static boolean DISCORD_BOT_ACTIVE;
	public static String CHAT_PLAYER_COLOR = "&6";
	public static String CHAT_ADMIN_COLOR = "&4";
	public static String CHAT_DISCORD_COLOR = "&9";
	public static String CHAT_OVERRIDE_LANG;
	public static int DISCORD_BOT_PORT = 10810;
	public static String DISCORD_BOT_ADRESS = "fexcraft.net";
	public static String DISCORD_BOT_TOKEN = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
	//
	public static String SERVLANG_STARTED = "Server has started. Running LandDeveloper v%s";
	public static String SERVLANG_STOPPING = "Server is closing.";
	public static String SERVLANG_JOINED = "%s joined the server.";
	public static String SERVLANG_LEFT = "%s left the server.";
	//
	public static final String GENERAL_CAT = "general";
	public static final String DISCORD_CAT = "discord";
	public static final String CLIENT_CAT = "client";
	public static final String TAX_CAT = "tax";
	public static final String CHAT_CAT = "chat";
	public static final String PRICES_CAT = "prices";
	public static final String SERVLANG = "server_lang";
	//
	public static File CONFIG_PATH;

	public LDConfig(File fl){
		super(fl, "Land Developer");
		CONFIG_PATH = file.getParentFile();
		DistrictType.loadConfig(CONFIG_PATH);
	}

	@Override
	protected void fillInfo(JsonMap map){
		map.add("info", "Land-Developer Main Configuration File");
		map.add("wiki", "https://fexcraft.net/wiki/mod/landdev");
	}

	@Override
	protected void fillEntries(){
		entries.add(new ConfigEntry(this, GENERAL_CAT, "server_icon", SERVER_ICON)
			.info("Server Icon to be shown in the Location Update GUI.")
			.cons((con, map) -> {
				SERVER_ICON = con.getString(map);
				SERVER_ICONHOLDER.set(SERVER_ICON);
			})
		);
		entries.add(new ConfigEntry(this, GENERAL_CAT, "default_icon", DEFAULT_ICON)
			.info("Default Dis/Mun/Cou/State Icon to be shown in the Location Update GUI.")
			.cons((con, map) -> {
				SERVER_ICON = con.getString(map);
				SERVER_ICONHOLDER.set(SERVER_ICON);
			})
		);
		entries.add(new ConfigEntry(this, GENERAL_CAT, "save_spaced_json", false)
			.info("If true, the JSON will be formatted to be easily readable, otherwise if false it will not have any spacing, to save on disk and load time.")
			.cons((con, map) -> SAVE_SPACED_JSON = con.getBoolean(map))
		);
		entries.add(new ConfigEntry(this, GENERAL_CAT, "protect_wilderness", true)
			.info("If wilderness protection should be enabled.")
			.cons((con, map) -> EDIT_WILDERNESS = !(PROTECT_WILDERNESS = con.getBoolean(map)))
		);
		entries.add(new ConfigEntry(this, GENERAL_CAT, "chunk_link_limit", 3).rang(0, 64)
			.info("How many chunks can be linked to another. Set to '0' to disable.")
			.cons((con, map) -> CHUNK_LINK_LIMIT = con.getInteger(map))
		);
		entries.add(new ConfigEntry(this, GENERAL_CAT, "request_timeout_days", 7).rang(1, 30)
			.info("Default days value for request timeouts.")
			.cons((con, map) -> REQUEST_TIMEOUT_DAYS = con.getInteger(map))
		);
		entries.add(new ConfigEntry(this, GENERAL_CAT, "save_chunks_in_regions", false)
			.info("Should chunk data be saved in custom/separate region files rather than the chunks themselves? WARNING: Existing chunk data may not port over when changing this setting.")
			.cons((con, map) -> SAVE_CHUNKS_IN_REGIONS = con.getBoolean(map))
		);
		entries.add(new ConfigEntry(this, GENERAL_CAT, "run_location_event", false)
				.info("Should the 'PlayerLocationEvent' be run every time the player enters another district/named-chunk? Use this if you have an LD Addon requiring it.")
				.cons((con, map) -> RUN_LOCATION_EVENT = con.getBoolean(map))
		);
		//
		entries.add(new ConfigEntry(this, PRICES_CAT, "default_chunk", 100000).rang(0, 1000000000)
			.info("Default price for unclaimed chunks. (1000 = 1$)")
			.cons((con, map) -> DEFAULT_CHUNK_PRICE = con.getInteger(map))
		);
		entries.add(new ConfigEntry(this, PRICES_CAT, "create_district", 5000000).rang(0, Integer.MAX_VALUE)
			.info("Server fee for creating a district.")
			.cons((con, map) -> DISTRICT_CREATION_FEE = con.getInteger(map))
		);
		entries.add(new ConfigEntry(this, PRICES_CAT, "create_municipality", 25000000).rang(0, Integer.MAX_VALUE)
			.info("Server fee for creating a municipality, half of it goes to the new Municipality.")
			.cons((con, map) -> MUNICIPALITY_CREATION_FEE = con.getInteger(map))
		);
		entries.add(new ConfigEntry(this, PRICES_CAT, "create_county", 250000000).rang(0, Integer.MAX_VALUE)
			.info("Server fee for creating a county, half of it goes to the new County.")
			.cons((con, map) -> COUNTY_CREATION_FEE = con.getInteger(map))
		);
		//
		entries.add(new ConfigEntry(this, TAX_CAT, "enabled", true)
			.info("If the Tax System should be enabled.")
			.cons((con, map) -> TAX_ENABLED = con.getBoolean(map))
		);
		entries.add(new ConfigEntry(this, TAX_CAT, "interval", 24).rang(1, 168)
			.info("Interval between tax collection cycles, in hours.")
			.cons((con, map) -> TAX_INTERVAL = con.getInteger(map) * Time.HOUR_MS)
		);
		entries.add(new ConfigEntry(this, TAX_CAT, "inactive_players", false)
			.info("If inactive (since more than one interval) players should be taxed as well.")
			.cons((con, map) -> TAX_OFFLINE = con.getBoolean(map))
		);
		//
		entries.add(new ConfigEntry(this, CHAT_CAT, "override", true)
			.info("If the vanilla chat should be overridden by the LandDev chat.")
			.cons((con, map) -> CHAT_OVERRIDE = con.getBoolean(map))
		);
		entries.add(new ConfigEntry(this, CHAT_CAT, "color_player", CHAT_PLAYER_COLOR)
			.info("Default color of the player role marker (default `#`) in chat.")
			.cons((con, map) -> CHAT_PLAYER_COLOR = con.getString(map))
		);
		entries.add(new ConfigEntry(this, CHAT_CAT, "color_admin", CHAT_ADMIN_COLOR)
			.info("Default color of the admin role marker (default `#`) in chat.")
			.cons((con, map) -> CHAT_ADMIN_COLOR = con.getString(map))
		);
		entries.add(new ConfigEntry(this, CHAT_CAT, "color_discord", CHAT_DISCORD_COLOR)
			.info("Default color of the discord role marker (default `#`) in chat.")
			.cons((con, map) -> CHAT_DISCORD_COLOR = con.getString(map))
		);
		//
		entries.add(new ConfigEntry(this, DISCORD_CAT, "active", false)
			.info("If the discord chat integration should be active.")
			.cons((con, map) -> DISCORD_BOT_ACTIVE = con.getBoolean(map))
		);
		entries.add(new ConfigEntry(this, DISCORD_CAT, "adress", DISCORD_BOT_ADRESS)
			.info("Adress/IP of the Discord Chat Bot you are using. By default this points to the one provided by Fexcraft.")
			.cons((con, map) -> DISCORD_BOT_ADRESS = con.getString(map))
		);
		entries.add(new ConfigEntry(this, DISCORD_CAT, "port", DISCORD_BOT_PORT).rang(1000, 65535)
			.info("Port on which the Discord Chat Bot is listening.")
			.cons((con, map) -> DISCORD_BOT_PORT = con.getInteger(map))
		);
		entries.add(new ConfigEntry(this, DISCORD_CAT, "token", DISCORD_BOT_TOKEN)
			.info("Token to use to communicate with the Discord Chat Bot.")
			.cons((con, map) -> DISCORD_BOT_TOKEN = con.getString(map))
		);
		//
		entries.add(new ConfigEntry(this, CLIENT_CAT, "location_update_on_left", true)
			.info("Set to false if the Location Update GUI should be on the right side.")
			.cons((con, map) -> LOCUP_SIDE = con.getBoolean(map))
		);
		//
		entries.add(new ConfigEntry(this, SERVLANG, "started", SERVLANG_STARTED)
			.info("Message to be send to discord when the server started.")
			.cons((con, map) -> SERVLANG_STARTED = con.getString(map))
		);
		entries.add(new ConfigEntry(this, SERVLANG, "stopping", SERVLANG_STOPPING)
			.info("Message to be send to discord when the server is stopping.")
			.cons((con, map) -> SERVLANG_STOPPING = con.getString(map))
		);
		entries.add(new ConfigEntry(this, SERVLANG, "player_joined", SERVLANG_JOINED)
			.info("Message to be send to discord when a player joins.")
			.cons((con, map) -> SERVLANG_JOINED = con.getString(map))
		);
		entries.add(new ConfigEntry(this, SERVLANG, "player_left", SERVLANG_LEFT)
			.info("Message to be send to discord when a player left.")
			.cons((con, map) -> SERVLANG_LEFT = con.getString(map))
		);
	}

	@Override
	protected void onReload(JsonMap map){
		if(EnvInfo.CLIENT){
			CHAT_OVERRIDE_LANG = ContainerInterface.translate("landdev.chat.player");
		}
		else {
			Broadcaster.SENDERS.values().removeIf(s -> !s.type().internal());
		}
	}

	private static final DecimalFormat df = new DecimalFormat("#.000", new DecimalFormatSymbols(Locale.US));
	static { df.setRoundingMode(RoundingMode.DOWN); }

	public static long format_price(String[] err, String val){
		try{
			String str = val.replace(Config.DOT, "").replace(",", ".");
			if(str.length() == 0) return 0;
			String format = df.format(Double.parseDouble(str));
			return Long.parseLong(format.replace(",", "").replace(".", ""));
		}
		catch(Exception e){
			err[0] = TranslationUtil.translateCmd("invalid_price") + " " + e.getMessage();
			if(Static.dev()) e.printStackTrace();
			return 0;
		}
	}

}
