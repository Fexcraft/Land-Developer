package net.fexcraft.mod.landdev.cmd;

import java.util.List;

import net.fexcraft.mod.landdev.LandDev;
import net.fexcraft.mod.landdev.gui.GuiHandler;
import net.fexcraft.mod.landdev.util.AliasLoader;
import net.fexcraft.mod.landdev.util.ResManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class DisCmd extends CommandBase {
	
    public DisCmd(){}
    
	@Override
	public String getName(){
		return AliasLoader.getOverride("dis");
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/" + getName();
	}
	
	@Override
	public List<String> getAliases(){
		return AliasLoader.getAlias("dis");
	}
    
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender){
    	return true;
    }

    @Override 
    public void execute(MinecraftServer server, ICommandSender sender, String[] args){ 
    	if(sender instanceof EntityPlayer == false) return;
    	EntityPlayer player = (EntityPlayer)sender;
    	player.openGui(LandDev.INSTANCE, GuiHandler.DISTRICT, sender.getEntityWorld(), 0, ResManager.getChunk(player).district.id, 0);
    }

}
