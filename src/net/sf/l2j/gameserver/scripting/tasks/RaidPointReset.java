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
                int points = switch (entry.getValue()) {
                    case 1 -> 1250;
                    case 2 -> 900;
                    case 3 -> 700;
                    case 4 -> 600;
                    case 5 -> 450;
                    case 6 -> 350;
                    case 7 -> 300;
                    case 8 -> 200;
                    case 9 -> 150;
                    case 10 -> 100;
                    default -> (entry.getValue() <= 50) ? 25 : 12;
                };
                rewards.merge(clan, points, Integer::sum);
            }
        }
        for (Map.Entry<Clan, Integer> entry : rewards.entrySet())
            entry.getKey().addReputationScore(entry.getValue());
        RaidPointManager.getInstance().cleanUp();
    }

    public void onEnd() {
    }
}
