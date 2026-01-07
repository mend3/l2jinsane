package net.sf.l2j.gameserver.scripting.scripts.teleports;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class RaceTrack extends Quest {
    private static final int RACE_MANAGER = 30995;

    private static final Map<Integer, Location> RETURN_LOCATIONS = new HashMap<>();

    public RaceTrack() {
        super(-1, "teleports");
        RETURN_LOCATIONS.put(30320, new Location(-80826, 149775, -3043));
        RETURN_LOCATIONS.put(30256, new Location(-12672, 122776, -3116));
        RETURN_LOCATIONS.put(30059, new Location(15670, 142983, -2705));
        RETURN_LOCATIONS.put(30080, new Location(83400, 147943, -3404));
        RETURN_LOCATIONS.put(30899, new Location(111409, 219364, -3545));
        RETURN_LOCATIONS.put(30177, new Location(82956, 53162, -1495));
        RETURN_LOCATIONS.put(30848, new Location(146331, 25762, -2018));
        RETURN_LOCATIONS.put(30233, new Location(116819, 76994, -2714));
        RETURN_LOCATIONS.put(31320, new Location(43835, -47749, -792));
        RETURN_LOCATIONS.put(31275, new Location(147930, -55281, -2728));
        RETURN_LOCATIONS.put(31964, new Location(87386, -143246, -1293));
        RETURN_LOCATIONS.put(31210, new Location(12882, 181053, -3560));
        addStartNpc(30320, 30256, 30059, 30080, 30899, 30177, 30848, 30233, 31320, 31275,
                31964, 31210);
        addTalkId(30995, 30320, 30256, 30059, 30080, 30899, 30177, 30848, 30233, 31320,
                31275, 31964, 31210);
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState(getName());
        if (RETURN_LOCATIONS.containsKey(npc.getNpcId())) {
            player.teleportTo(12661, 181687, -3560, 0);
            st.setState((byte) 1);
            st.set("id", Integer.toString(npc.getNpcId()));
        } else if (st.isStarted() && npc.getNpcId() == 30995) {
            player.teleportTo(RETURN_LOCATIONS.get(st.getInt("id")), 0);
            st.exitQuest(true);
        }
        return null;
    }
}
