package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q660_AidingTheFloranVillage extends Quest {
    private static final String qn = "Q660_AidingTheFloranVillage";

    private static final int MARIA = 30608;

    private static final int ALEX = 30291;

    private static final int WATCHING_EYES = 8074;

    private static final int GOLEM_SHARD = 8075;

    private static final int LIZARDMEN_SCALE = 8076;

    private static final int PLAIN_WATCHMAN = 21102;

    private static final int ROCK_GOLEM = 21103;

    private static final int LIZARDMEN_SUPPLIER = 21104;

    private static final int LIZARDMEN_AGENT = 21105;

    private static final int CURSED_SEER = 21106;

    private static final int LIZARDMEN_COMMANDER = 21107;

    private static final int LIZARDMEN_SHAMAN = 20781;

    private static final int ADENA = 57;

    private static final int ENCHANT_WEAPON_D = 955;

    private static final int ENCHANT_ARMOR_D = 956;

    public Q660_AidingTheFloranVillage() {
        super(660, "Aiding the Floran Village");
        setItemsIds(8074, 8076, 8075);
        addStartNpc(30608, 30291);
        addTalkId(30608, 30291);
        addKillId(21106, 21102, 21103, 20781, 21104, 21107, 21105);
    }

    private static boolean verifyAndRemoveItems(QuestState st, int numberToVerify) {
        int eyes = st.getQuestItemsCount(8074);
        int scale = st.getQuestItemsCount(8076);
        int shard = st.getQuestItemsCount(8075);
        if (eyes + scale + shard < numberToVerify)
            return false;
        if (eyes >= numberToVerify) {
            st.takeItems(8074, numberToVerify);
        } else {
            int currentNumber = numberToVerify - eyes;
            st.takeItems(8074, -1);
            if (scale >= currentNumber) {
                st.takeItems(8076, currentNumber);
            } else {
                currentNumber -= scale;
                st.takeItems(8076, -1);
                st.takeItems(8075, currentNumber);
            }
        }
        return true;
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q660_AidingTheFloranVillage");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30608-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30291-02.htm")) {
            if (player.getLevel() < 30) {
                htmltext = "30291-02a.htm";
            } else {
                st.setState((byte) 1);
                st.set("cond", "2");
                st.playSound("ItemSound.quest_accept");
            }
        } else if (event.equalsIgnoreCase("30291-05.htm")) {
            int count = st.getQuestItemsCount(8074) + st.getQuestItemsCount(8076) + st.getQuestItemsCount(8075);
            if (count == 0) {
                htmltext = "30291-05a.htm";
            } else {
                st.takeItems(8075, -1);
                st.takeItems(8076, -1);
                st.takeItems(8074, -1);
                st.rewardItems(57, count * 100 + ((count >= 45) ? 9000 : 0));
            }
        } else if (event.equalsIgnoreCase("30291-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30291-11.htm")) {
            if (!verifyAndRemoveItems(st, 100)) {
                htmltext = "30291-11a.htm";
            } else if (Rnd.get(10) < 8) {
                st.rewardItems(57, 1000);
            } else {
                st.rewardItems(57, 13000);
                st.rewardItems(956, 1);
            }
        } else if (event.equalsIgnoreCase("30291-12.htm")) {
            if (!verifyAndRemoveItems(st, 200)) {
                htmltext = "30291-12a.htm";
            } else {
                int luck = Rnd.get(15);
                if (luck < 8) {
                    st.rewardItems(57, 2000);
                } else if (luck < 12) {
                    st.rewardItems(57, 20000);
                    st.rewardItems(956, 1);
                } else {
                    st.rewardItems(955, 1);
                }
            }
        } else if (event.equalsIgnoreCase("30291-13.htm")) {
            if (!verifyAndRemoveItems(st, 500)) {
                htmltext = "30291-13a.htm";
            } else if (Rnd.get(10) < 8) {
                st.rewardItems(57, 5000);
            } else {
                st.rewardItems(57, 45000);
                st.rewardItems(955, 1);
            }
        } else if (event.equalsIgnoreCase("30291-17.htm")) {
            int count = st.getQuestItemsCount(8074) + st.getQuestItemsCount(8076) + st.getQuestItemsCount(8075);
            if (count != 0) {
                htmltext = "30291-17a.htm";
                st.takeItems(8074, -1);
                st.takeItems(8076, -1);
                st.takeItems(8075, -1);
                st.rewardItems(57, count * 100 + ((count >= 45) ? 9000 : 0));
            }
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q660_AidingTheFloranVillage");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                switch (npc.getNpcId()) {
                    case 30608:
                        htmltext = (player.getLevel() < 30) ? "30608-01.htm" : "30608-02.htm";
                        break;
                    case 30291:
                        htmltext = "30291-01.htm";
                        break;
                }
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30608:
                        htmltext = "30608-06.htm";
                        break;
                    case 30291:
                        cond = st.getInt("cond");
                        if (cond == 1) {
                            htmltext = "30291-03.htm";
                            st.set("cond", "2");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 2)
                            htmltext = st.hasAtLeastOneQuestItem(8074, 8076, 8075) ? "30291-04.htm" : "30291-05a.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "2");
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 21102:
            case 21106:
                st.dropItems(8074, 1, 0, 790000);
                break;
            case 21103:
                st.dropItems(8075, 1, 0, 750000);
                break;
            case 20781:
            case 21104:
            case 21105:
            case 21107:
                st.dropItems(8076, 1, 0, 670000);
                break;
        }
        return null;
    }
}
