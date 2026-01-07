package net.sf.l2j.gameserver.scripting.tasks;

import net.sf.l2j.gameserver.data.manager.RaidPointManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.scripting.ScheduledQuest;

import java.util.HashMap;
import java.util.Map;

public final class RaidPointReset extends ScheduledQuest {
    public RaidPointReset() {
        super(-1, "tasks");
    }

    public void onStart() {
        Map<Integer, Integer> ranks = RaidPointManager.getInstance().getWinners();
        Map<Clan, Integer> rewards = new HashMap<>();
        for (Clan clan : ClanTable.getInstance().getClans()) {
            if (clan.getLevel() < 5)
                continue;
            for (Map.Entry<Integer, Integer> entry : ranks.entrySet()) {
                if (!clan.isMember(entry.getKey()))
                    continue;
                int points = 0;
                switch (entry.getValue()) {
                    case 1:
                        points = 1250;
                        break;
                    case 2:
                        points = 900;
                        break;
                    case 3:
                        points = 700;
                        break;
                    case 4:
                        points = 600;
                        break;
                    case 5:
                        points = 450;
                        break;
                    case 6:
                        points = 350;
                        break;
                    case 7:
                        points = 300;
                        break;
                    case 8:
                        points = 200;
                        break;
                    case 9:
                        points = 150;
                        break;
                    case 10:
                        points = 100;
                        break;
                    default:
                        points = (entry.getValue() <= 50) ? 25 : 12;
                        break;
                }
                rewards.merge(clan, Integer.valueOf(points), Integer::sum);
            }
        }
        for (Map.Entry<Clan, Integer> entry : rewards.entrySet())
            entry.getKey().addReputationScore(entry.getValue());
        RaidPointManager.getInstance().cleanUp();
    }

    public void onEnd() {
    }
}
