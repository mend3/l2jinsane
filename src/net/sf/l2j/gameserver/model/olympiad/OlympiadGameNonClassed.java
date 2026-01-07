package net.sf.l2j.gameserver.model.olympiad;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.OlympiadType;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.util.List;

public class OlympiadGameNonClassed extends OlympiadGameNormal {
    private OlympiadGameNonClassed(int id, Participant[] opponents) {
        super(id, opponents);
    }

    protected static OlympiadGameNonClassed createGame(int id, List<Integer> list) {
        Participant[] opponents = OlympiadGameNormal.createListOfParticipants(list);
        if (opponents == null)
            return null;
        return new OlympiadGameNonClassed(id, opponents);
    }

    public final OlympiadType getType() {
        return OlympiadType.NON_CLASSED;
    }

    protected final int getDivider() {
        return Config.ALT_OLY_DIVIDER_NON_CLASSED;
    }

    protected final IntIntHolder[] getReward() {
        return Config.ALT_OLY_NONCLASSED_REWARD;
    }
}
