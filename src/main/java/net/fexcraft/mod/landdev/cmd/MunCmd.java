package net.fexcraft.mod.landdev.cmd;

import static net.fexcraft.mod.landdev.data.PermAction.CREATE_MUNICIPALITY;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translateCmd;

import java.util.List;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.municipality.Municipality;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class MunCmd extends CommandBase {
	
    public MunCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("mun");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("mun");
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	Player ply = ResManager.getPlayer((EntityPlayer)sender);
		Chunk_ chunk = ResManager.getChunk(sender.getCommandSenderEntity());
    	if(args.length > 0){
    		switch(args[0]){
	    		case "create":{
	    			County county = chunk.district.county();
	    			boolean cn = county.norms.get("new-municipalities").bool();
	    			boolean pp = ply.hasPermit(CREATE_MUNICIPALITY, county.getLayer(), county.id);
	    			if(!cn && !pp){
		    			Print.chat(sender, translateCmd("mun.no_new_municipalities"));
		    			Print.chat(sender, translateCmd("mun.no_create_permit"));
	    			}
	    			else{
	    				//TODO ply.entity.openGui(LandDev.INSTANCE, GuiHandler.MUNICIPALITY, sender.getEntityWorld(), Municipality.UI_CREATE, 0, 0);
	    			}
	    			return;
	    		}
	    		default:{
	    			Print.chat(sender, translateCmd("unknown_argument"));
	    			return;
	    		}
    		}
    	}
    	else{
    		if(chunk.district.municipality() == null){
    			Print.chat(sender, translateCmd("mun.not_in_a_municipality"));
    			return;
    		}
        	//TODO ply.entity.openGui(LandDev.INSTANCE, GuiHandler.MUNICIPALITY, sender.getEntityWorld(), 0, chunk.district.municipality().id, 0);
    	}
    }

}
