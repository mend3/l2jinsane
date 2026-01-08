package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q611_AllianceWithVarkaSilenos extends Quest {
    private static final String qn = "Q611_AllianceWithVarkaSilenos";

    private static final String qn2 = "Q612_WarWithKetraOrcs";

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    private static final Map<Integer, Integer> CHANCES_MOLAR = new HashMap<>();

    private static final int KETRA_BADGE_SOLDIER = 7226;

    private static final int KETRA_BADGE_OFFICER = 7227;

    private static final int KETRA_BADGE_CAPTAIN = 7228;

    private static final int VARKA_ALLIANCE_1 = 7221;

    private static final int VARKA_ALLIANCE_2 = 7222;

    private static final int VARKA_ALLIANCE_3 = 7223;

    private static final int VARKA_ALLIANCE_4 = 7224;

    private static final int VARKA_ALLIANCE_5 = 7225;

    private static final int VALOR_FEATHER = 7229;

    private static final int WISDOM_FEATHER = 7230;

    private static final int MOLAR_OF_KETRA_ORC = 7234;

    public Q611_AllianceWithVarkaSilenos() {
        super(611, "Alliance with Varka Silenos");
        CHANCES.put(21324, 500000);
        CHANCES.put(21325, 500000);
        CHANCES.put(21327, 509000);
        CHANCES.put(21328, 521000);
        CHANCES.put(21329, 519000);
        CHANCES.put(21331, 500000);
        CHANCES.put(21332, 500000);
        CHANCES.put(21334, 509000);
        CHANCES.put(21335, 518000);
        CHANCES.put(21336, 518000);
        CHANCES.put(21338, 527000);
        CHANCES.put(21339, 500000);
        CHANCES.put(21340, 500000);
        CHANCES.put(21342, 508000);
        CHANCES.put(21343, 628000);
        CHANCES.put(21344, 604000);
        CHANCES.put(21345, 627000);
        CHANCES.put(21346, 604000);
        CHANCES.put(21347, 649000);
        CHANCES.put(21348, 626000);
        CHANCES.put(21349, 626000);
        CHANCES_MOLAR.put(21324, 500000);
        CHANCES_MOLAR.put(21327, 510000);
        CHANCES_MOLAR.put(21328, 522000);
        CHANCES_MOLAR.put(21329, 519000);
        CHANCES_MOLAR.put(21331, 529000);
        CHANCES_MOLAR.put(21332, 529000);
        CHANCES_MOLAR.put(21334, 539000);
        CHANCES_MOLAR.put(21336, 548000);
        CHANCES_MOLAR.put(21338, 558000);
        CHANCES_MOLAR.put(21339, 568000);
        CHANCES_MOLAR.put(21340, 568000);
        CHANCES_MOLAR.put(21342, 578000);
        CHANCES_MOLAR.put(21343, 664000);
        CHANCES_MOLAR.put(21345, 713000);
        CHANCES_MOLAR.put(21347, 738000);
        setItemsIds(7226, 7227, 7228);
        addStartNpc(31378);
        addTalkId(31378);
        for (int mobs : CHANCES.keySet()) {
            addKillId(mobs);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q611_AllianceWithVarkaSilenos");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31378-03a.htm")) {
            if (player.isAlliedWithKetra()) {
                htmltext = "31378-02a.htm";
            } else {
                st.setState((byte) 1);
                st.playSound("ItemSound.quest_accept");
                for (int i = 7221; i <= 7225; i++) {
                    if (st.hasQuestItems(i)) {
                        st.set("cond", String.valueOf(i - 7219));
                        player.setAllianceWithVarkaKetra(7220 - i);
                        return "31378-0" + (i - 7217) + ".htm";
                    }
                }
                st.set("cond", "1");
            }
        } else if (event.equalsIgnoreCase("31378-10-1.htm")) {
            if (st.getQuestItemsCount(7226) >= 100) {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7226, -1);
                st.giveItems(7221, 1);
                player.setAllianceWithVarkaKetra(-1);
            } else {
                htmltext = "31378-03b.htm";
            }
        } else if (event.equalsIgnoreCase("31378-10-2.htm")) {
            if (st.getQuestItemsCount(7226) >= 200 && st.getQuestItemsCount(7227) >= 100) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7226, -1);
                st.takeItems(7227, -1);
                st.takeItems(7221, -1);
                st.giveItems(7222, 1);
                player.setAllianceWithVarkaKetra(-2);
            } else {
                htmltext = "31378-12.htm";
            }
        } else if (event.equalsIgnoreCase("31378-10-3.htm")) {
            if (st.getQuestItemsCount(7226) >= 300 && st.getQuestItemsCount(7227) >= 200 && st.getQuestItemsCount(7228) >= 100) {
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7226, -1);
                st.takeItems(7227, -1);
                st.takeItems(7228, -1);
                st.takeItems(7222, -1);
                st.giveItems(7223, 1);
                player.setAllianceWithVarkaKetra(-3);
            } else {
                htmltext = "31378-15.htm";
            }
        } else if (event.equalsIgnoreCase("31378-10-4.htm")) {
            if (st.getQuestItemsCount(7226) >= 300 && st.getQuestItemsCount(7227) >= 300 && st.getQuestItemsCount(7228) >= 200 && st.getQuestItemsCount(7229) >= 1) {
                st.set("cond", "5");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7226, -1);
                st.takeItems(7227, -1);
                st.takeItems(7228, -1);
                st.takeItems(7229, -1);
                st.takeItems(7223, -1);
                st.giveItems(7224, 1);
                player.setAllianceWithVarkaKetra(-4);
            } else {
                htmltext = "31378-21.htm";
            }
        } else if (event.equalsIgnoreCase("31378-20.htm")) {
            st.takeItems(7221, -1);
            st.takeItems(7222, -1);
            st.takeItems(7223, -1);
            st.takeItems(7224, -1);
            st.takeItems(7225, -1);
            st.takeItems(7229, -1);
            st.takeItems(7230, -1);
            player.setAllianceWithVarkaKetra(0);
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q611_AllianceWithVarkaSilenos");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 74) {
                    htmltext = "31378-01.htm";
                    break;
                }
                htmltext = "31378-02b.htm";
                st.exitQuest(true);
                player.setAllianceWithVarkaKetra(0);
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    if (st.getQuestItemsCount(7226) < 100) {
                        htmltext = "31378-03b.htm";
                        break;
                    }
                    htmltext = "31378-09.htm";
                    break;
                }
                if (cond == 2) {
                    if (st.getQuestItemsCount(7226) < 200 || st.getQuestItemsCount(7227) < 100) {
                        htmltext = "31378-12.htm";
                        break;
                    }
                    htmltext = "31378-13.htm";
                    break;
                }
                if (cond == 3) {
                    if (st.getQuestItemsCount(7226) < 300 || st.getQuestItemsCount(7227) < 200 || st.getQuestItemsCount(7228) < 100) {
                        htmltext = "31378-15.htm";
                        break;
                    }
                    htmltext = "31378-16.htm";
                    break;
                }
                if (cond == 4) {
                    if (st.getQuestItemsCount(7226) < 300 || st.getQuestItemsCount(7227) < 300 || st.getQuestItemsCount(7228) < 200 || !st.hasQuestItems(7229)) {
                        htmltext = "31378-21.htm";
                        break;
                    }
                    htmltext = "31378-22.htm";
                    break;
                }
                if (cond == 5) {
                    if (st.getQuestItemsCount(7226) < 400 || st.getQuestItemsCount(7227) < 400 || st.getQuestItemsCount(7228) < 200 || !st.hasQuestItems(7230)) {
                        htmltext = "31378-17.htm";
                        break;
                    }
                    htmltext = "31378-10-5.htm";
                    st.set("cond", "6");
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(7226, 400);
                    st.takeItems(7227, 400);
                    st.takeItems(7228, 200);
                    st.takeItems(7230, -1);
                    st.takeItems(7224, -1);
                    st.giveItems(7225, 1);
                    player.setAllianceWithVarkaKetra(-5);
                    break;
                }
                if (cond == 6)
                    htmltext = "31378-08.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int npcId = npc.getNpcId();
        QuestState st2 = st.getPlayer().getQuestState("Q612_WarWithKetraOrcs");
        if (st2 != null && Rnd.nextBoolean() && CHANCES_MOLAR.containsKey(npcId)) {
            st2.dropItems(7234, 1, 0, CHANCES_MOLAR.get(npcId));
            return null;
        }
        int cond = st.getInt("cond");
        if (cond == 6)
            return null;
        switch (npcId) {
            case 21324:
            case 21325:
            case 21327:
            case 21328:
            case 21329:
                if (cond == 1) {
                    st.dropItems(7226, 1, 100, CHANCES.get(npcId));
                    break;
                }
                if (cond == 2) {
                    st.dropItems(7226, 1, 200, CHANCES.get(npcId));
                    break;
                }
                if (cond == 3 || cond == 4) {
                    st.dropItems(7226, 1, 300, CHANCES.get(npcId));
                    break;
                }
                if (cond == 5)
                    st.dropItems(7226, 1, 400, CHANCES.get(npcId));
                break;
            case 21331:
            case 21332:
            case 21334:
            case 21335:
            case 21336:
            case 21338:
            case 21343:
            case 21344:
                if (cond == 2) {
                    st.dropItems(7227, 1, 100, CHANCES.get(npcId));
                    break;
                }
                if (cond == 3) {
                    st.dropItems(7227, 1, 200, CHANCES.get(npcId));
                    break;
                }
                if (cond == 4) {
                    st.dropItems(7227, 1, 300, CHANCES.get(npcId));
                    break;
                }
                if (cond == 5)
                    st.dropItems(7227, 1, 400, CHANCES.get(npcId));
                break;
            case 21339:
            case 21340:
            case 21342:
            case 21345:
            case 21346:
            case 21347:
            case 21348:
            case 21349:
                if (cond == 3) {
                    st.dropItems(7228, 1, 100, CHANCES.get(npcId));
                    break;
                }
                if (cond == 4 || cond == 5)
                    st.dropItems(7228, 1, 200, CHANCES.get(npcId));
                break;
        }
        return null;
    }
}
