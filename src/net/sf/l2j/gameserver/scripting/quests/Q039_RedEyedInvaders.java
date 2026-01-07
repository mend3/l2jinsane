package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q039_RedEyedInvaders extends Quest {
    private static final String qn = "Q039_RedEyedInvaders";

    private static final int BABENCO = 30334;

    private static final int BATHIS = 30332;

    private static final int MAILLE_LIZARDMAN = 20919;

    private static final int MAILLE_LIZARDMAN_SCOUT = 20920;

    private static final int MAILLE_LIZARDMAN_GUARD = 20921;

    private static final int ARANEID = 20925;

    private static final int BLACK_BONE_NECKLACE = 7178;

    private static final int RED_BONE_NECKLACE = 7179;

    private static final int INCENSE_POUCH = 7180;

    private static final int GEM_OF_MAILLE = 7181;

    private static final Map<Integer, int[]> FIRST_DP = new HashMap<>();

    private static final Map<Integer, int[]> SECOND_DP = new HashMap<>();

    private static final int GREEN_COLORED_LURE_HG = 6521;

    private static final int BABY_DUCK_RODE = 6529;

    private static final int FISHING_SHOT_NG = 6535;

    public Q039_RedEyedInvaders() {
        super(39, "Red-Eyed Invaders");
        FIRST_DP.put(Integer.valueOf(20921), new int[]{7179, 7178});
        FIRST_DP.put(Integer.valueOf(20919), new int[]{7178, 7179});
        FIRST_DP.put(Integer.valueOf(20920), new int[]{7178, 7179});
        SECOND_DP.put(Integer.valueOf(20925), new int[]{7181, 7180, 500000});
        SECOND_DP.put(Integer.valueOf(20921), new int[]{7180, 7181, 300000});
        SECOND_DP.put(Integer.valueOf(20920), new int[]{7180, 7181, 250000});
        setItemsIds(7178, 7179, 7180, 7181);
        addStartNpc(30334);
        addTalkId(30334, 30332);
        addKillId(20919, 20920, 20921, 20925);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q039_RedEyedInvaders");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30334-1.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30332-1.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30332-3.htm")) {
            st.set("cond", "4");
            st.takeItems(7178, -1);
            st.takeItems(7179, -1);
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30332-5.htm")) {
            st.takeItems(7180, -1);
            st.takeItems(7181, -1);
            st.giveItems(6521, 60);
            st.giveItems(6529, 1);
            st.giveItems(6535, 500);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q039_RedEyedInvaders");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 20) ? "30334-2.htm" : "30334-0.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30334:
                        htmltext = "30334-3.htm";
                        break;
                    case 30332:
                        if (cond == 1) {
                            htmltext = "30332-0.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30332-2a.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30332-2.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30332-3a.htm";
                            break;
                        }
                        if (cond == 5)
                            htmltext = "30332-4.htm";
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
        int npcId = npc.getNpcId();
        QuestState st = getRandomPartyMember(player, npc, "2");
        if (st != null && npcId != 20925) {
            int[] list = FIRST_DP.get(Integer.valueOf(npcId));
            if (st.dropItems(list[0], 1, 100, 500000) && st.getQuestItemsCount(list[1]) == 100)
                st.set("cond", "3");
        } else {
            st = getRandomPartyMember(player, npc, "4");
            if (st != null && npcId != 20919) {
                int[] list = SECOND_DP.get(Integer.valueOf(npcId));
                if (st.dropItems(list[0], 1, 30, list[2]) && st.getQuestItemsCount(list[1]) == 30)
                    st.set("cond", "5");
            }
        }
        return null;
    }
}
