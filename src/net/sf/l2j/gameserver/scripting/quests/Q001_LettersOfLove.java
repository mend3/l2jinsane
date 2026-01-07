package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q001_LettersOfLove extends Quest {
    private static final String qn = "Q001_LettersOfLove";

    private static final int DARIN = 30048;

    private static final int ROXXY = 30006;

    private static final int BAULRO = 30033;

    private static final int DARIN_LETTER = 687;

    private static final int ROXXY_KERCHIEF = 688;

    private static final int DARIN_RECEIPT = 1079;

    private static final int BAULRO_POTION = 1080;

    private static final int NECKLACE = 906;

    public Q001_LettersOfLove() {
        super(1, "Letters of Love");
        setItemsIds(687, 688, 1079, 1080);
        addStartNpc(30048);
        addTalkId(30048, 30006, 30033);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q001_LettersOfLove");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30048-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(687, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q001_LettersOfLove");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 2) ? "30048-01.htm" : "30048-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30048:
                        if (cond == 1) {
                            htmltext = "30048-07.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30048-08.htm";
                            st.set("cond", "3");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(688, 1);
                            st.giveItems(1079, 1);
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30048-09.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30048-10.htm";
                            st.takeItems(1080, 1);
                            st.giveItems(906, 1);
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(false);
                        }
                        break;
                    case 30006:
                        if (cond == 1) {
                            htmltext = "30006-01.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(687, 1);
                            st.giveItems(688, 1);
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30006-02.htm";
                            break;
                        }
                        if (cond > 2)
                            htmltext = "30006-03.htm";
                        break;
                    case 30033:
                        if (cond == 3) {
                            htmltext = "30033-01.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1079, 1);
                            st.giveItems(1080, 1);
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30033-02.htm";
                        break;
                }
                break;
            case 2:
                htmltext = getAlreadyCompletedMsg();
                break;
        }
        return htmltext;
    }
}
