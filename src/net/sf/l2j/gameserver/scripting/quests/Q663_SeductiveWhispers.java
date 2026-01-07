package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q663_SeductiveWhispers extends Quest {
    private static final String qn = "Q663_SeductiveWhispers";

    private static final int WILBERT = 30846;

    private static final int SPIRIT_BEAD = 8766;

    private static final int ADENA = 57;

    private static final int ENCHANT_WEAPON_A = 729;

    private static final int ENCHANT_ARMOR_A = 730;

    private static final int ENCHANT_WEAPON_B = 947;

    private static final int ENCHANT_ARMOR_B = 948;

    private static final int ENCHANT_WEAPON_C = 951;

    private static final int ENCHANT_WEAPON_D = 955;

    private static final int[] RECIPES = new int[]{2353, 4963, 4967, 5000, 5001, 5002, 5004, 5005, 5006, 5007};

    private static final int[] BLADES = new int[]{2115, 4104, 4108, 4114, 4115, 4116, 4118, 4119, 4120, 4121};

    private static final Map<Integer, String> CARDS = new HashMap<>();

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q663_SeductiveWhispers() {
        super(663, "Seductive Whispers");
        CARDS.put(Integer.valueOf(0), "No such card");
        CARDS.put(Integer.valueOf(11), "<font color=\"ff453d\"> Sun Card: 1 </font>");
        CARDS.put(Integer.valueOf(12), "<font color=\"ff453d\"> Sun Card: 2 </font>");
        CARDS.put(Integer.valueOf(13), "<font color=\"ff453d\"> Sun Card: 3 </font>");
        CARDS.put(Integer.valueOf(14), "<font color=\"ff453d\"> Sun Card: 4 </font>");
        CARDS.put(Integer.valueOf(15), "<font color=\"ff453d\"> Sun Card: 5 </font>");
        CARDS.put(Integer.valueOf(21), "<font color=\"fff802\"> Moon Card: 1 </font>");
        CARDS.put(Integer.valueOf(22), "<font color=\"fff802\"> Moon Card: 2 </font>");
        CARDS.put(Integer.valueOf(23), "<font color=\"fff802\"> Moon Card: 3 </font>");
        CARDS.put(Integer.valueOf(24), "<font color=\"fff802\"> Moon Card: 4 </font>");
        CARDS.put(Integer.valueOf(25), "<font color=\"fff802\"> Moon Card: 5 </font>");
        CHANCES.put(Integer.valueOf(20674), Integer.valueOf(807000));
        CHANCES.put(Integer.valueOf(20678), Integer.valueOf(372000));
        CHANCES.put(Integer.valueOf(20954), Integer.valueOf(460000));
        CHANCES.put(Integer.valueOf(20955), Integer.valueOf(537000));
        CHANCES.put(Integer.valueOf(20956), Integer.valueOf(540000));
        CHANCES.put(Integer.valueOf(20957), Integer.valueOf(565000));
        CHANCES.put(Integer.valueOf(20958), Integer.valueOf(425000));
        CHANCES.put(Integer.valueOf(20959), Integer.valueOf(682000));
        CHANCES.put(Integer.valueOf(20960), Integer.valueOf(372000));
        CHANCES.put(Integer.valueOf(20961), Integer.valueOf(547000));
        CHANCES.put(Integer.valueOf(20962), Integer.valueOf(522000));
        CHANCES.put(Integer.valueOf(20963), Integer.valueOf(498000));
        CHANCES.put(Integer.valueOf(20974), Integer.valueOf(1000000));
        CHANCES.put(Integer.valueOf(20975), Integer.valueOf(975000));
        CHANCES.put(Integer.valueOf(20976), Integer.valueOf(825000));
        CHANCES.put(Integer.valueOf(20996), Integer.valueOf(385000));
        CHANCES.put(Integer.valueOf(20997), Integer.valueOf(342000));
        CHANCES.put(Integer.valueOf(20998), Integer.valueOf(377000));
        CHANCES.put(Integer.valueOf(20999), Integer.valueOf(450000));
        CHANCES.put(Integer.valueOf(21000), Integer.valueOf(395000));
        CHANCES.put(Integer.valueOf(21001), Integer.valueOf(535000));
        CHANCES.put(Integer.valueOf(21002), Integer.valueOf(472000));
        CHANCES.put(Integer.valueOf(21006), Integer.valueOf(502000));
        CHANCES.put(Integer.valueOf(21007), Integer.valueOf(540000));
        CHANCES.put(Integer.valueOf(21008), Integer.valueOf(692000));
        CHANCES.put(Integer.valueOf(21009), Integer.valueOf(740000));
        CHANCES.put(Integer.valueOf(21010), Integer.valueOf(595000));
        setItemsIds(8766);
        addStartNpc(30846);
        addTalkId(30846);
        for (Iterator<Integer> iterator = CHANCES.keySet().iterator(); iterator.hasNext(); ) {
            int npcId = iterator.next();
            addKillId(npcId);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q663_SeductiveWhispers");
        if (st == null)
            return htmltext;
        int state = st.getInt("state");
        if (event.equalsIgnoreCase("30846-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("state", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30846-09.htm") && state % 10 <= 4) {
            if (state / 10 < 1) {
                if (st.getQuestItemsCount(8766) >= 50) {
                    st.takeItems(8766, 50);
                    st.set("state", "5");
                } else {
                    htmltext = "30846-10.htm";
                }
            } else {
                st.set("state", String.valueOf(state / 10 * 10 + 5));
                st.set("stateEx", "0");
                htmltext = "30846-09a.htm";
            }
        } else if (event.equalsIgnoreCase("30846-14.htm") && state % 10 == 5 && state / 1000 == 0) {
            int i0 = st.getInt("stateEx");
            int i1 = i0 % 10;
            int i2 = (i0 - i1) / 10;
            int param1 = Rnd.get(2) + 1;
            int param2 = Rnd.get(5) + 1;
            int i5 = state / 10;
            int param3 = param1 * 10 + param2;
            if (param1 == i2) {
                int i3 = param2 + i1;
                if (i3 % 5 == 0 && i3 != 10) {
                    if (state % 100 / 10 >= 7) {
                        st.set("state", "4");
                        st.rewardItems(57, 2384000);
                        st.rewardItems(729, 1);
                        st.rewardItems(730, 1);
                        st.rewardItems(730, 1);
                        htmltext = getHTML("30846-14.htm", i0, param3, player.getName());
                    } else {
                        st.set("state", String.valueOf(state / 10 * 10 + 7));
                        htmltext = getHTML("30846-13.htm", i0, param3, player.getName()).replace("%wincount%", String.valueOf(i5 + 1));
                    }
                } else {
                    st.set("state", String.valueOf(state / 10 * 10 + 6));
                    st.set("stateEx", String.valueOf(param3));
                    htmltext = getHTML("30846-12.htm", i0, param3, player.getName());
                }
            } else if (param2 == 5 || i1 == 5) {
                if (state % 100 / 10 >= 7) {
                    st.set("state", "4");
                    st.rewardItems(57, 2384000);
                    st.rewardItems(729, 1);
                    st.rewardItems(730, 1);
                    st.rewardItems(730, 1);
                    htmltext = getHTML("30846-14.htm", i0, param3, player.getName());
                } else {
                    st.set("state", String.valueOf(state / 10 * 10 + 7));
                    htmltext = getHTML("30846-13.htm", i0, param3, player.getName()).replace("%wincount%", String.valueOf(i5 + 1));
                }
            } else {
                st.set("state", String.valueOf(state / 10 * 10 + 6));
                st.set("stateEx", String.valueOf(param1 * 10 + param2));
                htmltext = getHTML("30846-12.htm", i0, param3, player.getName());
            }
        } else if (event.equalsIgnoreCase("30846-19.htm") && state % 10 == 6 && state / 1000 == 0) {
            int i0 = st.getInt("stateEx");
            int i1 = i0 % 10;
            int i2 = (i0 - i1) / 10;
            int param1 = Rnd.get(2) + 1;
            int param2 = Rnd.get(5) + 1;
            int param3 = param1 * 10 + param2;
            if (param1 == i2) {
                int i3 = param1 + i1;
                if (i3 % 5 == 0 && i3 != 10) {
                    st.set("state", "1");
                    st.set("stateEx", "0");
                    htmltext = getHTML("30846-19.htm", i0, param3, player.getName());
                } else {
                    st.set("state", String.valueOf(state / 10 * 10 + 5));
                    st.set("stateEx", String.valueOf(param3));
                    htmltext = getHTML("30846-18.htm", i0, param3, player.getName());
                }
            } else if (param2 == 5 || i1 == 5) {
                st.set("state", "1");
                htmltext = getHTML("30846-19.htm", i0, param3, player.getName());
            } else {
                st.set("state", String.valueOf(state / 10 * 10 + 5));
                st.set("stateEx", String.valueOf(param3));
                htmltext = getHTML("30846-18.htm", i0, param3, player.getName());
            }
        } else if (event.equalsIgnoreCase("30846-20.htm") && state % 10 == 7 && state / 1000 == 0) {
            st.set("state", String.valueOf((state / 10 + 1) * 10 + 4));
            st.set("stateEx", "0");
        } else if (event.equalsIgnoreCase("30846-21.htm") && state % 10 == 7 && state / 1000 == 0) {
            int round = state / 10;
            if (round == 0) {
                st.rewardItems(57, 40000);
            } else if (round == 1) {
                st.rewardItems(57, 80000);
            } else if (round == 2) {
                st.rewardItems(57, 110000);
                st.rewardItems(955, 1);
            } else if (round == 3) {
                st.rewardItems(57, 199000);
                st.rewardItems(951, 1);
            } else if (round == 4) {
                st.rewardItems(57, 388000);
                st.rewardItems(Rnd.get(RECIPES), 1);
            } else if (round == 5) {
                st.rewardItems(57, 675000);
                st.rewardItems(Rnd.get(BLADES), 1);
            } else if (round == 6) {
                st.rewardItems(57, 1284000);
                st.rewardItems(947, 1);
                st.rewardItems(948, 1);
                st.rewardItems(947, 1);
                st.rewardItems(948, 1);
            }
            st.set("state", "1");
            st.set("stateEx", "0");
        } else if (event.equalsIgnoreCase("30846-22.htm") && state % 10 == 1) {
            if (st.hasQuestItems(8766)) {
                st.set("state", "1005");
                st.takeItems(8766, 1);
            } else {
                htmltext = "30846-22a.htm";
            }
        } else if (event.equalsIgnoreCase("30846-25.htm") && state == 1005) {
            int i0 = st.getInt("stateEx");
            int i1 = i0 % 10;
            int i2 = (i0 - i1) / 10;
            int param1 = Rnd.get(2) + 1;
            int param2 = Rnd.get(5) + 1;
            int param3 = param1 * 10 + param2;
            if (param1 == i2) {
                int i3 = param2 + i1;
                if (i3 % 5 == 0 && i3 != 10) {
                    st.set("state", "1");
                    st.set("stateEx", "0");
                    st.rewardItems(57, 800);
                    htmltext = getHTML("30846-25.htm", i0, param3, player.getName()).replace("%card1%", String.valueOf(i1));
                } else {
                    st.set("state", "1006");
                    st.set("stateEx", String.valueOf(param3));
                    htmltext = getHTML("30846-24.htm", i0, param3, player.getName());
                }
            } else if (param2 == 5 || i2 == 5) {
                st.set("state", "1");
                st.set("stateEx", "0");
                st.rewardItems(57, 800);
                htmltext = getHTML("30846-25.htm", i0, param3, player.getName()).replace("%card1%", String.valueOf(i1));
            } else {
                st.set("state", "1006");
                st.set("stateEx", String.valueOf(param3));
                htmltext = getHTML("30846-24.htm", i0, param3, player.getName());
            }
        } else if (event.equalsIgnoreCase("30846-29.htm") && state == 1006) {
            int i0 = st.getInt("stateEx");
            int i1 = i0 % 10;
            int i2 = (i0 - i1) / 10;
            int param1 = Rnd.get(2) + 1;
            int param2 = Rnd.get(5) + 1;
            int param3 = param1 * 10 + param2;
            if (param1 == i2) {
                int i3 = param2 + i1;
                if (i3 % 5 == 0 && i3 != 10) {
                    st.set("state", "1");
                    st.set("stateEx", "0");
                    st.rewardItems(57, 800);
                    htmltext = getHTML("30846-29.htm", i0, param3, player.getName()).replace("%card1%", String.valueOf(i1));
                } else {
                    st.set("state", "1005");
                    st.set("stateEx", String.valueOf(param3));
                    htmltext = getHTML("30846-28.htm", i0, param3, player.getName());
                }
            } else if (param2 == 5 || i1 == 5) {
                st.set("state", "1");
                st.set("stateEx", "0");
                htmltext = getHTML("30846-29.htm", i0, param3, player.getName());
            } else {
                st.set("state", "1005");
                st.set("stateEx", String.valueOf(param3));
                htmltext = getHTML("30846-28.htm", i0, param3, player.getName());
            }
        } else if (event.equalsIgnoreCase("30846-30.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int state;
        QuestState st = player.getQuestState("Q663_SeductiveWhispers");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 50) ? "30846-02.htm" : "30846-01.htm";
                break;
            case 1:
                state = st.getInt("state");
                if (state < 4) {
                    if (st.hasQuestItems(8766)) {
                        htmltext = "30846-05.htm";
                        break;
                    }
                    htmltext = "30846-04.htm";
                    break;
                }
                if (state % 10 == 4) {
                    htmltext = "30846-05a.htm";
                    break;
                }
                if (state % 10 == 5) {
                    htmltext = "30846-11.htm";
                    break;
                }
                if (state % 10 == 6) {
                    htmltext = "30846-15.htm";
                    break;
                }
                if (state % 10 == 7) {
                    int round = state % 100 / 10;
                    if (round >= 7) {
                        st.rewardItems(57, 2384000);
                        st.rewardItems(729, 1);
                        st.rewardItems(730, 1);
                        st.rewardItems(730, 1);
                        htmltext = "30846-17.htm";
                        break;
                    }
                    htmltext = getHtmlText("30846-16.htm").replace("%wincount%", String.valueOf(state / 10 + 1));
                    break;
                }
                if (state == 1005) {
                    htmltext = "30846-23.htm";
                    break;
                }
                if (state == 1006)
                    htmltext = "30846-26.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(8766, 1, 0, CHANCES.get(Integer.valueOf(npc.getNpcId())));
        return null;
    }

    private String getHTML(String html, int index, int param3, String name) {
        return getHtmlText(html).replace("%card1pic%", CARDS.get(Integer.valueOf(index))).replace("%card2pic%", CARDS.get(Integer.valueOf(param3))).replace("%name%", name);
    }
}
