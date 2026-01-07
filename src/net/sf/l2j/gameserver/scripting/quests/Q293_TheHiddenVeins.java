package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q293_TheHiddenVeins extends Quest {
    private static final String qn = "Q293_TheHiddenVeins";

    private static final int CHRYSOLITE_ORE = 1488;

    private static final int TORN_MAP_FRAGMENT = 1489;

    private static final int HIDDEN_VEIN_MAP = 1490;

    private static final int SOULSHOT_FOR_BEGINNERS = 5789;

    private static final int FILAUR = 30535;

    private static final int CHINCHIRIN = 30539;

    private static final int UTUKU_ORC = 20446;

    private static final int UTUKU_ARCHER = 20447;

    private static final int UTUKU_GRUNT = 20448;

    public Q293_TheHiddenVeins() {
        super(293, "The Hidden Veins");
        setItemsIds(1488, 1489, 1490);
        addStartNpc(30535);
        addTalkId(30535, 30539);
        addKillId(20446, 20447, 20448);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q293_TheHiddenVeins");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30535-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30535-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30539-02.htm")) {
            if (st.getQuestItemsCount(1489) >= 4) {
                htmltext = "30539-03.htm";
                st.playSound("ItemSound.quest_itemget");
                st.takeItems(1489, 4);
                st.giveItems(1490, 1);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int chrysoliteOres, hiddenVeinMaps, reward;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q293_TheHiddenVeins");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.DWARF) {
                    htmltext = "30535-00.htm";
                    break;
                }
                if (player.getLevel() < 6) {
                    htmltext = "30535-01.htm";
                    break;
                }
                htmltext = "30535-02.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30535:
                        chrysoliteOres = st.getQuestItemsCount(1488);
                        hiddenVeinMaps = st.getQuestItemsCount(1490);
                        if (chrysoliteOres + hiddenVeinMaps == 0) {
                            htmltext = "30535-04.htm";
                            break;
                        }
                        if (hiddenVeinMaps > 0) {
                            if (chrysoliteOres > 0) {
                                htmltext = "30535-09.htm";
                            } else {
                                htmltext = "30535-08.htm";
                            }
                        } else {
                            htmltext = "30535-05.htm";
                        }
                        reward = chrysoliteOres * 5 + hiddenVeinMaps * 500 + ((chrysoliteOres >= 10) ? 2000 : 0);
                        st.takeItems(1488, -1);
                        st.takeItems(1490, -1);
                        st.rewardItems(57, reward);
                        if (player.isNewbie() && st.getInt("Reward") == 0) {
                            st.giveItems(5789, 6000);
                            st.playTutorialVoice("tutorial_voice_026");
                            st.set("Reward", "1");
                        }
                        break;
                    case 30539:
                        htmltext = "30539-01.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int chance = Rnd.get(100);
        if (chance > 50) {
            st.dropItemsAlways(1488, 1, 0);
        } else if (chance < 5) {
            st.dropItemsAlways(1489, 1, 0);
        }
        return null;
    }
}
