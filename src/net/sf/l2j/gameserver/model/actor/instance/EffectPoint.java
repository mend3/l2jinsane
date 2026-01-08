package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public class EffectPoint extends Npc {
    private final Player _owner;

    public EffectPoint(int objectId, NpcTemplate template, Creature owner) {
        super(objectId, template);
        this._owner = owner == null ? null : owner.getActingPlayer();
    }

    public Player getActingPlayer() {
        return this._owner;
    }

    public void onAction(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void onActionShift(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public boolean hasRandomAnimation() {
        return false;
    }
}
