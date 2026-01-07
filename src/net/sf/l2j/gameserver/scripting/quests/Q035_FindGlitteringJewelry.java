package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q035_FindGlitteringJewelry extends Quest {
    private static final String qn = "Q035_FindGlitteringJewelry";

    private static final int ELLIE = 30091;

    private static final int FELTON = 30879;

    private static final int ROUGH_JEWEL = 7162;

    private static final int ORIHARUKON = 1893;

    private static final int SILVER_NUGGET = 1873;

    private static final int THONS = 4044;

    private static final int JEWEL_BOX = 7077;

    public Q035_FindGlitteringJewelry() {
        super(35, "Find Glittering Jewelry");
        setItemsIds(7162);
        addStartNpc(30091);
        addTalkId(30091, 30879);
        addKillId(20135);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q035_FindGlitteringJewelry");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30091-1.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30879-1.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30091-3.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7162, 10);
        } else if (event.equalsIgnoreCase("30091-5.htm")) {
            if (st.getQuestItemsCount(1893) >= 5 && st.getQuestItemsCount(1873) >= 500 && st.getQuestItemsCount(4044) >= 150) {
                st.takeItems(1893, 5);
                st.takeItems(1873, 500);
                st.takeItems(4044, 150);
                st.giveItems(7077, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(false);
            } else {
                htmltext = "30091-4a.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q035_FindGlitteringJewelry");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 60) {
                    QuestState fwear = player.getQuestState("Q037_MakeFormalWear");
                    if (fwear != null && fwear.getInt("cond") == 6) {
                        htmltext = "30091-0.htm";
                        break;
                    }
                    htmltext = "30091-0a.htm";
                    break;
                }
                htmltext = "30091-0b.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30091:
                        if (cond == 1 || cond == 2) {
                            htmltext = "30091-1a.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30091-2.htm";
                            break;
                        }
                        if (cond == 4)
                            htmltext = (st.getQuestItemsCount(1893) >= 5 && st.getQuestItemsCount(1873) >= 500 && st.getQuestItemsCount(4044) >= 150) ? "30091-4.htm" : "30091-4a.htm";
                        break;
                    case 30879:
                        if (cond == 1) {
                            htmltext = "30879-0.htm";
                            break;
                        }
                        if (cond > 1)
                            htmltext = "30879-1a.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerCondition(player, npc, "cond", "2");
        if (st == null)
            return null;
        if (st.dropItems(7162, 1, 10, 500000))
            st.set("cond", "3");
        return null;
    }
}
