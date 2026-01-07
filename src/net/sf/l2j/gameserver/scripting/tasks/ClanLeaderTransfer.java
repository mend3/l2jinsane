package net.sf.l2j.gameserver.scripting.tasks;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.scripting.ScheduledQuest;

public class ClanLeaderTransfer extends ScheduledQuest {
    public ClanLeaderTransfer() {
        super(-1, "tasks");
    }

    public final void onStart() {
        for (Clan clan : ClanTable.getInstance().getClans()) {
            if (clan.getNewLeaderId() <= 0)
                continue;
            ClanMember member = clan.getClanMember(clan.getNewLeaderId());
            if (member == null) {
                clan.setNewLeaderId(0, true);
                continue;
            }
            clan.setNewLeader(member);
        }
    }

    public final void onEnd() {
    }
}
