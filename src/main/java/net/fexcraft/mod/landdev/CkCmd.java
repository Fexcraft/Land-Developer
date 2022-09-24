package net.fexcraft.mod.landdev;

import java.util.List;

import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CkCmd extends CommandBase {
	
    public CkCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("ck");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("ck");
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	EntityPlayer player = (EntityPlayer)sender;
		Chunk_ chunk = ResManager.getChunk(player);
    	if(args.length > 0 && args[0].equals("claim")){
    		int dis = args.length > 1 ? Integer.parseInt(args[1]) : chunk.district.id;
        	player.openGui(LandDev.INSTANCE, GuiHandler.CLAIM, sender.getEntityWorld(), chunk.key.x, dis, chunk.key.z);
    	}
    	else{
        	player.openGui(LandDev.INSTANCE, GuiHandler.CHUNK, sender.getEntityWorld(), 0, chunk.key.x, chunk.key.z);
    	}
    }

}
