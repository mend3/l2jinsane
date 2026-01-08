package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.actor.Npc;

public class NpcStat extends CreatureStat {
    public NpcStat(Npc activeChar) {
        super(activeChar);
    }

    public byte getLevel() {
        return this.getActiveChar().getTemplate().getLevel();
    }

    public Npc getActiveChar() {
        return (Npc) super.getActiveChar();
    }
}
