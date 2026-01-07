package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q348_AnArrogantSearch extends Quest {
    private static final String qn = "Q348_AnArrogantSearch";

    private static final int TITAN_POWERSTONE = 4287;

    private static final int HANELLIN_FIRST_LETTER = 4288;

    private static final int HANELLIN_SECOND_LETTER = 4289;

    private static final int HANELLIN_THIRD_LETTER = 4290;

    private static final int FIRST_KEY_OF_ARK = 4291;

    private static final int SECOND_KEY_OF_ARK = 4292;

    private static final int THIRD_KEY_OF_ARK = 4293;

    private static final int BOOK_OF_SAINT = 4397;

    private static final int BLOOD_OF_SAINT = 4398;

    private static final int BOUGH_OF_SAINT = 4399;

    private static final int WHITE_FABRIC_TRIBE = 4294;

    private static final int WHITE_FABRIC_ANGELS = 5232;

    private static final int BLOODED_FABRIC = 4295;

    private static final int ANTIDOTE = 1831;

    private static final int HEALING_POTION = 1061;

    private static final int HANELLIN = 30864;

    private static final int CLAUDIA_ATHEBALDT = 31001;

    private static final int MARTIEN = 30645;

    private static final int HARNE = 30144;

    private static final int ARK_GUARDIAN_CORPSE = 30980;

    private static final int HOLY_ARK_OF_SECRECY_1 = 30977;

    private static final int HOLY_ARK_OF_SECRECY_2 = 30978;

    private static final int HOLY_ARK_OF_SECRECY_3 = 30979;

    private static final int GUSTAV_ATHEBALDT = 30760;

    private static final int HARDIN = 30832;

    private static final int IASON_HEINE = 30969;

    private static final int LESSER_GIANT_MAGE = 20657;

    private static final int LESSER_GIANT_ELDER = 20658;

    private static final int PLATINUM_TRIBE_SHAMAN = 20828;

    private static final int PLATINUM_TRIBE_OVERLORD = 20829;

    private static final int GUARDIAN_ANGEL = 20859;

    private static final int SEAL_ANGEL = 20860;

    private static final int ANGEL_KILLER = 27184;

    private static final int ARK_GUARDIAN_ELBEROTH = 27182;

    private static final int ARK_GUARDIAN_SHADOW_FANG = 27183;

    private Npc _elberoth;

    private Npc _shadowFang;

    private Npc _angelKiller;

    public Q348_AnArrogantSearch() {
        super(348, "An Arrogant Search");
        setItemsIds(4287, 4288, 4289, 4290, 4291, 4292, 4293, 4397, 4398, 4399,
                4294, 5232);
        addStartNpc(30864);
        addTalkId(30864, 31001, 30645, 30144, 30977, 30978, 30979, 30980, 30760, 30832,
                30969);
        addSpawnId(27182, 27183, 27184);
        addAttackId(27182, 27183, 27184, 20828, 20829);
        addKillId(20657, 20658, 27182, 27183, 27184, 20828, 20829, 20859, 20860);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q348_AnArrogantSearch");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30864-05.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.set("cond", "2");
            st.set("points", "0");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("30864-09.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(4287, 1);
        } else if (event.equalsIgnoreCase("30864-17.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(4288, 1);
            st.giveItems(4289, 1);
            st.giveItems(4290, 1);
        } else if (event.equalsIgnoreCase("30864-36.htm")) {
            st.set("cond", "24");
            st.playSound("ItemSound.quest_middle");
            st.rewardItems(57, Rnd.get(1, 2) * 12000);
        } else if (event.equalsIgnoreCase("30864-37.htm")) {
            st.set("cond", "25");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30864-51.htm")) {
            st.set("cond", "26");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(5232, st.hasQuestItems(4295) ? 9 : 10);
        } else if (event.equalsIgnoreCase("30864-58.htm")) {
            st.set("cond", "27");
            st.playSound("ItemSound.quest_middle");
        } else if (event.equalsIgnoreCase("30864-57.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30864-56.htm")) {
            st.set("cond", "29");
            st.set("gustav", "0");
            st.set("hardin", "0");
            st.set("iason", "0");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(5232, 10);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q348_AnArrogantSearch");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (st.hasQuestItems(4295)) {
                    htmltext = "30864-00.htm";
                    break;
                }
                if (player.getLevel() < 60) {
                    htmltext = "30864-01.htm";
                    break;
                }
                htmltext = "30864-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 30864:
                        if (cond == 1) {
                            htmltext = "30864-02.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = !st.hasQuestItems(4287) ? "30864-06.htm" : "30864-07.htm";
                            break;
                        }
                        if (cond == 4) {
                            htmltext = "30864-09.htm";
                            break;
                        }
                        if (cond > 4 && cond < 21) {
                            htmltext = player.getInventory().hasAtLeastOneItem(4397, 4398, 4399) ? "30864-28.htm" : "30864-24.htm";
                            break;
                        }
                        if (cond == 21) {
                            htmltext = "30864-29.htm";
                            st.set("cond", "22");
                            st.takeItems(4397, 1);
                            st.takeItems(4398, 1);
                            st.takeItems(4399, 1);
                            st.playSound("ItemSound.quest_middle");
                            break;
                        }
                        if (cond == 22) {
                            if (st.hasQuestItems(4294)) {
                                htmltext = "30864-31.htm";
                                break;
                            }
                            if (st.getQuestItemsCount(1831) < 5 || !st.hasQuestItems(1061)) {
                                htmltext = "30864-30.htm";
                                break;
                            }
                            htmltext = "30864-31.htm";
                            st.takeItems(1831, 5);
                            st.takeItems(1061, 1);
                            st.giveItems(4294, 1);
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        if (cond == 24) {
                            htmltext = "30864-38.htm";
                            break;
                        }
                        if (cond == 25) {
                            if (st.hasQuestItems(4294)) {
                                htmltext = "30864-39.htm";
                                break;
                            }
                            if (st.hasQuestItems(4295)) {
                                htmltext = "30864-49.htm";
                                break;
                            }
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                            break;
                        }
                        if (cond == 26) {
                            int count = st.getQuestItemsCount(4295);
                            if (count + st.getQuestItemsCount(5232) < 10) {
                                htmltext = "30864-54.htm";
                                st.takeItems(4295, -1);
                                st.rewardItems(57, 1000 * count + 4000);
                                st.exitQuest(true);
                                break;
                            }
                            if (count < 10) {
                                htmltext = "30864-52.htm";
                                break;
                            }
                            if (count >= 10)
                                htmltext = "30864-53.htm";
                            break;
                        }
                        if (cond == 27) {
                            if (st.getInt("gustav") + st.getInt("hardin") + st.getInt("iason") == 3) {
                                htmltext = "30864-60.htm";
                                st.set("cond", "28");
                                st.rewardItems(57, 49000);
                                st.playSound("ItemSound.quest_middle");
                                break;
                            }
                            if (st.hasQuestItems(4295) && st.getInt("usedonbaium") != 1) {
                                htmltext = "30864-59.htm";
                                break;
                            }
                            htmltext = "30864-61.htm";
                            st.playSound("ItemSound.quest_finish");
                            st.exitQuest(true);
                            break;
                        }
                        if (cond == 28) {
                            htmltext = "30864-55.htm";
                            break;
                        }
                        if (cond == 29) {
                            int count = st.getQuestItemsCount(4295);
                            if (count + st.getQuestItemsCount(5232) < 10) {
                                htmltext = "30864-54.htm";
                                st.takeItems(4295, -1);
                                st.rewardItems(57, 5000 * count);
                                st.playSound("ItemSound.quest_finish");
                                st.exitQuest(true);
                                break;
                            }
                            if (count < 10) {
                                htmltext = "30864-52.htm";
                                break;
                            }
                            if (count >= 10)
                                htmltext = "30864-53.htm";
                        }
                        break;
                    case 30760:
                        if (cond == 27) {
                            if (st.getQuestItemsCount(4295) >= 3 && st.getInt("gustav") == 0) {
                                st.set("gustav", "1");
                                htmltext = "30760-01.htm";
                                st.takeItems(4295, 3);
                                break;
                            }
                            if (st.getInt("gustav") == 1) {
                                htmltext = "30760-02.htm";
                                break;
                            }
                            htmltext = "30760-03.htm";
                            st.set("usedonbaium", "1");
                        }
                        break;
                    case 30832:
                        if (cond == 27) {
                            if (st.hasQuestItems(4295) && st.getInt("hardin") == 0) {
                                st.set("hardin", "1");
                                htmltext = "30832-01.htm";
                                st.takeItems(4295, 1);
                                break;
                            }
                            if (st.getInt("hardin") == 1) {
                                htmltext = "30832-02.htm";
                                break;
                            }
                            htmltext = "30832-03.htm";
                            st.set("usedonbaium", "1");
                        }
                        break;
                    case 30969:
                        if (cond == 27) {
                            if (st.getQuestItemsCount(4295) >= 6 && st.getInt("iason") == 0) {
                                st.set("iason", "1");
                                htmltext = "30969-01.htm";
                                st.takeItems(4295, 6);
                                break;
                            }
                            if (st.getInt("iason") == 1) {
                                htmltext = "30969-02.htm";
                                break;
                            }
                            htmltext = "30969-03.htm";
                            st.set("usedonbaium", "1");
                        }
                        break;
                    case 30144:
                        if (cond >= 5 && cond <= 22) {
                            if (!st.hasQuestItems(4398)) {
                                if (st.hasQuestItems(4288)) {
                                    htmltext = "30144-01.htm";
                                    st.set("cond", "17");
                                    st.playSound("ItemSound.quest_middle");
                                    st.takeItems(4288, 1);
                                    st.addRadar(-418, 44174, -3568);
                                    break;
                                }
                                if (!st.hasQuestItems(4291)) {
                                    htmltext = "30144-03.htm";
                                    st.addRadar(-418, 44174, -3568);
                                    break;
                                }
                                htmltext = "30144-04.htm";
                                break;
                            }
                            htmltext = "30144-05.htm";
                        }
                        break;
                    case 31001:
                        if (cond >= 5 && cond <= 22) {
                            if (!st.hasQuestItems(4397)) {
                                if (st.hasQuestItems(4289)) {
                                    htmltext = "31001-01.htm";
                                    st.set("cond", "9");
                                    st.playSound("ItemSound.quest_middle");
                                    st.takeItems(4289, 1);
                                    st.addRadar(181472, 7158, -2725);
                                    break;
                                }
                                if (!st.hasQuestItems(4292)) {
                                    htmltext = "31001-03.htm";
                                    st.addRadar(181472, 7158, -2725);
                                    break;
                                }
                                htmltext = "31001-04.htm";
                                break;
                            }
                            htmltext = "31001-05.htm";
                        }
                        break;
                    case 30645:
                        if (cond >= 5 && cond <= 22) {
                            if (!st.hasQuestItems(4399)) {
                                if (st.hasQuestItems(4290)) {
                                    htmltext = "30645-01.htm";
                                    st.set("cond", "13");
                                    st.playSound("ItemSound.quest_middle");
                                    st.takeItems(4290, 1);
                                    st.addRadar(50693, 158674, 376);
                                    break;
                                }
                                if (!st.hasQuestItems(4293)) {
                                    htmltext = "30645-03.htm";
                                    st.addRadar(50693, 158674, 376);
                                    break;
                                }
                                htmltext = "30645-04.htm";
                                break;
                            }
                            htmltext = "30645-05.htm";
                        }
                        break;
                    case 30980:
                        if (!st.hasQuestItems(4288) && cond >= 5 && cond <= 22) {
                            if (!st.hasQuestItems(4291) && !st.hasQuestItems(4398)) {
                                if (st.getInt("angelkiller") == 0) {
                                    htmltext = "30980-01.htm";
                                    if (this._angelKiller == null)
                                        this._angelKiller = addSpawn(27184, npc, false, 0L, true);
                                    if (st.getInt("cond") != 18) {
                                        st.set("cond", "18");
                                        st.playSound("ItemSound.quest_middle");
                                    }
                                    break;
                                }
                                htmltext = "30980-02.htm";
                                st.giveItems(4291, 1);
                                st.playSound("ItemSound.quest_itemget");
                                st.unset("angelkiller");
                                break;
                            }
                            htmltext = "30980-03.htm";
                        }
                        break;
                    case 30977:
                        if (!st.hasQuestItems(4288) && cond >= 5 && cond <= 22) {
                            if (!st.hasQuestItems(4398)) {
                                if (st.hasQuestItems(4291)) {
                                    htmltext = "30977-02.htm";
                                    st.set("cond", "20");
                                    st.playSound("ItemSound.quest_middle");
                                    st.takeItems(4291, 1);
                                    st.giveItems(4398, 1);
                                    if (st.hasQuestItems(4397, 4399))
                                        st.set("cond", "21");
                                    break;
                                }
                                htmltext = "30977-04.htm";
                                break;
                            }
                            htmltext = "30977-03.htm";
                        }
                        break;
                    case 30978:
                        if (!st.hasQuestItems(4289) && cond >= 5 && cond <= 22) {
                            if (!st.hasQuestItems(4397)) {
                                if (!st.hasQuestItems(4292)) {
                                    htmltext = "30978-01.htm";
                                    if (this._elberoth == null)
                                        this._elberoth = addSpawn(27182, npc, false, 0L, true);
                                    break;
                                }
                                htmltext = "30978-02.htm";
                                st.set("cond", "12");
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(4292, 1);
                                st.giveItems(4397, 1);
                                if (st.hasQuestItems(4398, 4399))
                                    st.set("cond", "21");
                                break;
                            }
                            htmltext = "30978-03.htm";
                        }
                        break;
                    case 30979:
                        if (!st.hasQuestItems(4290) && cond >= 5 && cond <= 22) {
                            if (!st.hasQuestItems(4399)) {
                                if (!st.hasQuestItems(4293)) {
                                    htmltext = "30979-01.htm";
                                    if (this._shadowFang == null)
                                        this._shadowFang = addSpawn(27183, npc, false, 0L, true);
                                    break;
                                }
                                htmltext = "30979-02.htm";
                                st.set("cond", "16");
                                st.playSound("ItemSound.quest_middle");
                                st.takeItems(4293, 1);
                                st.giveItems(4399, 1);
                                if (st.hasQuestItems(4398, 4397))
                                    st.set("cond", "21");
                                break;
                            }
                            htmltext = "30979-03.htm";
                        }
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onSpawn(Npc npc) {
        switch (npc.getNpcId()) {
            case 27182:
                npc.broadcastNpcSay("This does not belong to you. Take your hands out!");
                break;
            case 27183:
                npc.broadcastNpcSay("I don't believe it! Grrr!");
                break;
            case 27184:
                npc.broadcastNpcSay("I have the key, do you wish to steal it?");
                break;
        }
        return null;
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        int cond;
        Player player = attacker.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        switch (npc.getNpcId()) {
            case 27182:
                if (npc.getScriptValue() == 0) {
                    npc.broadcastNpcSay("...I feel very sorry, but I have taken your life.");
                    npc.setScriptValue(1);
                }
                break;
            case 27183:
                if (npc.getScriptValue() == 0) {
                    npc.broadcastNpcSay("I will cover this mountain with your blood!");
                    npc.setScriptValue(1);
                }
                break;
            case 27184:
                if (npc.getScriptValue() == 0) {
                    npc.broadcastNpcSay("Haha.. Really amusing! As for the key, search the corpse!");
                    npc.setScriptValue(1);
                }
                if (npc.getCurrentHp() / npc.getMaxHp() < 0.5D) {
                    npc.abortAttack();
                    npc.broadcastNpcSay("Can't get rid of you... Did you get the key from the corpse?");
                    npc.decayMe();
                    st.set("cond", "19");
                    st.set("angelkiller", "1");
                    st.playSound("ItemSound.quest_middle");
                    this._angelKiller = null;
                }
                break;
            case 20828:
            case 20829:
                cond = st.getInt("cond");
                if ((cond == 24 || cond == 25) && st.hasQuestItems(4294)) {
                    int points = st.getInt("points") + ((npc.getNpcId() == 20828) ? 60 : 70);
                    if (points > ((cond == 24) ? 80000 : 100000)) {
                        st.set("points", Integer.toString(0));
                        st.takeItems(4294, 1);
                        st.giveItems(4295, 1);
                        if (cond != 24) {
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                        break;
                    }
                    st.set("points", Integer.toString(points));
                }
                break;
        }
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = checkPlayerState(player, npc, (byte) 1);
        if (st == null)
            return null;
        int cond = st.getInt("cond");
        switch (npc.getNpcId()) {
            case 20657:
            case 20658:
                if (cond == 2)
                    st.dropItems(4287, 1, 1, 100000);
                break;
            case 27182:
                if (cond >= 5 && cond <= 22 && !st.hasQuestItems(4292)) {
                    st.set("cond", "11");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(4292, 1);
                    npc.broadcastNpcSay("Oh, dull-witted.. God, they...");
                }
                this._elberoth = null;
                break;
            case 27183:
                if (cond >= 5 && cond <= 22 && !st.hasQuestItems(4293)) {
                    st.set("cond", "15");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(4293, 1);
                    npc.broadcastNpcSay("You do not know.. Seven seals are.. coughs");
                }
                this._shadowFang = null;
                break;
            case 20828:
            case 20829:
                if ((cond == 24 || cond == 25) && st.hasQuestItems(4294)) {
                    int points = st.getInt("points") + ((npc.getNpcId() == 20828) ? 600 : 700);
                    if (points > ((cond == 24) ? 80000 : 100000)) {
                        st.set("points", Integer.toString(0));
                        st.takeItems(4294, 1);
                        st.giveItems(4295, 1);
                        if (cond != 24) {
                            st.playSound("ItemSound.quest_itemget");
                            break;
                        }
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                        break;
                    }
                    st.set("points", Integer.toString(points));
                }
                break;
            case 20859:
            case 20860:
                if ((cond == 26 || cond == 29) && Rnd.get(4) < 1 && st.hasQuestItems(5232)) {
                    st.playSound("ItemSound.quest_itemget");
                    st.takeItems(5232, 1);
                    st.giveItems(4295, 1);
                }
                break;
            case 27184:
                this._angelKiller = null;
                break;
        }
        return null;
    }
}
