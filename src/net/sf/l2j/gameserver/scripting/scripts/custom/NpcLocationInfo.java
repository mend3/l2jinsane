package net.sf.l2j.gameserver.scripting.scripts.custom;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class NpcLocationInfo extends Quest {
    private static final String qn = "NpcLocationInfo";

    private static final int[] RADARS = new int[]{
            30006, 30039, 30040, 30041, 30042, 30043, 30044, 30045, 30046, 30283,
            30003, 30004, 30001, 30002, 30031, 30033, 30035, 30032, 30036, 30026,
            30027, 30029, 30028, 30054, 30055, 30005, 30048, 30312, 30368, 30049,
            30047, 30497, 30050, 30311, 30051, 30134, 30224, 30348, 30355, 30347,
            30432, 30356, 30349, 30346, 30433, 30357, 30431, 30430, 30307, 30138,
            30137, 30135, 30136, 30143, 30360, 30145, 30135, 30144, 30358, 30359,
            30141, 30139, 30140, 30350, 30421, 30419, 30130, 30351, 30353, 30354,
            30146, 30285, 30284, 30221, 30217, 30219, 30220, 30218, 30216, 30363,
            30149, 30150, 30148, 30147, 30155, 30156, 30157, 30158, 30154, 30153,
            30152, 30151, 30423, 30414, 31853, 30223, 30362, 30222, 30371, 31852,
            30540, 30541, 30542, 30543, 30544, 30545, 30546, 30547, 30548, 30531,
            30532, 30533, 30534, 30535, 30536, 30525, 30526, 30527, 30518, 30519,
            30516, 30517, 30520, 30521, 30522, 30523, 30524, 30537, 30650, 30538,
            30539, 30671, 30651, 30550, 30554, 30553, 30576, 30577, 30578, 30579,
            30580, 30581, 30582, 30583, 30584, 30569, 30570, 30571, 30572, 30564,
            30560, 30561, 30558, 30559, 30562, 30563, 30565, 30566, 30567, 30568,
            30585, 30587};

    public NpcLocationInfo() {
        super(-1, "custom");
        addStartNpc(30598, 30599, 30600, 30601, 30602);
        addTalkId(30598, 30599, 30600, 30601, 30602);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("NpcLocationInfo");
        if (st == null)
            return htmltext;
        if (StringUtil.isDigit(event)) {
            htmltext = null;
            int npcId = Integer.parseInt(event);
            if (ArraysUtil.contains(RADARS, npcId)) {
                for (L2Spawn spawn : SpawnTable.getInstance().getSpawns()) {
                    if (npcId == spawn.getNpcId()) {
                        st.addRadar(spawn.getLocX(), spawn.getLocY(), spawn.getLocZ());
                        break;
                    }
                }
                htmltext = "MoveToLoc.htm";
            }
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        return npc.getNpcId() + ".htm";
    }
}
