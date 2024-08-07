package net.fexcraft.mod.landdev.util;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import net.fexcraft.lib.common.Static;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.landdev.data.IconHolder;
import net.fexcraft.mod.landdev.data.district.DistrictType;
import net.fexcraft.mod.landdev.util.broad.Broadcaster;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class Settings {
	
	public static File CONFIG_PATH;
	//
	public static String SERVER_ICON = "http://fexcraft.net/files/mod_data/landdev/default_server_icon.png";
	public static String DEFAULT_ICON = "http://fexcraft.net/files/mod_data/landdev/default_icon.png";
	public static final String BROADCASTER = "http://fexcraft.net/files/mod_data/landdev/broadcaster_icon.png";
	public static IconHolder SERVER_ICONHOLDER = new IconHolder(SERVER_ICON);
	public static boolean SAVE_SPACED_JSON;
	public static boolean PROTECT_WILDERNESS;
	public static boolean EDIT_WILDERNESS;
	public static boolean LOCUP_SIDE;
	public static boolean SAVE_CHUNKS_IN_REGIONS;
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
	public static final String DEFAULT_CAT = "default_settings";
	public static final String DISCORD_CAT = "discord_settings";
	public static final String CLIENT_CAT = "client_settings";
	//
	private static Configuration config;
	
	public static void initialize(FMLPreInitializationEvent event){
		CONFIG_PATH = event.getSuggestedConfigurationFile().getParentFile();
		config = new Configuration(event.getSuggestedConfigurationFile(), "1.0", true);
		config.load();
		config.setCategoryRequiresMcRestart(DEFAULT_CAT, false);
		config.setCategoryRequiresWorldRestart(DEFAULT_CAT, true);
		config.setCategoryComment(DEFAULT_CAT, "General Settings.");
		config.setCategoryRequiresMcRestart(DISCORD_CAT, false);
		config.setCategoryRequiresWorldRestart(DISCORD_CAT, true);
		config.setCategoryComment(DISCORD_CAT, "Discord Settings.");
		config.setCategoryRequiresMcRestart(CLIENT_CAT, false);
		config.setCategoryRequiresWorldRestart(CLIENT_CAT, false);
		config.setCategoryComment(CLIENT_CAT, "Client Settings.");
		refresh(true);
		//
		DistrictType.loadConfig(CONFIG_PATH);
	}

	public static List<IConfigElement> getList(){
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		list.add(new ConfigElement(Settings.getConfig().getCategory(DEFAULT_CAT)));
		list.add(new ConfigElement(Settings.getConfig().getCategory(CLIENT_CAT)));
		list.add(new ConfigElement(Settings.getConfig().getCategory(DISCORD_CAT)));
		return list;
	}

	public static final Configuration getConfig(){
		return config;
	}
	
	public static void refresh(boolean save){
		SERVER_ICON = config.getString("server_icon", DEFAULT_CAT, SERVER_ICON, "Server Icon to be shown in the Location Update GUI.");
		SERVER_ICONHOLDER.set(SERVER_ICON);
		DEFAULT_ICON = config.getString("default_icon", DEFAULT_CAT, DEFAULT_ICON, "Default Dis/Mun/Cou/State Icon to be shown in the Location Update GUI.");
		SAVE_SPACED_JSON = config.getBoolean("save_spaced_json", DEFAULT_CAT, false, "If true, the JSON will be formatted to be easily readable, otherwise if false it will not have any spacing, to save on disk and load time.");
		DEFAULT_CHUNK_PRICE = config.getInt("default_chunk_price", DEFAULT_CAT, 100000, 0, 1000000000, "Default price for unclaimed chunks. (1000 = 1$)");
		COUNTY_CREATION_FEE = config.getInt("county_creation_fee", DEFAULT_CAT, 250000000, 0, Integer.MAX_VALUE, "Server fee for creating a county, half of it goes to the new County.");
		MUNICIPALITY_CREATION_FEE = config.getInt("municipality_creation_fee", DEFAULT_CAT, 25000000, 0, Integer.MAX_VALUE, "Server fee for creating a municipality, half of it goes to the new Municipality.");
		DISTRICT_CREATION_FEE = config.getInt("district_creation_fee", DEFAULT_CAT, 5000000, 0, Integer.MAX_VALUE, "Server fee for creating a district.");
		PROTECT_WILDERNESS = config.getBoolean("protect_wilderness", DEFAULT_CAT, true, "If wilderness protection should be enabled.");
		EDIT_WILDERNESS = !PROTECT_WILDERNESS;
		CHUNK_LINK_LIMIT = config.getInt("chunk_link_limit", DEFAULT_CAT, 3, 0, 64, "How many chunks can be linked to another. Set to '0' to disable.");
		REQUEST_TIMEOUT_DAYS = config.getInt("request_timeout_days", DEFAULT_CAT, 7, 1, 64, "Default days value for request timeouts.");
		SAVE_CHUNKS_IN_REGIONS = config.getBoolean("save_chunks_in_regions", DEFAULT_CAT, false, "Should chunk data be saved in custom/separate region files rather than the chunks themselves? WARNING: Existing chunk data may not port over when changing this setting.");
		TAX_ENABLED = config.getBoolean("tax_enabled", DEFAULT_CAT, true, "If the Tax System should be enabled.");
		TAX_INTERVAL = config.getInt("tax_interval", DEFAULT_CAT, 24, 1, 168, "Interval between tax collection cycles, in hours.");
		TAX_INTERVAL *= Time.HOUR_MS;
		TAX_OFFLINE = config.getBoolean("tax_offline_players", DEFAULT_CAT, false, "If offline (inactive since more than one interval) players should be taxed as well.");
		//
		CHAT_OVERRIDE_LANG = TranslationUtil.translate("chat.player");
		Broadcaster.SENDERS.values().removeIf(s -> !s.type().internal());
		CHAT_OVERRIDE = config.getBoolean("chat_override", DEFAULT_CAT, true, "If the vanilla chat should be overriden by the LandDev chat.");
		CHAT_PLAYER_COLOR = config.getString("chat_color_player", DEFAULT_CAT, CHAT_PLAYER_COLOR, "Default color of the player role marker (default `#`) in chat.");
		CHAT_ADMIN_COLOR = config.getString("chat_color_admin", DEFAULT_CAT, CHAT_ADMIN_COLOR, "Default color of the admin role marker (default `#`) in chat.");
		CHAT_DISCORD_COLOR = config.getString("chat_color_discord", DEFAULT_CAT, CHAT_DISCORD_COLOR, "Default color of the discord role marker (default `#`) in chat.");
		//
		DISCORD_BOT_ACTIVE = config.getBoolean("discord_bot_active", DISCORD_CAT, false, "If the discord chat integration should be active.");
		DISCORD_BOT_ADRESS = config.getString("discord_bot_adress", DISCORD_CAT, DISCORD_BOT_ADRESS, "Adress/IP of the Discord Chat Bot you are using. By default this points to the one provided by Fexcraft");
		DISCORD_BOT_PORT = config.getInt("discord_bot_port", DISCORD_CAT, DISCORD_BOT_PORT, 1000, 65535, "Port on which the Discord Chat Bot is listening.");
		DISCORD_BOT_TOKEN = config.getString("discord_bot_token", DISCORD_CAT, DISCORD_BOT_TOKEN, "Token to use to communicate with the Discord Chat Bot.");
		//
		LOCUP_SIDE = config.getBoolean("location_update_on_left", CLIENT_CAT, true, "Set to false if the Location Update GUI should be on the right side.");
		//
		if(save || config.hasChanged()) config.save();
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