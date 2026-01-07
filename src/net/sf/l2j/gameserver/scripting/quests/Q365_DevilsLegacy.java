package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q365_DevilsLegacy extends Quest {
    private static final String qn = "Q365_DevilsLegacy";

    private static final int RANDOLF = 30095;

    private static final int COLLOB = 30092;

    private static final int PIRATE_TREASURE_CHEST = 5873;

    public Q365_DevilsLegacy() {
        super(365, "Devil's Legacy");
        setItemsIds(5873);
        addStartNpc(30095);
        addTalkId(30095, 30092);
        addKillId(20836, 20845, 21629, 21630);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q365_DevilsLegacy");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30095-02.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30095-06.htm")) {
            st.playSound("ItemSound.quest_giveup");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30092-05.htm")) {
            if (!st.hasQuestItems(5873)) {
                htmltext = "30092-02.htm";
            } else if (st.getQuestItemsCount(57) < 600) {
                htmltext = "30092-03.htm";
            } else {
                st.takeItems(5873, 1);
                st.takeItems(57, 600);
                if (Rnd.get(100) < 80) {
                    int i0 = Rnd.get(100);
                    if (i0 < 1) {
                        st.giveItems(955, 1);
                    } else if (i0 < 4) {
                        st.giveItems(956, 1);
                    } else if (i0 < 36) {
                        st.giveItems(1868, 1);
                    } else if (i0 < 68) {
                        st.giveItems(1884, 1);
                    } else {
                        st.giveItems(1872, 1);
                    }
                    htmltext = "30092-05.htm";
                } else {
                    int i0 = Rnd.get(1000);
                    if (i0 < 10) {
                        st.giveItems(951, 1);
                    } else if (i0 < 40) {
                        st.giveItems(952, 1);
                    } else if (i0 < 60) {
                        st.giveItems(955, 1);
                    } else if (i0 < 260) {
                        st.giveItems(956, 1);
                    } else if (i0 < 445) {
                        st.giveItems(1879, 1);
                    } else if (i0 < 630) {
                        st.giveItems(1880, 1);
                    } else if (i0 < 815) {
                        st.giveItems(1882, 1);
                    } else {
                        st.giveItems(1881, 1);
                    }
                    htmltext = "30092-06.htm";
                    L2Skill skill = SkillTable.getInstance().getInfo(4082, 1);
                    if (skill != null && player.getFirstEffect(skill) == null)
                        skill.getEffects(npc, player);
                }
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int reward;
        String htmltext = Quest.getNoQuestMsg();
        QuestState st = player.getQuestState("Q365_DevilsLegacy");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 39) ? "30095-00.htm" : "30095-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30095:
                        if (!st.hasQuestItems(5873)) {
                            htmltext = "30095-03.htm";
                            break;
                        }
                        htmltext = "30095-05.htm";
                        reward = st.getQuestItemsCount(5873) * 400;
                        st.takeItems(5873, -1);
                        st.rewardItems(57, reward + 19800);
                        break;
                    case 30092:
                        htmltext = "30092-01.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(5873, 1, 0, (npc.getNpcId() == 20836) ? 360000 : 520000);
        return null;
    }
}
