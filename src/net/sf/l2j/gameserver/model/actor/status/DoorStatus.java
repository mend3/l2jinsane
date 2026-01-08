package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.actor.instance.Door;

public class DoorStatus extends CreatureStatus {
    public DoorStatus(Door activeChar) {
        super(activeChar);
    }

    public Door getActiveChar() {
        return (Door)super.getActiveChar();
    }
}
