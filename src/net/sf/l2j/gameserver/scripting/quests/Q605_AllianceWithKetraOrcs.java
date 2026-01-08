package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q605_AllianceWithKetraOrcs extends Quest {
    private static final String qn = "Q605_AllianceWithKetraOrcs";

    private static final String qn2 = "Q606_WarWithVarkaSilenos";

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    private static final Map<Integer, Integer> CHANCES_MANE = new HashMap<>();

    private static final int VARKA_BADGE_SOLDIER = 7216;

    private static final int VARKA_BADGE_OFFICER = 7217;

    private static final int VARKA_BADGE_CAPTAIN = 7218;

    private static final int KETRA_ALLIANCE_1 = 7211;

    private static final int KETRA_ALLIANCE_2 = 7212;

    private static final int KETRA_ALLIANCE_3 = 7213;

    private static final int KETRA_ALLIANCE_4 = 7214;

    private static final int KETRA_ALLIANCE_5 = 7215;

    private static final int TOTEM_OF_VALOR = 7219;

    private static final int TOTEM_OF_WISDOM = 7220;

    private static final int VARKA_MANE = 7233;

    public Q605_AllianceWithKetraOrcs() {
        super(605, "Alliance with Ketra Orcs");
        CHANCES.put(21350, 500000);
        CHANCES.put(21351, 500000);
        CHANCES.put(21353, 509000);
        CHANCES.put(21354, 521000);
        CHANCES.put(21355, 519000);
        CHANCES.put(21357, 500000);
        CHANCES.put(21358, 500000);
        CHANCES.put(21360, 509000);
        CHANCES.put(21361, 518000);
        CHANCES.put(21362, 518000);
        CHANCES.put(21364, 527000);
        CHANCES.put(21365, 500000);
        CHANCES.put(21366, 500000);
        CHANCES.put(21368, 508000);
        CHANCES.put(21369, 628000);
        CHANCES.put(21370, 604000);
        CHANCES.put(21371, 627000);
        CHANCES.put(21372, 604000);
        CHANCES.put(21373, 649000);
        CHANCES.put(21374, 626000);
        CHANCES.put(21375, 626000);
        CHANCES_MANE.put(21350, 500000);
        CHANCES_MANE.put(21353, 510000);
        CHANCES_MANE.put(21354, 522000);
        CHANCES_MANE.put(21355, 519000);
        CHANCES_MANE.put(21357, 529000);
        CHANCES_MANE.put(21358, 529000);
        CHANCES_MANE.put(21360, 539000);
        CHANCES_MANE.put(21362, 548000);
        CHANCES_MANE.put(21364, 558000);
        CHANCES_MANE.put(21365, 568000);
        CHANCES_MANE.put(21366, 568000);
        CHANCES_MANE.put(21368, 568000);
        CHANCES_MANE.put(21369, 664000);
        CHANCES_MANE.put(21371, 713000);
        CHANCES_MANE.put(21373, 738000);
        setItemsIds(7216, 7217, 7218);
        addStartNpc(31371);
        addTalkId(31371);
        for (int mobs : CHANCES.keySet()) {
            addKillId(mobs);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q605_AllianceWithKetraOrcs");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31371-03a.htm")) {
            if (player.isAlliedWithVarka()) {
                htmltext = "31371-02a.htm";
            } else {
                st.setState((byte) 1);
                st.playSound("ItemSound.quest_accept");
                for (int i = 7211; i <= 7215; i++) {
                    if (st.hasQuestItems(i)) {
                        st.set("cond", String.valueOf(i - 7209));
                        player.setAllianceWithVarkaKetra(i - 7210);
                        return "31371-0" + (i - 7207) + ".htm";
                    }
                }
                st.set("cond", "1");
            }
        } else if (event.equalsIgnoreCase("31371-10-1.htm")) {
            if (st.getQuestItemsCount(7216) >= 100) {
                st.set("cond", "2");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7216, -1);
                st.giveItems(7211, 1);
                player.setAllianceWithVarkaKetra(1);
            } else {
                htmltext = "31371-03b.htm";
            }
        } else if (event.equalsIgnoreCase("31371-10-2.htm")) {
            if (st.getQuestItemsCount(7216) >= 200 && st.getQuestItemsCount(7217) >= 100) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7216, -1);
                st.takeItems(7217, -1);
                st.takeItems(7211, -1);
                st.giveItems(7212, 1);
                player.setAllianceWithVarkaKetra(2);
            } else {
                htmltext = "31371-12.htm";
            }
        } else if (event.equalsIgnoreCase("31371-10-3.htm")) {
            if (st.getQuestItemsCount(7216) >= 300 && st.getQuestItemsCount(7217) >= 200 && st.getQuestItemsCount(7218) >= 100) {
                st.set("cond", "4");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7216, -1);
                st.takeItems(7217, -1);
                st.takeItems(7218, -1);
                st.takeItems(7212, -1);
                st.giveItems(7213, 1);
                player.setAllianceWithVarkaKetra(3);
            } else {
                htmltext = "31371-15.htm";
            }
        } else if (event.equalsIgnoreCase("31371-10-4.htm")) {
            if (st.getQuestItemsCount(7216) >= 300 && st.getQuestItemsCount(7217) >= 300 && st.getQuestItemsCount(7218) >= 200 && st.getQuestItemsCount(7219) >= 1) {
                st.set("cond", "5");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(7216, -1);
                st.takeItems(7217, -1);
                st.takeItems(7218, -1);
                st.takeItems(7219, -1);
                st.takeItems(7213, -1);
                st.giveItems(7214, 1);
                player.setAllianceWithVarkaKetra(4);
            } else {
                htmltext = "31371-21.htm";
            }
        } else if (event.equalsIgnoreCase("31371-20.htm")) {
            st.takeItems(7211, -1);
            st.takeItems(7212, -1);
            st.takeItems(7213, -1);
            st.takeItems(7214, -1);
            st.takeItems(7215, -1);
            st.takeItems(7219, -1);
            st.takeItems(7220, -1);
            player.setAllianceWithVarkaKetra(0);
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q605_AllianceWithKetraOrcs");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() >= 74) {
                    htmltext = "31371-01.htm";
                    break;
                }
                htmltext = "31371-02b.htm";
                st.exitQuest(true);
                player.setAllianceWithVarkaKetra(0);
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    if (st.getQuestItemsCount(7216) < 100) {
                        htmltext = "31371-03b.htm";
                        break;
                    }
                    htmltext = "31371-09.htm";
                    break;
                }
                if (cond == 2) {
                    if (st.getQuestItemsCount(7216) < 200 || st.getQuestItemsCount(7217) < 100) {
                        htmltext = "31371-12.htm";
                        break;
                    }
                    htmltext = "31371-13.htm";
                    break;
                }
                if (cond == 3) {
                    if (st.getQuestItemsCount(7216) < 300 || st.getQuestItemsCount(7217) < 200 || st.getQuestItemsCount(7218) < 100) {
                        htmltext = "31371-15.htm";
                        break;
                    }
                    htmltext = "31371-16.htm";
                    break;
                }
                if (cond == 4) {
                    if (st.getQuestItemsCount(7216) < 300 || st.getQuestItemsCount(7217) < 300 || st.getQuestItemsCount(7218) < 200 || !st.hasQuestItems(7219)) {
                        htmltext = "31371-21.htm";
                        break;
                    }
                    htmltext = "31371-22.htm";
                    break;
                }
                if (cond == 5) {
                    if (st.getQuestItemsCount(7216) < 400 || st.getQuestItemsCount(7217) < 400 || st.getQuestItemsCount(7218) < 200 || !st.hasQuestItems(7220)) {
                        htmltext = "31371-17.htm";
                        break;
                    }
                    htmltext = "31371-10-5.htm";
                    st.set("cond", "6");
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(7216, 400);
                    st.takeItems(7217, 400);
                    st.takeItems(7218, 200);
                    st.takeItems(7220, -1);
                    st.takeItems(7214, -1);
                    st.giveItems(7215, 1);
                    player.setAllianceWithVarkaKetra(5);
                    break;
                }
                if (cond == 6)
                    htmltext = "31371-08.htm";
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
        QuestState st2 = st.getPlayer().getQuestState("Q606_WarWithVarkaSilenos");
        if (st2 != null && Rnd.nextBoolean() && CHANCES_MANE.containsKey(npcId)) {
            st2.dropItems(7233, 1, 0, CHANCES_MANE.get(npcId));
            return null;
        }
        int cond = st.getInt("cond");
        if (cond == 6)
            return null;
        switch (npcId) {
            case 21350:
            case 21351:
            case 21353:
            case 21354:
            case 21355:
                if (cond == 1) {
                    st.dropItems(7216, 1, 100, CHANCES.get(npcId));
                    break;
                }
                if (cond == 2) {
                    st.dropItems(7216, 1, 200, CHANCES.get(npcId));
                    break;
                }
                if (cond == 3 || cond == 4) {
                    st.dropItems(7216, 1, 300, CHANCES.get(npcId));
                    break;
                }
                if (cond == 5)
                    st.dropItems(7216, 1, 400, CHANCES.get(npcId));
                break;
            case 21357:
            case 21358:
            case 21360:
            case 21361:
            case 21362:
            case 21364:
            case 21369:
            case 21370:
                if (cond == 2) {
                    st.dropItems(7217, 1, 100, CHANCES.get(npcId));
                    break;
                }
                if (cond == 3) {
                    st.dropItems(7217, 1, 200, CHANCES.get(npcId));
                    break;
                }
                if (cond == 4) {
                    st.dropItems(7217, 1, 300, CHANCES.get(npcId));
                    break;
                }
                if (cond == 5)
                    st.dropItems(7217, 1, 400, CHANCES.get(npcId));
                break;
            case 21365:
            case 21366:
            case 21368:
            case 21371:
            case 21372:
            case 21373:
            case 21374:
            case 21375:
                if (cond == 3) {
                    st.dropItems(7218, 1, 100, CHANCES.get(npcId));
                    break;
                }
                if (cond == 4 || cond == 5)
                    st.dropItems(7218, 1, 200, CHANCES.get(npcId));
                break;
        }
        return null;
    }
}
