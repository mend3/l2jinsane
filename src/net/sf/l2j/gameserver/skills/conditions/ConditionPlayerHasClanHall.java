package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.skills.Env;

import java.util.List;

public final class ConditionPlayerHasClanHall extends Condition {
    private final List<Integer> _clanHall;

    public ConditionPlayerHasClanHall(List<Integer> clanHall) {
        this._clanHall = clanHall;
    }

    public boolean testImpl(Env env) {
        if (env.getPlayer() == null)
            return false;
        Clan clan = env.getPlayer().getClan();
        if (clan == null)
            return (this._clanHall.size() == 1 && this._clanHall.get(0) == 0);
        if (this._clanHall.size() == 1 && this._clanHall.get(0) == -1)
            return clan.hasClanHall();
        return this._clanHall.contains(clan.getClanHallId());
    }
}
