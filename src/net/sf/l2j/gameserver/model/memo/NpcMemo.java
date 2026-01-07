package net.sf.l2j.gameserver.model.memo;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;

public class NpcMemo extends AbstractMemo {
    public int getInteger(String key) {
        return getInteger(key, 0);
    }

    public boolean load() {
        return true;
    }

    public boolean storeMe() {
        return true;
    }

    public Player getPlayer(String name) {
        return getObject(name, Player.class);
    }

    public Summon getSummon(String name) {
        return getObject(name, Summon.class);
    }
}
