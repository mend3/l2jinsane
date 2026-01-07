package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExPlayScene;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q127_KamaelAWindowToTheFuture extends Quest {
    private static final String qn = "Q127_KamaelAWindowToTheFuture";

    private static final int DOMINIC = 31350;

    private static final int KLAUS = 30187;

    private static final int ALDER = 32092;

    private static final int AKLAN = 31288;

    private static final int OLTLIN = 30862;

    private static final int JURIS = 30113;

    private static final int RODEMAI = 30756;

    private static final int MARK_DOMINIC = 8939;

    private static final int MARK_HUMAN = 8940;

    private static final int MARK_DWARF = 8941;

    private static final int MARK_ORC = 8944;

    private static final int MARK_DELF = 8943;

    private static final int MARK_ELF = 8942;

    public Q127_KamaelAWindowToTheFuture() {
        super(127, "Kamael: A Window to the Future");
        setItemsIds(8939, 8940, 8941, 8944, 8943, 8942);
        addStartNpc(31350);
        addTalkId(31350, 30187, 32092, 31288, 30862, 30113, 30756);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q127_KamaelAWindowToTheFuture");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31350-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(8939, 1);
        } else if (event.equalsIgnoreCase("31350-06.htm")) {
            st.takeItems(8940, -1);
            st.takeItems(8941, -1);
            st.takeItems(8942, -1);
            st.takeItems(8943, -1);
            st.takeItems(8944, -1);
            st.takeItems(8939, -1);
            st.rewardItems(57, 159100);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        } else if (event.equalsIgnoreCase("30187-06.htm")) {
            st.set("cond", "2");
        } else if (event.equalsIgnoreCase("30187-08.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8940, 1);
        } else if (event.equalsIgnoreCase("32092-05.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8941, 1);
        } else if (event.equalsIgnoreCase("31288-04.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8944, 1);
        } else if (event.equalsIgnoreCase("30862-04.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8943, 1);
        } else if (event.equalsIgnoreCase("30113-04.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(8942, 1);
        } else {
            if (event.equalsIgnoreCase("kamaelstory")) {
                st.set("cond", "8");
                st.playSound("ItemSound.quest_middle");
                player.sendPacket(ExPlayScene.STATIC_PACKET);
                return null;
            }
            if (event.equalsIgnoreCase("30756-05.htm")) {
                st.set("cond", "9");
                st.playSound("ItemSound.quest_middle");
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q127_KamaelAWindowToTheFuture");
        if (st == null)
            return htmltext;
        npc.getNpcId();
        int cond = st.getInt("cond");
        switch (st.getState()) {
            case 0:
                htmltext = "31350-01.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30187:
                        if (cond == 1) {
                            htmltext = "30187-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "30187-06.htm";
                        break;
                    case 32092:
                        if (cond == 3)
                            htmltext = "32092-01.htm";
                        break;
                    case 31288:
                        if (cond == 4)
                            htmltext = "31288-01.htm";
                        break;
                    case 30862:
                        if (cond == 5)
                            htmltext = "30862-01.htm";
                        break;
                    case 30113:
                        if (cond == 6)
                            htmltext = "30113-01.htm";
                        break;
                    case 30756:
                        if (cond == 7) {
                            htmltext = "30756-01.htm";
                            break;
                        }
                        if (cond == 8)
                            htmltext = "30756-04.htm";
                        break;
                    case 31350:
                        if (cond == 9)
                            htmltext = "31350-05.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                return htmltext;
        }
        return htmltext;
    }
}
