package net.fexcraft.mod.landdev.cmd;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.county.County;
import net.fexcraft.mod.landdev.data.player.Player;
import net.fexcraft.mod.landdev.data.state.State;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.uni.UniEntity;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.List;

import static net.fexcraft.mod.landdev.data.PermAction.CREATE_COUNTY;
import static net.fexcraft.mod.landdev.util.TranslationUtil.translateCmd;

public class CntCmd extends CommandBase {

    public CntCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("ct");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("ct");
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	Player ply = ResManager.getPlayer(UniEntity.get(sender));
		Chunk_ chunk = ResManager.getChunk(sender.getCommandSenderEntity());
    	if(args.length > 0){
    		switch(args[0]){
	    		case "create":{
	    			State state = chunk.district.state();
	    			boolean cn = state.norms.get("new-counties").bool();
	    			boolean pp = ply.hasPermit(CREATE_COUNTY, state.getLayer(), state.id);
	    			if(!cn && !pp){
		    			Print.chat(sender, translateCmd("ct.no_new_county"));
		    			Print.chat(sender, translateCmd("ct.no_create_permit"));
	    			}
	    			else{
	    				//TODO ply.entity.openGui(LandDev.INSTANCE, GuiHandler.COUNTY, sender.getEntityWorld(), County.UI_CREATE, 0, 0);
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
        	//TODO ply.entity.openGui(LandDev.INSTANCE, GuiHandler.COUNTY, sender.getEntityWorld(), 0, chunk.district.county().id, 0);
    	}
    }

}
