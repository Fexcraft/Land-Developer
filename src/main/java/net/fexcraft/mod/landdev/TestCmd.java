package net.fexcraft.mod.landdev;

import net.fexcraft.lib.mc.api.registry.fCommand;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class TestCmd extends CommandBase {
	
    public TestCmd(){}
    
    @Override 
    public String getName(){ 
        return "ldt";
    } 

    @Override         
    public String getUsage(ICommandSender sender){ 
        return "/ldt";
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
