package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q235_MimirsElixir extends Quest {
    private static final String qn = "Q235_MimirsElixir";

    private static final int STAR_OF_DESTINY = 5011;

    private static final int PURE_SILVER = 6320;

    private static final int TRUE_GOLD = 6321;

    private static final int SAGE_STONE = 6322;

    private static final int BLOOD_FIRE = 6318;

    private static final int MIMIR_ELIXIR = 6319;

    private static final int MAGISTER_MIXING_STONE = 5905;

    private static final int SCROLL_ENCHANT_WEAPON_A = 729;

    private static final int JOAN = 30718;

    private static final int LADD = 30721;

    private static final int MIXING_URN = 31149;

    public Q235_MimirsElixir() {
        super(235, "Mimir's Elixir");
        setItemsIds(6320, 6321, 6322, 6318, 5905, 6319);
        addStartNpc(30721);
        addTalkId(30721, 30718, 31149);
        addKillId(20965, 21090);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q235_MimirsElixir");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30721-06.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30721-12.htm") && st.hasQuestItems(6321)) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(5905, 1);
        } else if (event.equalsIgnoreCase("30721-16.htm") && st.hasQuestItems(6319)) {
            player.broadcastPacket(new MagicSkillUse(player, player, 4339, 1, 1, 1));
            st.takeItems(5905, -1);
            st.takeItems(6319, -1);
            st.takeItems(5011, -1);
            st.giveItems(729, 1);
            player.broadcastPacket(new SocialAction(player, 3));
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
        } else if (event.equalsIgnoreCase("30718-03.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("31149-02.htm")) {
            if (!st.hasQuestItems(5905))
                htmltext = "31149-havent.htm";
        } else if (event.equalsIgnoreCase("31149-03.htm")) {
            if (!st.hasQuestItems(5905, 6320))
                htmltext = "31149-havent.htm";
        } else if (event.equalsIgnoreCase("31149-05.htm")) {
            if (!st.hasQuestItems(5905, 6320, 6321))
                htmltext = "31149-havent.htm";
        } else if (event.equalsIgnoreCase("31149-07.htm")) {
            if (!st.hasQuestItems(5905, 6320, 6321, 6318))
                htmltext = "31149-havent.htm";
        } else if (event.equalsIgnoreCase("31149-success.htm")) {
            if (st.hasQuestItems(5905, 6320, 6321, 6318)) {
                st.set("cond", "8");
                st.playSound("ItemSound.quest_middle");
                st.takeItems(6320, -1);
                st.takeItems(6321, -1);
                st.takeItems(6318, -1);
                st.giveItems(6319, 1);
            } else {
                htmltext = "31149-havent.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q235_MimirsElixir");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 75) {
                    htmltext = "30721-01b.htm";
                    break;
                }
                if (!st.hasQuestItems(5011)) {
                    htmltext = "30721-01a.htm";
                    break;
                }
                htmltext = "30721-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30721:
                        if (cond == 1) {
                            if (st.hasQuestItems(6320)) {
                                htmltext = "30721-08.htm";
                                st.set("cond", "2");
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            htmltext = "30721-07.htm";
                            break;
                        }
                        if (cond < 5) {
                            htmltext = "30721-10.htm";
                            break;
                        }
                        if (cond == 5 && st.hasQuestItems(6321)) {
                            htmltext = "30721-11.htm";
                            break;
                        }
                        if (cond == 6 || cond == 7) {
                            htmltext = "30721-13.htm";
                            break;
                        }
                        if (cond == 8 && st.hasQuestItems(6319))
                            htmltext = "30721-14.htm";
                        break;
                    case 30718:
                        if (cond == 2) {
                            htmltext = "30718-01.htm";
                            break;
                        }
                        if (cond == 3) {
                            htmltext = "30718-04.htm";
                            break;
                        }
                        if (cond == 4 && st.hasQuestItems(6322)) {
                            htmltext = "30718-05.htm";
                            st.set("cond", "5");
                            st.playSound("ItemSound.quest_middle");
                            st.takeItems(6322, -1);
                            st.giveItems(6321, 1);
                            break;
                        }
                        if (cond > 4)
                            htmltext = "30718-06.htm";
                        break;
                    case 31149:
                        htmltext = "31149-01.htm";
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
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 20965:
                if (st.getInt("cond") == 3 && st.dropItems(6322, 1, 1, 200000))
                    st.set("cond", "4");
                break;
            case 21090:
                if (st.getInt("cond") == 6 && st.dropItems(6318, 1, 1, 200000))
                    st.set("cond", "7");
                break;
        }
        return null;
    }
}
