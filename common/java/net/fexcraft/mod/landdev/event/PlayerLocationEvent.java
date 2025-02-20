package net.fexcraft.mod.landdev.event;

import net.fexcraft.mod.landdev.data.player.LDPlayer;

/**
 * Called when a player moves to another District or named Chunk.
 *
 * @author Ferdinand Calo' (FEX___96)
 */
public class PlayerLocationEvent extends LDEvent {

    private final boolean label;
    private final LDPlayer player;

    public PlayerLocationEvent(LDPlayer play, boolean ck_label){
        label = ck_label;
        player = play;
    }

    /** True if the event was only called because of a named chunk,
     * maybe be false if different district but still a named chunk. */
    public boolean label(){
        return label;
    }

    public LDPlayer player(){
        return player;
    }

}
