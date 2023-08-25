package net.fexcraft.mod.landdev.data;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public enum MailType {

	EXPIRED(0, 246, 0, 236),
	INVITE(16, 246, 16, 236),
	SYSTEM(32, 246, 32, 236),
	LAYER(48, 246, 48, 236),
	BUSINESS(64, 246, 64, 236),
	PRIVATE(80, 246, 80, 236);

	public int u_unread;
	public int v_unread;
	public int u_read;
	public int v_read;

	MailType(int u_u, int v_u, int u_r, int v_r){
		u_unread = u_u;
		v_unread = v_u;
		u_read = u_r;
		v_read = v_r;
	}

}
