package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q105_SkirmishWithTheOrcs extends Quest {
    private static final String qn = "Q105_SkirmishWithTheOrcs";

    private static final int KENDELL_ORDER_1 = 1836;

    private static final int KENDELL_ORDER_2 = 1837;

    private static final int KENDELL_ORDER_3 = 1838;

    private static final int KENDELL_ORDER_4 = 1839;

    private static final int KENDELL_ORDER_5 = 1840;

    private static final int KENDELL_ORDER_6 = 1841;

    private static final int KENDELL_ORDER_7 = 1842;

    private static final int KENDELL_ORDER_8 = 1843;

    private static final int KABOO_CHIEF_TORC_1 = 1844;

    private static final int KABOO_CHIEF_TORC_2 = 1845;

    private static final int KABOO_CHIEF_UOPH = 27059;

    private static final int KABOO_CHIEF_KRACHA = 27060;

    private static final int KABOO_CHIEF_BATOH = 27061;

    private static final int KABOO_CHIEF_TANUKIA = 27062;

    private static final int KABOO_CHIEF_TUREL = 27064;

    private static final int KABOO_CHIEF_ROKO = 27065;

    private static final int KABOO_CHIEF_KAMUT = 27067;

    private static final int KABOO_CHIEF_MURTIKA = 27068;

    private static final int SPIRITSHOT_FOR_BEGINNERS = 5790;

    private static final int SOULSHOT_FOR_BEGINNERS = 5789;

    private static final int RED_SUNSET_STAFF = 754;

    private static final int RED_SUNSET_SWORD = 981;

    private static final int ECHO_BATTLE = 4412;

    private static final int ECHO_LOVE = 4413;

    private static final int ECHO_SOLITUDE = 4414;

    private static final int ECHO_FEAST = 4415;

    private static final int ECHO_CELEBRATION = 4416;

    public Q105_SkirmishWithTheOrcs() {
        super(105, "Skirmish with the Orcs");
        setItemsIds(1836, 1837, 1838, 1839, 1840, 1841, 1842, 1843, 1844, 1845);
        addStartNpc(30218);
        addTalkId(30218);
        addKillId(27059, 27060, 27061, 27062, 27064, 27065, 27067, 27068);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q105_SkirmishWithTheOrcs");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30218-03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.giveItems(Rnd.get(1836, 1839), 1);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q105_SkirmishWithTheOrcs");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() != ClassRace.ELF) {
                    htmltext = "30218-00.htm";
                    break;
                }
                if (player.getLevel() < 10) {
                    htmltext = "30218-01.htm";
                    break;
                }
                htmltext = "30218-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "30218-05.htm";
                    break;
                }
                if (cond == 2) {
                    htmltext = "30218-06.htm";
                    st.set("cond", "3");
                    st.playSound("ItemSound.quest_middle");
                    st.takeItems(1844, 1);
                    st.takeItems(1836, 1);
                    st.takeItems(1837, 1);
                    st.takeItems(1838, 1);
                    st.takeItems(1839, 1);
                    st.giveItems(Rnd.get(1840, 1843), 1);
                    break;
                }
                if (cond == 3) {
                    htmltext = "30218-07.htm";
                    break;
                }
                if (cond == 4) {
                    htmltext = "30218-08.htm";
                    st.takeItems(1845, 1);
                    st.takeItems(1840, 1);
                    st.takeItems(1841, 1);
                    st.takeItems(1842, 1);
                    st.takeItems(1843, 1);
                    if (player.isMageClass()) {
                        st.giveItems(754, 1);
                    } else {
                        st.giveItems(981, 1);
                    }
                    if (player.isNewbie()) {
                        st.showQuestionMark(26);
                        if (player.isMageClass()) {
                            st.playTutorialVoice("tutorial_voice_027");
                            st.giveItems(5790, 3000);
                        } else {
                            st.playTutorialVoice("tutorial_voice_026");
                            st.giveItems(5789, 7000);
                        }
                    }
                    st.giveItems(4412, 10);
                    st.giveItems(4413, 10);
                    st.giveItems(4414, 10);
                    st.giveItems(4415, 10);
                    st.giveItems(4416, 10);
                    player.broadcastPacket(new SocialAction(player, 3));
                    st.playSound("ItemSound.quest_finish");
                    st.exitQuest(false);
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
            case 27059:
            case 27060:
            case 27061:
            case 27062:
                if (st.getInt("cond") == 1 && st.hasQuestItems(npc.getNpcId() - 25223)) {
                    st.set("cond", "2");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(1844, 1);
                }
                break;
            case 27064:
            case 27065:
                if (st.getInt("cond") == 3 && st.hasQuestItems(npc.getNpcId() - 25224)) {
                    st.set("cond", "4");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(1845, 1);
                }
                break;
            case 27067:
            case 27068:
                if (st.getInt("cond") == 3 && st.hasQuestItems(npc.getNpcId() - 25225)) {
                    st.set("cond", "4");
                    st.playSound("ItemSound.quest_middle");
                    st.giveItems(1845, 1);
                }
                break;
        }
        return null;
    }
}
