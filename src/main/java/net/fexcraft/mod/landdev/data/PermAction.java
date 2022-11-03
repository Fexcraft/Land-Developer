package net.fexcraft.mod.landdev.data;

public enum PermAction {
	
	ACT_CLAIM("claim"),
	ACT_CREATE_LAYER("create_layer"),
	ACT_MANAGE_DISTRICT("manage_district"),
	ACT_SET_CHUNK_TAX("set_tax_chunk"),
	ACT_SET_PLAYER_TAX("set_tax_player"),
	;
	
	public final String norm;
	
	PermAction(String normid){
		norm = normid;
	}

	public static class PermActions {
		
		public final PermAction[] actions;
		
		public PermActions(PermAction... actions){
			this.actions = actions;
		}
		
		public boolean isValid(PermAction act){
			for(PermAction a : actions) if(act == a) return true;
			return false;
		}
		
	}

}
