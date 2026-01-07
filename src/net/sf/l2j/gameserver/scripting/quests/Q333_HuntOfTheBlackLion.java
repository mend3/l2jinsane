package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q333_HuntOfTheBlackLion extends Quest {
    private static final String qn = "Q333_HuntOfTheBlackLion";

    private static final int SOPHYA = 30735;

    private static final int REDFOOT = 30736;

    private static final int RUPIO = 30471;

    private static final int UNDRIAS = 30130;

    private static final int LOCKIRIN = 30531;

    private static final int MORGAN = 30737;

    private static final int BLACK_LION_MARK = 1369;

    private static final int LION_CLAW = 3675;

    private static final int LION_EYE = 3676;

    private static final int GUILD_COIN = 3677;

    private static final int UNDEAD_ASH = 3848;

    private static final int BLOODY_AXE_INSIGNIA = 3849;

    private static final int DELU_FANG = 3850;

    private static final int STAKATO_TALON = 3851;

    private static final int SOPHYA_LETTER_1 = 3671;

    private static final int SOPHYA_LETTER_2 = 3672;

    private static final int SOPHYA_LETTER_3 = 3673;

    private static final int SOPHYA_LETTER_4 = 3674;

    private static final int CARGO_BOX_1 = 3440;

    private static final int CARGO_BOX_2 = 3441;

    private static final int CARGO_BOX_3 = 3442;

    private static final int CARGO_BOX_4 = 3443;

    private static final int GLUDIO_APPLE = 3444;

    private static final int CORN_MEAL = 3445;

    private static final int WOLF_PELTS = 3446;

    private static final int MOONSTONE = 3447;

    private static final int GLUDIO_WHEAT_FLOWER = 3448;

    private static final int SPIDERSILK_ROPE = 3449;

    private static final int ALEXANDRITE = 3450;

    private static final int SILVER_TEA = 3451;

    private static final int GOLEM_PART = 3452;

    private static final int FIRE_EMERALD = 3453;

    private static final int SILK_FROCK = 3454;

    private static final int PORCELAN_URN = 3455;

    private static final int IMPERIAL_DIAMOND = 3456;

    private static final int STATUE_SHILIEN_HEAD = 3457;

    private static final int STATUE_SHILIEN_TORSO = 3458;

    private static final int STATUE_SHILIEN_ARM = 3459;

    private static final int STATUE_SHILIEN_LEG = 3460;

    private static final int COMPLETE_STATUE = 3461;

    private static final int TABLET_FRAGMENT_1 = 3462;

    private static final int TABLET_FRAGMENT_2 = 3463;

    private static final int TABLET_FRAGMENT_3 = 3464;

    private static final int TABLET_FRAGMENT_4 = 3465;

    private static final int COMPLETE_TABLET = 3466;

    private static final int ADENA = 57;

    private static final int SWIFT_ATTACK_POTION = 735;

    private static final int SCROLL_OF_ESCAPE = 736;

    private static final int HEALING_POTION = 1061;

    private static final int SOULSHOT_D = 1463;

    private static final int SPIRITSHOT_D = 2510;

    private static final int[][] DROPLIST = new int[][]{
            {3671, 20160, 3848, 500000, 3440, 90000}, {3671, 20171, 3848, 500000, 3440, 60000}, {3671, 20197, 3848, 500000, 3440, 70000}, {3671, 20198, 3848, 500000, 3440, 80000}, {3671, 20200, 3848, 500000, 3440, 100000}, {3671, 20201, 3848, 500000, 3440, 110000}, {3672, 20207, 3849, 500000, 3441, 60000}, {3672, 20208, 3849, 500000, 3441, 70000}, {3672, 20209, 3849, 500000, 3441, 80000}, {3672, 20210, 3849, 500000, 3441, 90000},
            {3672, 20211, 3849, 500000, 3441, 100000}, {3673, 20251, 3850, 500000, 3442, 100000}, {3673, 20252, 3850, 500000, 3442, 110000}, {3673, 20253, 3850, 500000, 3442, 120000}, {3674, 20157, 3851, 500000, 3443, 100000}, {3674, 20230, 3851, 500000, 3443, 110000}, {3674, 20232, 3851, 500000, 3443, 120000}, {3674, 20234, 3851, 500000, 3443, 130000}};

    public Q333_HuntOfTheBlackLion() {
        super(333, "Hunt Of The Black Lion");
        setItemsIds(3675, 3676, 3677, 3848, 3849, 3850, 3851, 3671, 3672, 3673,
                3674);
        addStartNpc(30735);
        addTalkId(30735, 30736, 30471, 30130, 30531, 30737);
        for (int[] i : DROPLIST) {
            addKillId(i[1]);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q333_HuntOfTheBlackLion");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30735-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30735-10.htm")) {
            if (!st.hasQuestItems(3671)) {
                st.giveItems(3671, 1);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30735-11.htm")) {
            if (!st.hasQuestItems(3672)) {
                st.giveItems(3672, 1);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30735-12.htm")) {
            if (!st.hasQuestItems(3673)) {
                st.giveItems(3673, 1);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30735-13.htm")) {
            if (!st.hasQuestItems(3674)) {
                st.giveItems(3674, 1);
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (event.equalsIgnoreCase("30735-16.htm")) {
            if (st.getQuestItemsCount(3675) > 9) {
                st.takeItems(3675, 10);
                int eyes = st.getQuestItemsCount(3676);
                if (eyes < 5) {
                    htmltext = "30735-17a.htm";
                    st.giveItems(3676, 1);
                    int random = Rnd.get(100);
                    if (random < 25) {
                        st.giveItems(1061, 20);
                    } else if (random < 50) {
                        st.giveItems(player.isMageClass() ? 2510 : 1463, player.isMageClass() ? 50 : 100);
                    } else if (random < 75) {
                        st.giveItems(736, 20);
                    } else {
                        st.giveItems(735, 3);
                    }
                } else if (eyes < 9) {
                    htmltext = "30735-18b.htm";
                    st.giveItems(3676, 1);
                    int random = Rnd.get(100);
                    if (random < 25) {
                        st.giveItems(1061, 25);
                    } else if (random < 50) {
                        st.giveItems(player.isMageClass() ? 2510 : 1463, player.isMageClass() ? 100 : 200);
                    } else if (random < 75) {
                        st.giveItems(736, 20);
                    } else {
                        st.giveItems(735, 3);
                    }
                } else {
                    htmltext = "30735-19b.htm";
                    int random = Rnd.get(100);
                    if (random < 25) {
                        st.giveItems(1061, 50);
                    } else if (random < 50) {
                        st.giveItems(player.isMageClass() ? 2510 : 1463, player.isMageClass() ? 200 : 400);
                    } else if (random < 75) {
                        st.giveItems(736, 30);
                    } else {
                        st.giveItems(735, 4);
                    }
                }
            }
        } else if (event.equalsIgnoreCase("30735-20.htm")) {
            st.takeItems(3671, -1);
            st.takeItems(3672, -1);
            st.takeItems(3673, -1);
            st.takeItems(3674, -1);
        } else if (event.equalsIgnoreCase("30735-26.htm")) {
            st.takeItems(3675, -1);
            st.takeItems(3676, -1);
            st.takeItems(3677, -1);
            st.takeItems(1369, -1);
            st.takeItems(3671, -1);
            st.takeItems(3672, -1);
            st.takeItems(3673, -1);
            st.takeItems(3674, -1);
            st.giveItems(57, 12400);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30736-03.htm")) {
            boolean cargo1 = st.hasQuestItems(3440);
            boolean cargo2 = st.hasQuestItems(3441);
            boolean cargo3 = st.hasQuestItems(3442);
            boolean cargo4 = st.hasQuestItems(3443);
            if ((cargo1 || cargo2 || cargo3 || cargo4) && player.getAdena() > 649) {
                st.takeItems(57, 650);
                if (cargo1) {
                    st.takeItems(3440, 1);
                } else if (cargo2) {
                    st.takeItems(3441, 1);
                } else if (cargo3) {
                    st.takeItems(3442, 1);
                } else {
                    st.takeItems(3443, 1);
                }
                int i0 = Rnd.get(100);
                int i1 = Rnd.get(100);
                if (i0 < 40) {
                    if (i1 < 33) {
                        htmltext = "30736-04a.htm";
                        st.giveItems(3444, 1);
                    } else if (i1 < 66) {
                        htmltext = "30736-04b.htm";
                        st.giveItems(3445, 1);
                    } else {
                        htmltext = "30736-04c.htm";
                        st.giveItems(3446, 1);
                    }
                } else if (i0 < 60) {
                    if (i1 < 33) {
                        htmltext = "30736-04d.htm";
                        st.giveItems(3447, 1);
                    } else if (i1 < 66) {
                        htmltext = "30736-04e.htm";
                        st.giveItems(3448, 1);
                    } else {
                        htmltext = "30736-04f.htm";
                        st.giveItems(3449, 1);
                    }
                } else if (i0 < 70) {
                    if (i1 < 33) {
                        htmltext = "30736-04g.htm";
                        st.giveItems(3450, 1);
                    } else if (i1 < 66) {
                        htmltext = "30736-04h.htm";
                        st.giveItems(3451, 1);
                    } else {
                        htmltext = "30736-04i.htm";
                        st.giveItems(3452, 1);
                    }
                } else if (i0 < 75) {
                    if (i1 < 33) {
                        htmltext = "30736-04j.htm";
                        st.giveItems(3453, 1);
                    } else if (i1 < 66) {
                        htmltext = "30736-04k.htm";
                        st.giveItems(3454, 1);
                    } else {
                        htmltext = "30736-04l.htm";
                        st.giveItems(3455, 1);
                    }
                } else if (i0 < 76) {
                    htmltext = "30736-04m.htm";
                    st.giveItems(3456, 1);
                } else if (Rnd.nextBoolean()) {
                    htmltext = "30736-04n.htm";
                    if (i1 < 25) {
                        st.giveItems(3457, 1);
                    } else if (i1 < 50) {
                        st.giveItems(3458, 1);
                    } else if (i1 < 75) {
                        st.giveItems(3459, 1);
                    } else {
                        st.giveItems(3460, 1);
                    }
                } else {
                    htmltext = "30736-04o.htm";
                    if (i1 < 25) {
                        st.giveItems(3462, 1);
                    } else if (i1 < 50) {
                        st.giveItems(3463, 1);
                    } else if (i1 < 75) {
                        st.giveItems(3464, 1);
                    } else {
                        st.giveItems(3465, 1);
                    }
                }
            } else {
                htmltext = "30736-05.htm";
            }
        } else if (event.equalsIgnoreCase("30736-07.htm")) {
            int state = st.getInt("state");
            if (player.getAdena() > 200 + state * 200)
                if (state < 3) {
                    int i0 = Rnd.get(100);
                    if (i0 < 5) {
                        htmltext = "30736-08a.htm";
                    } else if (i0 < 10) {
                        htmltext = "30736-08b.htm";
                    } else if (i0 < 15) {
                        htmltext = "30736-08c.htm";
                    } else if (i0 < 20) {
                        htmltext = "30736-08d.htm";
                    } else if (i0 < 25) {
                        htmltext = "30736-08e.htm";
                    } else if (i0 < 30) {
                        htmltext = "30736-08f.htm";
                    } else if (i0 < 35) {
                        htmltext = "30736-08g.htm";
                    } else if (i0 < 40) {
                        htmltext = "30736-08h.htm";
                    } else if (i0 < 45) {
                        htmltext = "30736-08i.htm";
                    } else if (i0 < 50) {
                        htmltext = "30736-08j.htm";
                    } else if (i0 < 55) {
                        htmltext = "30736-08k.htm";
                    } else if (i0 < 60) {
                        htmltext = "30736-08l.htm";
                    } else if (i0 < 65) {
                        htmltext = "30736-08m.htm";
                    } else if (i0 < 70) {
                        htmltext = "30736-08n.htm";
                    } else if (i0 < 75) {
                        htmltext = "30736-08o.htm";
                    } else if (i0 < 80) {
                        htmltext = "30736-08p.htm";
                    } else if (i0 < 85) {
                        htmltext = "30736-08q.htm";
                    } else if (i0 < 90) {
                        htmltext = "30736-08r.htm";
                    } else if (i0 < 95) {
                        htmltext = "30736-08s.htm";
                    } else {
                        htmltext = "30736-08t.htm";
                    }
                    st.takeItems(57, 200 + state * 200);
                    st.set("state", String.valueOf(state + 1));
                } else {
                    htmltext = "30736-08.htm";
                }
        } else if (event.equalsIgnoreCase("30471-03.htm")) {
            if (st.hasQuestItems(3457, 3458, 3459, 3460)) {
                st.takeItems(3457, 1);
                st.takeItems(3458, 1);
                st.takeItems(3459, 1);
                st.takeItems(3460, 1);
                if (Rnd.nextBoolean()) {
                    htmltext = "30471-04.htm";
                    st.giveItems(3461, 1);
                } else {
                    htmltext = "30471-05.htm";
                }
            }
        } else if (event.equalsIgnoreCase("30471-06.htm")) {
            if (st.hasQuestItems(3462, 3463, 3464, 3465)) {
                st.takeItems(3462, 1);
                st.takeItems(3463, 1);
                st.takeItems(3464, 1);
                st.takeItems(3465, 1);
                if (Rnd.nextBoolean()) {
                    htmltext = "30471-07.htm";
                    st.giveItems(3466, 1);
                } else {
                    htmltext = "30471-08.htm";
                }
            }
        } else if (event.equalsIgnoreCase("30130-04.htm") && st.hasQuestItems(3461)) {
            st.takeItems(3461, 1);
            st.giveItems(57, 30000);
        } else if (event.equalsIgnoreCase("30531-04.htm") && st.hasQuestItems(3466)) {
            st.takeItems(3466, 1);
            st.giveItems(57, 30000);
        } else if (event.equalsIgnoreCase("30737-06.htm")) {
            boolean cargo1 = st.hasQuestItems(3440);
            boolean cargo2 = st.hasQuestItems(3441);
            boolean cargo3 = st.hasQuestItems(3442);
            boolean cargo4 = st.hasQuestItems(3443);
            if (cargo1 || cargo2 || cargo3 || cargo4) {
                if (cargo1) {
                    st.takeItems(3440, 1);
                } else if (cargo2) {
                    st.takeItems(3441, 1);
                } else if (cargo3) {
                    st.takeItems(3442, 1);
                } else {
                    st.takeItems(3443, 1);
                }
                int coins = st.getQuestItemsCount(3677);
                if (coins < 40) {
                    htmltext = "30737-03.htm";
                    st.giveItems(57, 100);
                } else if (coins < 80) {
                    htmltext = "30737-04.htm";
                    st.giveItems(57, 200);
                } else {
                    htmltext = "30737-05.htm";
                    st.giveItems(57, 300);
                }
                if (coins < 80)
                    st.giveItems(3677, 1);
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int count;
        QuestState st = player.getQuestState("Q333_HuntOfTheBlackLion");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 25) {
                    htmltext = "30735-01.htm";
                    break;
                }
                if (!st.hasQuestItems(1369)) {
                    htmltext = "30735-02.htm";
                    break;
                }
                htmltext = "30735-03.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30735:
                        if (!st.hasAtLeastOneQuestItem(3671, 3672, 3673, 3674)) {
                            htmltext = "30735-14.htm";
                            break;
                        }
                        if (!st.hasAtLeastOneQuestItem(3848, 3849, 3850, 3851)) {
                            htmltext = st.hasAtLeastOneQuestItem(3440, 3441, 3442, 3443) ? "30735-15a.htm" : "30735-15.htm";
                            break;
                        }
                        count = st.getQuestItemsCount(3848) + st.getQuestItemsCount(3849) + st.getQuestItemsCount(3850) + st.getQuestItemsCount(3851);
                        st.takeItems(3848, -1);
                        st.takeItems(3849, -1);
                        st.takeItems(3850, -1);
                        st.takeItems(3851, -1);
                        st.giveItems(57, count * 35);
                        if (count >= 20 && count < 50) {
                            st.giveItems(3675, 1);
                        } else if (count >= 50 && count < 100) {
                            st.giveItems(3675, 2);
                        } else if (count >= 100) {
                            st.giveItems(3675, 3);
                        }
                        htmltext = st.hasAtLeastOneQuestItem(3440, 3441, 3442, 3443) ? "30735-23.htm" : "30735-22.htm";
                        break;
                    case 30736:
                        htmltext = st.hasAtLeastOneQuestItem(3440, 3441, 3442, 3443) ? "30736-02.htm" : "30736-01.htm";
                        break;
                    case 30471:
                        if (st.hasQuestItems(3457, 3458, 3459, 3460) || st.hasQuestItems(3462, 3463, 3464, 3465)) {
                            htmltext = "30471-02.htm";
                            break;
                        }
                        htmltext = "30471-01.htm";
                        break;
                    case 30130:
                        if (!st.hasQuestItems(3461)) {
                            htmltext = st.hasQuestItems(3457, 3458, 3459, 3460) ? "30130-02.htm" : "30130-01.htm";
                            break;
                        }
                        htmltext = "30130-03.htm";
                        break;
                    case 30531:
                        if (!st.hasQuestItems(3466)) {
                            htmltext = st.hasQuestItems(3462, 3463, 3464, 3465) ? "30531-02.htm" : "30531-01.htm";
                            break;
                        }
                        htmltext = "30531-03.htm";
                        break;
                    case 30737:
                        htmltext = st.hasAtLeastOneQuestItem(3440, 3441, 3442, 3443) ? "30737-02.htm" : "30737-01.htm";
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
        for (int[] info : DROPLIST) {
            if (st.hasQuestItems(info[0]) && npc.getNpcId() == info[1]) {
                st.dropItems(info[2], 1, 0, info[3]);
                st.dropItems(info[4], 1, 0, info[5]);
                break;
            }
        }
        return null;
    }
}
