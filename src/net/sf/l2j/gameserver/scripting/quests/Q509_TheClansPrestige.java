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

public class Q509_TheClansPrestige extends Quest {
    private static final String qn = "Q509_TheClansPrestige";

    private static final int VALDIS = 31331;

    private static final int DAIMON_EYES = 8489;

    private static final int HESTIA_FAIRY_STONE = 8490;

    private static final int NUCLEUS_OF_LESSER_GOLEM = 8491;

    private static final int FALSTON_FANG = 8492;

    private static final int SHAID_TALON = 8493;

    private static final int DAIMON_THE_WHITE_EYED = 25290;

    private static final int HESTIA_GUARDIAN_DEITY = 25293;

    private static final int PLAGUE_GOLEM = 25523;

    private static final int DEMON_AGENT_FALSTON = 25322;

    private static final int QUEEN_SHYEED = 25514;

    private static final int[][] reward_list = new int[][]{{25290, 8489, 180, 215}, {25293, 8490, 430, 465}, {25523, 8491, 380, 415}, {25322, 8492, 220, 255}, {25514, 8493, 130, 165}};

    private static final int[][] radar = new int[][]{{186320, -43904, -3175}, {134672, -115600, -1216}, {170000, -59900, -3848}, {93296, -75104, -1824}, {79635, -55612, -5980}};

    public Q509_TheClansPrestige() {
        super(509, "The Clan's Prestige");
        setItemsIds(8489, 8490, 8491, 8492, 8493);
        addStartNpc(31331);
        addTalkId(31331);
        addKillId(25290, 25293, 25523, 25322, 25514);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q509_TheClansPrestige");
        if (st == null)
            return htmltext;
        if (StringUtil.isDigit(event)) {
            htmltext = "31331-" + event + ".htm";
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
        } else if (event.equalsIgnoreCase("31331-6.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int raid, item, reward;
        QuestState st = player.getQuestState("Q509_TheClansPrestige");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        Clan clan = player.getClan();
        switch (st.getState()) {
            case 0:
                if (!player.isClanLeader()) {
                    htmltext = "31331-0a.htm";
                    break;
                }
                if (clan.getLevel() < 6) {
                    htmltext = "31331-0b.htm";
                    break;
                }
                htmltext = "31331-0c.htm";
                break;
            case 1:
                raid = st.getInt("raid");
                item = reward_list[raid - 1][1];
                if (!st.hasQuestItems(item)) {
                    htmltext = "31331-" + raid + "a.htm";
                    break;
                }
                reward = Rnd.get(reward_list[raid - 1][2], reward_list[raid - 1][3]);
                htmltext = "31331-" + raid + "b.htm";
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
