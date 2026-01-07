package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Q662_AGameOfCards extends Quest {
    private static final String qn = "Q662_AGameOfCards";

    private static final int KLUMP = 30845;

    private static final int RED_GEM = 8765;

    private static final int EW_S = 959;

    private static final int EW_A = 729;

    private static final int EW_B = 947;

    private static final int EW_C = 951;

    private static final int EW_D = 955;

    private static final int EA_D = 956;

    private static final int ZIGGO_GEMSTONE = 8868;

    private static final Map<Integer, String> CARDS = new HashMap<>();

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q662_AGameOfCards() {
        super(662, "A Game Of Cards");
        CARDS.put(Integer.valueOf(0), "?");
        CARDS.put(Integer.valueOf(1), "!");
        CARDS.put(Integer.valueOf(2), "=");
        CARDS.put(Integer.valueOf(3), "T");
        CARDS.put(Integer.valueOf(4), "V");
        CARDS.put(Integer.valueOf(5), "O");
        CARDS.put(Integer.valueOf(6), "P");
        CARDS.put(Integer.valueOf(7), "S");
        CARDS.put(Integer.valueOf(8), "E");
        CARDS.put(Integer.valueOf(9), "H");
        CARDS.put(Integer.valueOf(10), "A");
        CARDS.put(Integer.valueOf(11), "R");
        CARDS.put(Integer.valueOf(12), "D");
        CARDS.put(Integer.valueOf(13), "I");
        CARDS.put(Integer.valueOf(14), "N");
        CHANCES.put(Integer.valueOf(18001), Integer.valueOf(232000));
        CHANCES.put(Integer.valueOf(20672), Integer.valueOf(357000));
        CHANCES.put(Integer.valueOf(20673), Integer.valueOf(373000));
        CHANCES.put(Integer.valueOf(20674), Integer.valueOf(583000));
        CHANCES.put(Integer.valueOf(20677), Integer.valueOf(435000));
        CHANCES.put(Integer.valueOf(20955), Integer.valueOf(358000));
        CHANCES.put(Integer.valueOf(20958), Integer.valueOf(283000));
        CHANCES.put(Integer.valueOf(20959), Integer.valueOf(455000));
        CHANCES.put(Integer.valueOf(20961), Integer.valueOf(365000));
        CHANCES.put(Integer.valueOf(20962), Integer.valueOf(348000));
        CHANCES.put(Integer.valueOf(20965), Integer.valueOf(457000));
        CHANCES.put(Integer.valueOf(20966), Integer.valueOf(493000));
        CHANCES.put(Integer.valueOf(20968), Integer.valueOf(418000));
        CHANCES.put(Integer.valueOf(20972), Integer.valueOf(350000));
        CHANCES.put(Integer.valueOf(20973), Integer.valueOf(453000));
        CHANCES.put(Integer.valueOf(21002), Integer.valueOf(315000));
        CHANCES.put(Integer.valueOf(21004), Integer.valueOf(320000));
        CHANCES.put(Integer.valueOf(21006), Integer.valueOf(335000));
        CHANCES.put(Integer.valueOf(21008), Integer.valueOf(462000));
        CHANCES.put(Integer.valueOf(21010), Integer.valueOf(397000));
        CHANCES.put(Integer.valueOf(21109), Integer.valueOf(507000));
        CHANCES.put(Integer.valueOf(21112), Integer.valueOf(552000));
        CHANCES.put(Integer.valueOf(21114), Integer.valueOf(587000));
        CHANCES.put(Integer.valueOf(21116), Integer.valueOf(812000));
        CHANCES.put(Integer.valueOf(21278), Integer.valueOf(483000));
        CHANCES.put(Integer.valueOf(21279), Integer.valueOf(483000));
        CHANCES.put(Integer.valueOf(21280), Integer.valueOf(483000));
        CHANCES.put(Integer.valueOf(21281), Integer.valueOf(483000));
        CHANCES.put(Integer.valueOf(21286), Integer.valueOf(515000));
        CHANCES.put(Integer.valueOf(21287), Integer.valueOf(515000));
        CHANCES.put(Integer.valueOf(21288), Integer.valueOf(515000));
        CHANCES.put(Integer.valueOf(21289), Integer.valueOf(515000));
        CHANCES.put(Integer.valueOf(21508), Integer.valueOf(493000));
        CHANCES.put(Integer.valueOf(21510), Integer.valueOf(527000));
        CHANCES.put(Integer.valueOf(21513), Integer.valueOf(562000));
        CHANCES.put(Integer.valueOf(21515), Integer.valueOf(598000));
        CHANCES.put(Integer.valueOf(21520), Integer.valueOf(458000));
        CHANCES.put(Integer.valueOf(21526), Integer.valueOf(552000));
        CHANCES.put(Integer.valueOf(21530), Integer.valueOf(488000));
        CHANCES.put(Integer.valueOf(21535), Integer.valueOf(573000));
        setItemsIds(8765);
        addStartNpc(30845);
        addTalkId(30845);
        for (Iterator<Integer> iterator = CHANCES.keySet().iterator(); iterator.hasNext(); ) {
            int monster = iterator.next();
            addKillId(monster);
        }
    }

    private static void giveReward(QuestState st, int item, int count) {
        Item template = ItemTable.getInstance().getTemplate(item);
        if (template.isStackable()) {
            st.giveItems(item, count);
        } else {
            for (int i = 0; i < count; i++)
                st.giveItems(item, 1);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q662_AGameOfCards");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30845-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("state", "0");
            st.set("stateEx", "0");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30845-04.htm")) {
            int state = st.getInt("state");
            int stateEx = st.getInt("stateEx");
            if (state == 0 && stateEx == 0 && st.getQuestItemsCount(8765) >= 50)
                htmltext = "30845-05.htm";
        } else if (event.equalsIgnoreCase("30845-07.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30845-11.htm")) {
            int state = st.getInt("state");
            int stateEx = st.getInt("stateEx");
            if (state == 0 && stateEx == 0 && st.getQuestItemsCount(8765) >= 50) {
                int i1 = Rnd.get(70) + 1;
                int i2 = Rnd.get(70) + 1;
                int i3 = Rnd.get(70) + 1;
                int i4 = Rnd.get(70) + 1;
                int i5 = Rnd.get(70) + 1;
                if (i1 >= 57) {
                    i1 -= 56;
                } else if (i1 >= 43) {
                    i1 -= 42;
                } else if (i1 >= 29) {
                    i1 -= 28;
                } else if (i1 >= 15) {
                    i1 -= 14;
                }
                if (i2 >= 57) {
                    i2 -= 56;
                } else if (i2 >= 43) {
                    i2 -= 42;
                } else if (i2 >= 29) {
                    i2 -= 28;
                } else if (i2 >= 15) {
                    i2 -= 14;
                }
                if (i3 >= 57) {
                    i3 -= 56;
                } else if (i3 >= 43) {
                    i3 -= 42;
                } else if (i3 >= 29) {
                    i3 -= 28;
                } else if (i3 >= 15) {
                    i3 -= 14;
                }
                if (i4 >= 57) {
                    i4 -= 56;
                } else if (i4 >= 43) {
                    i4 -= 42;
                } else if (i4 >= 29) {
                    i4 -= 28;
                } else if (i4 >= 15) {
                    i4 -= 14;
                }
                if (i5 >= 57) {
                    i5 -= 56;
                } else if (i5 >= 43) {
                    i5 -= 42;
                } else if (i5 >= 29) {
                    i5 -= 28;
                } else if (i5 >= 15) {
                    i5 -= 14;
                }
                st.set("state", String.valueOf(i4 * 1000000 + i3 * 10000 + i2 * 100 + i1));
                st.set("stateEx", String.valueOf(i5));
                st.takeItems(8765, 50);
            }
        } else if (event.equals("First") || event.equals("Second") || event.equals("Third") || event.equals("Fourth") || event.equals("Fifth")) {
            int state = st.getInt("state");
            int stateEx = st.getInt("stateEx");
            int i0 = state;
            int i1 = stateEx;
            int i5 = i1 % 100;
            int i9 = i1 / 100;
            i1 = i0 % 100;
            int i2 = i0 % 10000 / 100;
            int i3 = i0 % 1000000 / 10000;
            int i4 = i0 % 100000000 / 1000000;
            if (event.equals("First")) {
                if (i9 % 2 < 1)
                    i9++;
            } else if (event.equals("Second")) {
                if (i9 % 4 < 2)
                    i9 += 2;
            } else if (event.equals("Third")) {
                if (i9 % 8 < 4)
                    i9 += 4;
            } else if (event.equals("Fourth")) {
                if (i9 % 16 < 8)
                    i9 += 8;
            } else if (event.equals("Fifth")) {
                if (i9 % 32 < 16)
                    i9 += 16;
            }
            if (i9 % 32 < 31) {
                st.set("stateEx", String.valueOf(i9 * 100 + i5));
                htmltext = getHtmlText("30845-12.htm");
            } else if (i9 % 32 == 31) {
                int i6 = 0;
                int i8 = 0;
                if (i1 >= 1 && i1 <= 14 && i2 >= 1 && i2 <= 14 && i3 >= 1 && i3 <= 14 && i4 >= 1 && i4 <= 14 && i5 >= 1 && i5 <= 14) {
                    if (i1 == i2) {
                        i6 += 10;
                        i8 += 8;
                    }
                    if (i1 == i3) {
                        i6 += 10;
                        i8 += 4;
                    }
                    if (i1 == i4) {
                        i6 += 10;
                        i8 += 2;
                    }
                    if (i1 == i5) {
                        i6 += 10;
                        i8++;
                    }
                    if (i6 % 100 < 10) {
                        if (i8 % 16 < 8) {
                            if (i8 % 8 < 4)
                                if (i2 == i3) {
                                    i6 += 10;
                                    i8 += 4;
                                }
                            if (i8 % 4 < 2)
                                if (i2 == i4) {
                                    i6 += 10;
                                    i8 += 2;
                                }
                            if (i8 % 2 < 1)
                                if (i2 == i5) {
                                    i6 += 10;
                                    i8++;
                                }
                        }
                    } else if (i6 % 10 == 0) {
                        if (i8 % 16 < 8) {
                            if (i8 % 8 < 4)
                                if (i2 == i3) {
                                    i6++;
                                    i8 += 4;
                                }
                            if (i8 % 4 < 2)
                                if (i2 == i4) {
                                    i6++;
                                    i8 += 2;
                                }
                            if (i8 % 2 < 1)
                                if (i2 == i5) {
                                    i6++;
                                    i8++;
                                }
                        }
                    }
                    if (i6 % 100 < 10) {
                        if (i8 % 8 < 4) {
                            if (i8 % 4 < 2)
                                if (i3 == i4) {
                                    i6 += 10;
                                    i8 += 2;
                                }
                            if (i8 % 2 < 1)
                                if (i3 == i5) {
                                    i6 += 10;
                                    i8++;
                                }
                        }
                    } else if (i6 % 10 == 0) {
                        if (i8 % 8 < 4) {
                            if (i8 % 4 < 2)
                                if (i3 == i4) {
                                    i6++;
                                    i8 += 2;
                                }
                            if (i8 % 2 < 1)
                                if (i3 == i5) {
                                    i6++;
                                    i8++;
                                }
                        }
                    }
                    if (i6 % 100 < 10) {
                        if (i8 % 4 < 2)
                            if (i8 % 2 < 1)
                                if (i4 == i5) {
                                    i6 += 10;
                                    i8++;
                                }
                    } else if (i6 % 10 == 0) {
                        if (i8 % 4 < 2)
                            if (i8 % 2 < 1)
                                if (i4 == i5) {
                                    i6++;
                                    i8++;
                                }
                    }
                }
                if (i6 == 40) {
                    giveReward(st, 8868, 43);
                    giveReward(st, 959, 3);
                    giveReward(st, 729, 1);
                    htmltext = getHtmlText("30845-13.htm");
                } else if (i6 == 30) {
                    giveReward(st, 959, 2);
                    giveReward(st, 951, 2);
                    htmltext = getHtmlText("30845-14.htm");
                } else if (i6 == 21 || i6 == 12) {
                    giveReward(st, 729, 1);
                    giveReward(st, 947, 2);
                    giveReward(st, 955, 1);
                    htmltext = getHtmlText("30845-15.htm");
                } else if (i6 == 20) {
                    giveReward(st, 951, 2);
                    htmltext = getHtmlText("30845-16.htm");
                } else if (i6 == 11) {
                    giveReward(st, 951, 1);
                    htmltext = getHtmlText("30845-17.htm");
                } else if (i6 == 10) {
                    giveReward(st, 956, 2);
                    htmltext = getHtmlText("30845-18.htm");
                } else if (i6 == 0) {
                    htmltext = getHtmlText("30845-19.htm");
                }
                st.set("state", "0");
                st.set("stateEx", "0");
            }
            htmltext = htmltext.replace("%FontColor1%", (i9 % 2 < 1) ? "ffff00" : "ff6f6f").replace("%Cell1%", (i9 % 2 < 1) ? CARDS.get(Integer.valueOf(0)) : CARDS.get(Integer.valueOf(i1)));
            htmltext = htmltext.replace("%FontColor2%", (i9 % 4 < 2) ? "ffff00" : "ff6f6f").replace("%Cell2%", (i9 % 4 < 2) ? CARDS.get(Integer.valueOf(0)) : CARDS.get(Integer.valueOf(i2)));
            htmltext = htmltext.replace("%FontColor3%", (i9 % 8 < 4) ? "ffff00" : "ff6f6f").replace("%Cell3%", (i9 % 8 < 4) ? CARDS.get(Integer.valueOf(0)) : CARDS.get(Integer.valueOf(i3)));
            htmltext = htmltext.replace("%FontColor4%", (i9 % 16 < 8) ? "ffff00" : "ff6f6f").replace("%Cell4%", (i9 % 16 < 8) ? CARDS.get(Integer.valueOf(0)) : CARDS.get(Integer.valueOf(i4)));
            htmltext = htmltext.replace("%FontColor5%", (i9 % 32 < 16) ? "ffff00" : "ff6f6f").replace("%Cell5%", (i9 % 32 < 16) ? CARDS.get(Integer.valueOf(0)) : CARDS.get(Integer.valueOf(i5)));
        } else if (event.equalsIgnoreCase("30845-20.htm")) {
            if (st.getQuestItemsCount(8765) < 50)
                htmltext = "30845-21.htm";
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int state, stateEx;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q662_AGameOfCards");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 61) ? "30845-02.htm" : "30845-01.htm";
                break;
            case 1:
                state = st.getInt("state");
                stateEx = st.getInt("stateEx");
                if (state == 0 && stateEx == 0) {
                    htmltext = (st.getQuestItemsCount(8765) < 50) ? "30845-04.htm" : "30845-05.htm";
                    break;
                }
                if (state != 0 && stateEx != 0) {
                    int i0 = state;
                    int i1 = stateEx;
                    int i5 = i1 % 100;
                    int i9 = i1 / 100;
                    i1 = i0 % 100;
                    int i2 = i0 % 10000 / 100;
                    int i3 = i0 % 1000000 / 10000;
                    int i4 = i0 % 100000000 / 1000000;
                    htmltext = getHtmlText("30845-11a.htm");
                    htmltext = htmltext.replace("%FontColor1%", (i9 % 2 < 1) ? "ffff00" : "ff6f6f").replace("%Cell1%", (i9 % 2 < 1) ? CARDS.get(Integer.valueOf(0)) : CARDS.get(Integer.valueOf(i1)));
                    htmltext = htmltext.replace("%FontColor2%", (i9 % 4 < 2) ? "ffff00" : "ff6f6f").replace("%Cell2%", (i9 % 4 < 2) ? CARDS.get(Integer.valueOf(0)) : CARDS.get(Integer.valueOf(i2)));
                    htmltext = htmltext.replace("%FontColor3%", (i9 % 8 < 4) ? "ffff00" : "ff6f6f").replace("%Cell3%", (i9 % 8 < 4) ? CARDS.get(Integer.valueOf(0)) : CARDS.get(Integer.valueOf(i3)));
                    htmltext = htmltext.replace("%FontColor4%", (i9 % 16 < 8) ? "ffff00" : "ff6f6f").replace("%Cell4%", (i9 % 16 < 8) ? CARDS.get(Integer.valueOf(0)) : CARDS.get(Integer.valueOf(i4)));
                    htmltext = htmltext.replace("%FontColor5%", (i9 % 32 < 16) ? "ffff00" : "ff6f6f").replace("%Cell5%", (i9 % 32 < 16) ? CARDS.get(Integer.valueOf(0)) : CARDS.get(Integer.valueOf(i5)));
                }
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMemberState(player, npc, (byte) 1);
        if (st == null)
            return null;
        st.dropItems(8765, 1, 0, CHANCES.get(Integer.valueOf(npc.getNpcId())));
        return null;
    }
}
