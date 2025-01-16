package net.fexcraft.mod.landdev.event;

import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.player.LDPlayer;

/**
 * Called when someone leaves a Layer, e.g. Municipality, County
 *
 * @author Ferdinand Calo' (FEX___96)
 */
public class LeaveLayerEvent extends LDEvent {

    private final Layer layer;
    private final LDPlayer player;

    public LeaveLayerEvent(Layer lay, LDPlayer play){
        layer = lay;
        player = play;
    }

    public Layer layer(){
        return layer;
    }

    public LDPlayer player(){
        return player;
    }

}
