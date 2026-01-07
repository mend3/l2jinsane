package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q401_PathToAWarrior extends Quest {
    private static final String qn = "Q401_PathToAWarrior";

    private static final int AURON_LETTER = 1138;

    private static final int WARRIOR_GUILD_MARK = 1139;

    private static final int RUSTED_BRONZE_SWORD_1 = 1140;

    private static final int RUSTED_BRONZE_SWORD_2 = 1141;

    private static final int RUSTED_BRONZE_SWORD_3 = 1142;

    private static final int SIMPLON_LETTER = 1143;

    private static final int POISON_SPIDER_LEG = 1144;

    private static final int MEDALLION_OF_WARRIOR = 1145;

    private static final int AURON = 30010;

    private static final int SIMPLON = 30253;

    public Q401_PathToAWarrior() {
        super(401, "Path to a Warrior");
        setItemsIds(1138, 1139, 1140, 1141, 1142, 1143, 1144);
        addStartNpc(30010);
        addTalkId(30010, 30253);
        addKillId(20035, 20038, 20042, 20043);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q401_PathToAWarrior");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30010-05.htm")) {
            if (player.getClassId() != ClassId.HUMAN_FIGHTER) {
                htmltext = (player.getClassId() == ClassId.WARRIOR) ? "30010-03.htm" : "30010-02b.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30010-02.htm";
            } else if (st.hasQuestItems(1145)) {
                htmltext = "30010-04.htm";
            }
        } else if (event.equalsIgnoreCase("30010-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1138, 1);
        } else if (event.equalsIgnoreCase("30253-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1138, 1);
            st.giveItems(1139, 1);
        } else if (event.equalsIgnoreCase("30010-11.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1141, 1);
            st.takeItems(1143, 1);
            st.giveItems(1142, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q401_PathToAWarrior");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30010-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30010:
                        if (cond == 1) {
                            htmltext = "30010-07.htm";
                            break;
                        }
                        if (cond == 2 || cond == 3) {
                            htmltext = "30010-08.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30010-09.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = "30010-12.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30010-13.htm";
                            st.takeItems(1144, -1);
                            st.takeItems(1142, 1);
                            st.giveItems(1145, 1);
                            st.rewardExpAndSp(3200L, 1500);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30253:
                        if (cond == 1) {
                            htmltext = "30253-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            if (!st.hasQuestItems(1140)) {
                                htmltext = "30253-03.htm";
                                break;
                            }
                            if (st.getQuestItemsCount(1140) <= 9)
                                htmltext = "30253-03b.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30253-04.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1140, 10);
                            st.takeItems(1139, 1);
                            st.giveItems(1141, 1);
                            st.giveItems(1143, 1);
                            break;
                        }
                        if (cond == 4)
                            htmltext = "30253-05.htm";
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
        switch (npc.getNpcId()) {
            case 20035:
            case 20042:
                if (st.getInt("cond") == 2 && st.dropItems(1140, 1, 10, 400000))
                    st.set("cond", "3");
                break;
            case 20038:
            case 20043:
                if (st.getInt("cond") == 5 && st.getItemEquipped(7) == 1142 &&
                        st.dropItemsAlways(1144, 1, 20))
                    st.set("cond", "6");
                break;
        }
        return null;
    }
}
