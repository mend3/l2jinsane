package net.sf.l2j.gameserver.model.olympiad;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.OlympiadType;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.util.List;

public class OlympiadGameClassed extends OlympiadGameNormal {
    private OlympiadGameClassed(int id, Participant[] opponents) {
        super(id, opponents);
    }

    protected static OlympiadGameClassed createGame(int id, List<List<Integer>> classList) {
        if (classList == null || classList.isEmpty())
            return null;
        while (!classList.isEmpty()) {
            List<Integer> list = Rnd.get(classList);
            if (list == null || list.size() < 2) {
                classList.remove(list);
                continue;
            }
            Participant[] opponents = OlympiadGameNormal.createListOfParticipants(list);
            if (opponents == null) {
                classList.remove(list);
                continue;
            }
            return new OlympiadGameClassed(id, opponents);
        }
        return null;
    }

    public final OlympiadType getType() {
        return OlympiadType.CLASSED;
    }

    protected final int getDivider() {
        return Config.ALT_OLY_DIVIDER_CLASSED;
    }

    protected final IntIntHolder[] getReward() {
        return Config.ALT_OLY_CLASSED_REWARD;
    }
}
