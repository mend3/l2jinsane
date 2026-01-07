package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q508_AClansReputation extends Quest {
    private static final String qn = "Q508_AClansReputation";

    private static final int SIR_ERIC_RODEMAI = 30868;

    private static final int NUCLEUS_OF_FLAMESTONE_GIANT = 8494;

    private static final int THEMIS_SCALE = 8277;

    private static final int NUCLEUS_OF_HEKATON_PRIME = 8279;

    private static final int TIPHON_SHARD = 8280;

    private static final int GLAKI_NUCLEUS = 8281;

    private static final int RAHHA_FANG = 8282;

    private static final int FLAMESTONE_GIANT = 25524;

    private static final int PALIBATI_QUEEN_THEMIS = 25252;

    private static final int HEKATON_PRIME = 25140;

    private static final int GARGOYLE_LORD_TIPHON = 25255;

    private static final int LAST_LESSER_GIANT_GLAKI = 25245;

    private static final int RAHHA = 25051;

    private static final int[][] reward_list = new int[][]{{25252, 8277, 65, 100}, {25140, 8279, 40, 75}, {25255, 8280, 30, 65}, {25245, 8281, 105, 140}, {25051, 8282, 40, 75}, {25524, 8494, 60, 95}};

    private static final int[][] radar = new int[][]{{192346, 21528, -3648}, {191979, 54902, -7658}, {170038, -26236, -3824}, {171762, 55028, -5992}, {117232, -9476, -3320}, {144218, -5816, -4722}};

    public Q508_AClansReputation() {
        super(508, "A Clan's Reputation");
        setItemsIds(8277, 8279, 8280, 8281, 8282, 8494);
        addStartNpc(30868);
        addTalkId(30868);
        addKillId(25524, 25252, 25140, 25255, 25245, 25051);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q508_AClansReputation");
        if (st == null)
            return htmltext;
        if (StringUtil.isDigit(event)) {
            htmltext = "30868-" + event + ".htm";
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("raid", event);
            st.playSound("ItemSound.quest_accept");
            int evt = Integer.parseInt(event);
            int x = radar[evt - 1][0];
            int y = radar[evt - 1][1];
            int z = radar[evt - 1][2];
            if (x + y + z > 0)
                st.addRadar(x, y, z);
        } else if (event.equalsIgnoreCase("30868-7.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int raid, item, reward;
        QuestState st = player.getQuestState("Q508_AClansReputation");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        Clan clan = player.getClan();
        switch (st.getState()) {
            case 0:
                if (!player.isClanLeader()) {
                    htmltext = "30868-0a.htm";
                    break;
                }
                if (clan.getLevel() < 5) {
                    htmltext = "30868-0b.htm";
                    break;
                }
                htmltext = "30868-0c.htm";
                break;
            case 1:
                raid = st.getInt("raid");
                item = reward_list[raid - 1][1];
                if (!st.hasQuestItems(item)) {
                    htmltext = "30868-" + raid + "a.htm";
                    break;
                }
                reward = Rnd.get(reward_list[raid - 1][2], reward_list[raid - 1][3]);
                htmltext = "30868-" + raid + "b.htm";
                st.takeItems(item, 1);
                clan.addReputationScore(reward);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(reward));
                clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getClanLeaderQuestState(player, npc);
        if (st == null || !st.isStarted())
            return null;
        int raid = st.getInt("raid");
        if (reward_list[raid - 1][0] == npc.getNpcId())
            st.dropItemsAlways(reward_list[raid - 1][1], 1, 1);
        return null;
    }
}
