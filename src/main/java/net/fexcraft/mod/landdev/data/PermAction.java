package net.fexcraft.mod.landdev.data;

public enum PermAction {

	CHUNK_CLAIM,
	CHUNK_TAX,
	CHUNK_CUSTOMTAX,
	CREATE_MUNICIPALITY,
	CREATE_DISTRICT,
	CREATE_COUNTY,
	CREATE_STATE,
	MANAGE_DISTRICT,
	MANAGE_MUNICIPALITY,
	MAIL_READ,
	MAIL_DELETE,
	PLAYER_TAX,
	PLAYER_INVITE,
	FINANCES_USE,
	FINANCES_MANAGE,
	;

	public static PermActions DISTRICT_ACTIONS = new PermActions(
		CHUNK_CLAIM,
		CHUNK_TAX,
		CHUNK_CUSTOMTAX,
		MAIL_READ,
		MAIL_DELETE
	);
	public static PermActions MUNICIPALITY_STAFF = new PermActions(
		CHUNK_CLAIM,
		CHUNK_TAX,
		CHUNK_CUSTOMTAX,
		PLAYER_TAX,
		PLAYER_INVITE,
		FINANCES_USE,
		FINANCES_MANAGE,
		MAIL_READ,
		MAIL_DELETE,
		CREATE_DISTRICT,
		MANAGE_DISTRICT,
		MANAGE_MUNICIPALITY
	);
	public static PermActions MUNICIPALITY_CITIZEN = new PermActions(CHUNK_CLAIM);
	public static PermActions COUNTY_STAFF = new PermActions(
		CHUNK_CLAIM,
		CHUNK_TAX,
		CHUNK_CUSTOMTAX,
		PLAYER_TAX,
		FINANCES_USE,
		FINANCES_MANAGE,
		MAIL_READ,
		MAIL_DELETE
	);
	public static PermActions COUNTY_CITIZEN = new PermActions(
		CHUNK_CLAIM,
		CREATE_MUNICIPALITY
	);
	public static PermActions STATE_STAFF = new PermActions(
		CHUNK_CLAIM,
		FINANCES_USE,
		FINANCES_MANAGE,
		MAIL_READ,
		MAIL_DELETE
	);
	/*public static PermActions STATE_CITIZEN = new PermActions(
		CHUNK_CLAIM,
		CREATE_COUNTY,
		CREATE_STATE
	);*/

	public static PermAction get(String key){
		for(PermAction act : values()){
			if(act.name().equals(key)) return act;
		}
		return null;
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
