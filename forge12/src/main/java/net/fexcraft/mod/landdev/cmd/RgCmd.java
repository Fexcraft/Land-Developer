package net.fexcraft.mod.landdev.cmd;

import net.fexcraft.lib.mc.utils.Print;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.ui.LDKeys;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.ResManager;
import net.fexcraft.mod.uni.UniEntity;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import java.util.List;

import static net.fexcraft.mod.landdev.util.TranslationUtil.translateCmd;

public class RgCmd extends CommandBase {

    public RgCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("rg");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("rg");
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	LDPlayer ply = ResManager.getPlayer(UniEntity.get(sender));
		Chunk_ chunk = ResManager.getChunkP(sender.getCommandSenderEntity());
    	if(args.length > 0){
    		switch(args[0]){
	    		case "create":{
	    			//TODO
	    			return;
	    		}
	    		default:{
	    			Print.chat(sender, translateCmd("unknown_argument"));
	    			return;
	    		}
    		}
    	}
    	else{
			ply.entity.openUI(LDKeys.REGION, 0, chunk.district.region().id, 0);
    	}
    }

}
