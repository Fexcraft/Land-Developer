package net.fexcraft.mod.landdev.event;

import net.fexcraft.mod.landdev.data.Layer;
import net.fexcraft.mod.landdev.data.player.LDPlayer;

/**
 * Called when a new layer is created, e.g. Municipality, County
 *
 * @author Moose1002
 */
public class CreateLayerEvent extends LDEvent {

    private final Layer layer;
    private final LDPlayer player;

    public CreateLayerEvent(Layer lay, LDPlayer play){
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
