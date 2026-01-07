package net.sf.l2j.gameserver.scripting.tasks;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.scripting.ScheduledQuest;

public final class ClanLadderRefresh extends ScheduledQuest {
    public ClanLadderRefresh() {
        super(-1, "tasks");
    }

    public void onStart() {
        ClanTable.getInstance().refreshClansLadder(true);
    }

    public void onEnd() {
    }
}
