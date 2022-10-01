package net.fexcraft.mod.landdev.cmd;

import static net.fexcraft.mod.fsmm.util.Config.getWorthAsString;

import java.util.List;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.landdev.util.Settings;
import net.fexcraft.mod.landdev.util.TranslationUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class LDCmd extends CommandBase {
	
    public LDCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("landdev");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("landdev");
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	Player player = ResManager.getPlayer((EntityPlayer)sender);
    	if(args.length > 0){
    		switch(args[0]){
	    		case "fees":{
	    			Chunk_ chunk = ResManager.getChunk(player.entity);
	        		Print.chat(sender, TranslationUtil.translateCmd("fees"));
	        		long sf = Settings.MUNICIPALITY_CREATION_FEE;
	        		long cf = chunk.district.county().norms.get("new-municipality-fee").integer();
	        		Print.chat(sender, TranslationUtil.translateCmd("fees_municipality"));
	        		Print.chat(sender, TranslationUtil.translateCmd("fees_server", getWorthAsString(sf)));
	        		Print.chat(sender, TranslationUtil.translateCmd("fees_county", getWorthAsString(cf)));
	        		Print.chat(sender, TranslationUtil.translateCmd("fees_total", getWorthAsString(sf + cf)));
	        		return;
	    		}
    			case "help":
    			default:{
	        		Print.chat(sender, "&0[&bLD&0]&6>>&2===========");
	        		Print.chat(sender, "/ld (UI)");
	        		Print.chat(sender, "/ld help");
	        		Print.chat(sender, "/ld fees");
    				return;
    			}
    		}
    	}
    	player.openGui(-1, 0, (int)player.entity.posX >> 4, (int)player.entity.posZ >> 4);
    }

}
