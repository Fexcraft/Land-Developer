package net.fexcraft.mod.landdev.data;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

public interface PermInteractive {
	
	public boolean can(PermAction act, EntityPlayer player, UUID uuid);

}
