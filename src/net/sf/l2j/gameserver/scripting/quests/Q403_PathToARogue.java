package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q403_PathToARogue extends Quest {
    private static final String qn = "Q403_PathToARogue";

    private static final int BEZIQUE_LETTER = 1180;

    private static final int NETI_BOW = 1181;

    private static final int NETI_DAGGER = 1182;

    private static final int SPARTOI_BONES = 1183;

    private static final int HORSESHOE_OF_LIGHT = 1184;

    private static final int MOST_WANTED_LIST = 1185;

    private static final int STOLEN_JEWELRY = 1186;

    private static final int STOLEN_TOMES = 1187;

    private static final int STOLEN_RING = 1188;

    private static final int STOLEN_NECKLACE = 1189;

    private static final int BEZIQUE_RECOMMENDATION = 1190;

    private static final int BEZIQUE = 30379;

    private static final int NETI = 30425;

    public Q403_PathToARogue() {
        super(403, "Path to a Rogue");
        setItemsIds(1180, 1181, 1182, 1183, 1184, 1185, 1186, 1187, 1188, 1189);
        addStartNpc(30379);
        addTalkId(30379, 30425);
        addKillId(20035, 20042, 20045, 20051, 20054, 20060, 27038);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q403_PathToARogue");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30379-05.htm")) {
            if (player.getClassId() != ClassId.HUMAN_FIGHTER) {
                htmltext = (player.getClassId() == ClassId.ROGUE) ? "30379-02a.htm" : "30379-02.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30379-02.htm";
            } else if (st.hasQuestItems(1190)) {
                htmltext = "30379-04.htm";
            }
        } else if (event.equalsIgnoreCase("30379-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1180, 1);
        } else if (event.equalsIgnoreCase("30425-05.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1181, 1);
            st.giveItems(1182, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q403_PathToARogue");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30379-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30379:
                        if (cond == 1) {
                            htmltext = "30379-07.htm";
                            break;
                        }
                        if (cond == 2 || cond == 3) {
                            htmltext = "30379-10.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30379-08.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1184, 1);
                            st.giveItems(1185, 1);
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30379-11.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30379-09.htm";
                            st.takeItems(1181, 1);
                            st.takeItems(1182, 1);
                            st.takeItems(1186, 1);
                            st.takeItems(1189, 1);
                            st.takeItems(1188, 1);
                            st.takeItems(1187, 1);
                            st.giveItems(1190, 1);
                            st.rewardExpAndSp(3200L, 1500);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30425:
                        if (cond == 1) {
                            htmltext = "30425-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "30425-06.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30425-07.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1183, 10);
                            st.giveItems(1184, 1);
                            break;
                        }
                        if (cond > 3)
                            htmltext = "30425-08.htm";
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
        int equippedItemId = st.getItemEquipped(7);
        if (equippedItemId != 1181 && equippedItemId != 1182)
            return null;
        switch (npc.getNpcId()) {
            case 20035:
            case 20045:
            case 20051:
                if (st.getInt("cond") == 2 && st.dropItems(1183, 1, 10, 200000))
                    st.set("cond", "3");
                break;
            case 20042:
                if (st.getInt("cond") == 2 && st.dropItems(1183, 1, 10, 300000))
                    st.set("cond", "3");
                break;
            case 20054:
            case 20060:
                if (st.getInt("cond") == 2 && st.dropItems(1183, 1, 10, 800000))
                    st.set("cond", "3");
                break;
            case 27038:
                if (st.getInt("cond") == 5) {
                    int randomItem = Rnd.get(1186, 1189);
                    if (!st.hasQuestItems(randomItem)) {
                        st.giveItems(randomItem, 1);
                        if (st.hasQuestItems(1186, 1187, 1188, 1189)) {
                            st.set("cond", "6");
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
                break;
        }
        return null;
    }
}
