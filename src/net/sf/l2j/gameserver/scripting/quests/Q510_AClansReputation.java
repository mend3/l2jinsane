package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q510_AClansReputation extends Quest {
    private static final String qn = "Q510_AClansReputation";

    private static final int VALDIS = 31331;

    private static final int TYRANNOSAURUS_CLAW = 8767;

    public Q510_AClansReputation() {
        super(510, "A Clan's Reputation");
        setItemsIds(8767);
        addStartNpc(31331);
        addTalkId(31331);
        addKillId(22215, 22216, 22217);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q510_AClansReputation");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31331-3.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("31331-6.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int count;
        QuestState st = player.getQuestState("Q510_AClansReputation");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (!player.isClanLeader() || player.getClan().getLevel() < 5) ? "31331-0.htm" : "31331-1.htm";
                break;
            case 1:
                count = 50 * st.getQuestItemsCount(8767);
                if (count > 0) {
                    Clan clan = player.getClan();
                    htmltext = "31331-7.htm";
                    st.takeItems(8767, -1);
                    clan.addReputationScore(count);
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(count));
                    clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
                    break;
                }
                htmltext = "31331-4.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getClanLeaderQuestState(player, npc);
        if (st == null || !st.isStarted())
            return null;
        st.dropItemsAlways(8767, 1, 0);
        return null;
    }
}
