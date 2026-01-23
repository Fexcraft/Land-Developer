package net.fexcraft.mod.landdev.util;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.landdev.data.chunk.ChunkType;
import net.fexcraft.mod.landdev.data.chunk.Chunk_;
import net.fexcraft.mod.landdev.data.player.LDPlayer;
import net.fexcraft.mod.landdev.data.prop.Property;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class InteractHandler {

	public static boolean control(int x, int y, int z, Object entity, boolean interact){
		LDPlayer player = ResManager.getPlayer(entity);
		if(player == null) return false;
		if(player.adm) return true;
		Chunk_ chunk = ResManager.getChunkS(x, z);
		for(Property prop : chunk.propholder.properties){
			if(prop.isInside(x, y, z)) return hasperm(prop, chunk, player, interact);
		}
		if(chunk.district.id < 0){
			if(chunk.district.id == -1){
				return LDConfig.EDIT_WILDERNESS;
			}
			else if(chunk.district.id == -2){
				//TODO temporary claims
				return false;
			}
			else{
				player.entity.bar("interact.control.unknown_district");
				return false;
			}
		}
		return hasperm(chunk, player, interact);
	}

	public static boolean hasperm(Chunk_ chunk, LDPlayer player, boolean interact){
		if(interact && chunk.access.interact) return true;
		ChunkType type = chunk.owner.unowned ? ChunkType.NORMAL : chunk.type;
		boolean pass = false;
		switch(type){
			case NORMAL:
				pass = chunk.district.owner.isPartOf(player);
				break;
			case PRIVATE:
				if(chunk.owner.playerchunk){
					pass = player.uuid.equals(chunk.owner.player);
				}
				else{
					switch(chunk.owner.owner){
						case COMPANY:
							//TODO
							break;
						case COUNTY:
							pass = chunk.district.owner.manageable().isManager(player.uuid);
							break;
						case DISTRICT:
							pass = chunk.district.manage.isManager(player.uuid) || chunk.district.owner.manageable().isManager(player.uuid);
							break;
						case MUNICIPALITY:
							pass = chunk.district.owner.manageable().isManager(player.uuid);
							break;
						case REGION:
							pass = chunk.district.region().manage.isManager(player.uuid);
							break;
						default:
							break;
					}
				}
				break;
			case PUBLIC:
				pass = true;
				break;
			case RESTRICTED:
				switch(chunk.owner.owner){
					case COMPANY:
						//TODO
						break;
					case COUNTY:
						pass = chunk.district.owner.manageable().isStaff(player.uuid);
						break;
					case DISTRICT:
						pass = chunk.district.manage.isManager(player.uuid) || chunk.district.owner.manageable().isStaff(player.uuid);
						break;
					case MUNICIPALITY:
						pass = chunk.district.owner.manageable().isStaff(player.uuid);
						break;
					case REGION:
						pass = chunk.district.region().manage.isStaff(player.uuid);
						break;
					default:
						break;
				}
				break;
			case LOCKED:
				break;
			default:
				player.entity.bar("interact.control.unknown_chunk_type");
				return false;

		}
		if(!pass){
			Long l = chunk.access.players.get(player.uuid);
			if(l != null){
				if(Time.getDate() < l) return true;
				else{
					chunk.access.players.remove(player.uuid);
				}
			}
			//TODO company check
		}
		return pass;
	}

	public static boolean hasperm(Property prop, Chunk_ chunk, LDPlayer player, boolean interact){
		if(prop.rent.renter.unowned) return prop.owner.hasPerm(player, chunk);
		else return prop.rent.renter.hasPerm(player, chunk);
	}

}
