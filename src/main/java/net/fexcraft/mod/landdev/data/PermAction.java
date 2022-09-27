package net.fexcraft.mod.landdev.data;

public enum PermAction {
	
	ACT_CLAIM("claim"),
	ACT_CREATE_LAYER("create_layer");
	
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
