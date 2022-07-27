package net.fexcraft.mod.landdev;

import java.util.List;

import net.fexcraft.mod.landdev.util.AliasLoader;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class DebugCmd extends CommandBase {
	
    public DebugCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("ld-debug");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("ld-debug");
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	((EntityPlayer)sender).openGui(LandDev.INSTANCE, 0, sender.getEntityWorld(), 0, 0, 0);
    }

}
