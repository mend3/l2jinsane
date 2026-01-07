package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q413_PathToAShillienOracle extends Quest {
    private static final String qn = "Q413_PathToAShillienOracle";

    private static final int SIDRA_LETTER = 1262;

    private static final int BLANK_SHEET = 1263;

    private static final int BLOODY_RUNE = 1264;

    private static final int GARMIEL_BOOK = 1265;

    private static final int PRAYER_OF_ADONIUS = 1266;

    private static final int PENITENT_MARK = 1267;

    private static final int ASHEN_BONES = 1268;

    private static final int ANDARIEL_BOOK = 1269;

    private static final int ORB_OF_ABYSS = 1270;

    private static final int SIDRA = 30330;

    private static final int ADONIUS = 30375;

    private static final int TALBOT = 30377;

    public Q413_PathToAShillienOracle() {
        super(413, "Path to a Shillien Oracle");
        setItemsIds(1262, 1263, 1264, 1265, 1266, 1267, 1268, 1269);
        addStartNpc(30330);
        addTalkId(30330, 30375, 30377);
        addKillId(20776, 20457, 20458, 20514, 20515);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q413_PathToAShillienOracle");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30330-05.htm")) {
            if (player.getClassId() != ClassId.DARK_MYSTIC) {
                htmltext = (player.getClassId() == ClassId.SHILLIEN_ORACLE) ? "30330-02a.htm" : "30330-03.htm";
            } else if (player.getLevel() < 19) {
                htmltext = "30330-02.htm";
            } else if (st.hasQuestItems(1270)) {
                htmltext = "30330-04.htm";
            }
        } else if (event.equalsIgnoreCase("30330-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1262, 1);
        } else if (event.equalsIgnoreCase("30377-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1262, 1);
            st.giveItems(1263, 5);
        } else if (event.equalsIgnoreCase("30375-04.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(1266, 1);
            st.giveItems(1267, 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q413_PathToAShillienOracle");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = "30330-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30330:
                        if (cond == 1) {
                            htmltext = "30330-07.htm";
                            break;
                        }
                        if (cond > 1 && cond < 4) {
                            htmltext = "30330-08.htm";
                            break;
                        }
                        if (cond > 3 && cond < 7) {
                            htmltext = "30330-09.htm";
                            break;
                        }
                        if (cond == 7) {
                            htmltext = "30330-10.htm";
                            st.takeItems(1269, 1);
                            st.takeItems(1265, 1);
                            st.giveItems(1270, 1);
                            st.rewardExpAndSp(3200L, 3120);
                            player.broadcastPacket(new SocialAction(player, 3));
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                        }
                        break;
                    case 30377:
                        if (cond == 1) {
                            htmltext = "30377-01.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = st.hasQuestItems(1264) ? "30377-04.htm" : "30377-03.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30377-05.htm";
                            st.set("cond", "4");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1264, -1);
                            st.giveItems(1265, 1);
                            st.giveItems(1266, 1);
                            break;
                        }
                        if (cond > 3 && cond < 7) {
                            htmltext = "30377-06.htm";
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30377-07.htm";
                        break;
                    case 30375:
                        if (cond == 4) {
                            htmltext = "30375-01.htm";
                            break;
                        }
                        if (cond == 5) {
                            htmltext = st.hasQuestItems(1268) ? "30375-05.htm" : "30375-06.htm";
                            break;
                        }
                        if (cond == 6) {
                            htmltext = "30375-07.htm";
                            st.set("cond", "7");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(1268, -1);
                            st.takeItems(1267, -1);
                            st.giveItems(1269, 1);
                            break;
                        }
                        if (cond == 7)
                            htmltext = "30375-08.htm";
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
        if (npc.getNpcId() == 20776) {
            if (st.getInt("cond") == 2) {
                st.takeItems(1263, 1);
                if (st.dropItemsAlways(1264, 1, 5))
                    st.set("cond", "3");
            }
        } else if (st.getInt("cond") == 5 && st.dropItemsAlways(1268, 1, 10)) {
            st.set("cond", "6");
        }
        return null;
    }
}
