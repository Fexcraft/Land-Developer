package net.fexcraft.mod.landdev.data;

import java.util.UUID;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public interface PermInteractive {
	
	public boolean can(PermAction act, UUID uuid);
	
	public boolean can(UUID uuid, PermAction... acts);

}
