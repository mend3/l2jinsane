package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.skills.Env;

public final class ConditionPlayerHasCastle extends Condition {
    private final int _castle;

    public ConditionPlayerHasCastle(int castle) {
        this._castle = castle;
    }

    public boolean testImpl(Env env) {
        if (env.getPlayer() == null)
            return false;
        Clan clan = env.getPlayer().getClan();
        if (clan == null)
            return (this._castle == 0);
        if (this._castle == -1)
            return clan.hasCastle();
        return (clan.getCastleId() == this._castle);
    }
}
