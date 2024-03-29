package net.fexcraft.mod.landdev.data;

import java.util.UUID;

public interface PermInteractive {
	
	public boolean can(PermAction act, UUID uuid);
	
	public boolean can(UUID uuid, PermAction... acts);

}
