package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestSkillList extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player cha = getClient().getPlayer();
        if (cha == null)
            return;
        cha.sendSkillList();
    }
}
