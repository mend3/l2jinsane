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

public class Q426_QuestForFishingShot extends Quest {
    private static final String qn = "Q426_QuestForFishingShot";

    private static final int SWEET_FLUID = 7586;

    private static final Map<Integer, Integer> MOBS1 = new HashMap<>();

    private static final Map<Integer, Integer> MOBS2 = new HashMap<>();

    private static final Map<Integer, Integer> MOBS3 = new HashMap<>();

    private static final Map<Integer, Integer> MOBS4 = new HashMap<>();

    private static final Map<Integer, Integer> MOBS5 = new HashMap<>();

    private static final Map<Integer, int[]> MOBSspecial = new HashMap<>();

    public Q426_QuestForFishingShot() {
        super(426, "Quest for Fishing Shot");
        MOBS1.put(Integer.valueOf(20005), Integer.valueOf(45));
        MOBS1.put(Integer.valueOf(20013), Integer.valueOf(100));
        MOBS1.put(Integer.valueOf(20016), Integer.valueOf(100));
        MOBS1.put(Integer.valueOf(20017), Integer.valueOf(115));
        MOBS1.put(Integer.valueOf(20030), Integer.valueOf(105));
        MOBS1.put(Integer.valueOf(20132), Integer.valueOf(70));
        MOBS1.put(Integer.valueOf(20038), Integer.valueOf(135));
        MOBS1.put(Integer.valueOf(20044), Integer.valueOf(125));
        MOBS1.put(Integer.valueOf(20046), Integer.valueOf(100));
        MOBS1.put(Integer.valueOf(20047), Integer.valueOf(100));
        MOBS1.put(Integer.valueOf(20050), Integer.valueOf(140));
        MOBS1.put(Integer.valueOf(20058), Integer.valueOf(140));
        MOBS1.put(Integer.valueOf(20063), Integer.valueOf(160));
        MOBS1.put(Integer.valueOf(20066), Integer.valueOf(170));
        MOBS1.put(Integer.valueOf(20070), Integer.valueOf(180));
        MOBS1.put(Integer.valueOf(20074), Integer.valueOf(195));
        MOBS1.put(Integer.valueOf(20077), Integer.valueOf(205));
        MOBS1.put(Integer.valueOf(20078), Integer.valueOf(205));
        MOBS1.put(Integer.valueOf(20079), Integer.valueOf(205));
        MOBS1.put(Integer.valueOf(20080), Integer.valueOf(220));
        MOBS1.put(Integer.valueOf(20081), Integer.valueOf(370));
        MOBS1.put(Integer.valueOf(20083), Integer.valueOf(245));
        MOBS1.put(Integer.valueOf(20084), Integer.valueOf(255));
        MOBS1.put(Integer.valueOf(20085), Integer.valueOf(265));
        MOBS1.put(Integer.valueOf(20087), Integer.valueOf(565));
        MOBS1.put(Integer.valueOf(20088), Integer.valueOf(605));
        MOBS1.put(Integer.valueOf(20089), Integer.valueOf(250));
        MOBS1.put(Integer.valueOf(20100), Integer.valueOf(85));
        MOBS1.put(Integer.valueOf(20103), Integer.valueOf(110));
        MOBS1.put(Integer.valueOf(20105), Integer.valueOf(110));
        MOBS1.put(Integer.valueOf(20115), Integer.valueOf(190));
        MOBS1.put(Integer.valueOf(20120), Integer.valueOf(20));
        MOBS1.put(Integer.valueOf(20131), Integer.valueOf(45));
        MOBS1.put(Integer.valueOf(20135), Integer.valueOf(360));
        MOBS1.put(Integer.valueOf(20157), Integer.valueOf(235));
        MOBS1.put(Integer.valueOf(20162), Integer.valueOf(195));
        MOBS1.put(Integer.valueOf(20176), Integer.valueOf(280));
        MOBS1.put(Integer.valueOf(20211), Integer.valueOf(170));
        MOBS1.put(Integer.valueOf(20225), Integer.valueOf(160));
        MOBS1.put(Integer.valueOf(20227), Integer.valueOf(180));
        MOBS1.put(Integer.valueOf(20230), Integer.valueOf(260));
        MOBS1.put(Integer.valueOf(20232), Integer.valueOf(245));
        MOBS1.put(Integer.valueOf(20234), Integer.valueOf(290));
        MOBS1.put(Integer.valueOf(20241), Integer.valueOf(700));
        MOBS1.put(Integer.valueOf(20267), Integer.valueOf(215));
        MOBS1.put(Integer.valueOf(20268), Integer.valueOf(295));
        MOBS1.put(Integer.valueOf(20269), Integer.valueOf(255));
        MOBS1.put(Integer.valueOf(20270), Integer.valueOf(365));
        MOBS1.put(Integer.valueOf(20271), Integer.valueOf(295));
        MOBS1.put(Integer.valueOf(20286), Integer.valueOf(700));
        MOBS1.put(Integer.valueOf(20308), Integer.valueOf(110));
        MOBS1.put(Integer.valueOf(20312), Integer.valueOf(45));
        MOBS1.put(Integer.valueOf(20317), Integer.valueOf(20));
        MOBS1.put(Integer.valueOf(20324), Integer.valueOf(85));
        MOBS1.put(Integer.valueOf(20333), Integer.valueOf(100));
        MOBS1.put(Integer.valueOf(20341), Integer.valueOf(100));
        MOBS1.put(Integer.valueOf(20346), Integer.valueOf(85));
        MOBS1.put(Integer.valueOf(20349), Integer.valueOf(850));
        MOBS1.put(Integer.valueOf(20356), Integer.valueOf(165));
        MOBS1.put(Integer.valueOf(20357), Integer.valueOf(140));
        MOBS1.put(Integer.valueOf(20363), Integer.valueOf(70));
        MOBS1.put(Integer.valueOf(20368), Integer.valueOf(85));
        MOBS1.put(Integer.valueOf(20371), Integer.valueOf(100));
        MOBS1.put(Integer.valueOf(20386), Integer.valueOf(85));
        MOBS1.put(Integer.valueOf(20389), Integer.valueOf(90));
        MOBS1.put(Integer.valueOf(20403), Integer.valueOf(110));
        MOBS1.put(Integer.valueOf(20404), Integer.valueOf(95));
        MOBS1.put(Integer.valueOf(20433), Integer.valueOf(100));
        MOBS1.put(Integer.valueOf(20436), Integer.valueOf(140));
        MOBS1.put(Integer.valueOf(20448), Integer.valueOf(45));
        MOBS1.put(Integer.valueOf(20456), Integer.valueOf(20));
        MOBS1.put(Integer.valueOf(20463), Integer.valueOf(85));
        MOBS1.put(Integer.valueOf(20470), Integer.valueOf(45));
        MOBS1.put(Integer.valueOf(20471), Integer.valueOf(85));
        MOBS1.put(Integer.valueOf(20475), Integer.valueOf(20));
        MOBS1.put(Integer.valueOf(20478), Integer.valueOf(110));
        MOBS1.put(Integer.valueOf(20487), Integer.valueOf(90));
        MOBS1.put(Integer.valueOf(20511), Integer.valueOf(100));
        MOBS1.put(Integer.valueOf(20525), Integer.valueOf(20));
        MOBS1.put(Integer.valueOf(20528), Integer.valueOf(100));
        MOBS1.put(Integer.valueOf(20536), Integer.valueOf(15));
        MOBS1.put(Integer.valueOf(20537), Integer.valueOf(15));
        MOBS1.put(Integer.valueOf(20538), Integer.valueOf(15));
        MOBS1.put(Integer.valueOf(20539), Integer.valueOf(15));
        MOBS1.put(Integer.valueOf(20544), Integer.valueOf(15));
        MOBS1.put(Integer.valueOf(20550), Integer.valueOf(300));
        MOBS1.put(Integer.valueOf(20551), Integer.valueOf(300));
        MOBS1.put(Integer.valueOf(20552), Integer.valueOf(650));
        MOBS1.put(Integer.valueOf(20553), Integer.valueOf(335));
        MOBS1.put(Integer.valueOf(20554), Integer.valueOf(390));
        MOBS1.put(Integer.valueOf(20555), Integer.valueOf(350));
        MOBS1.put(Integer.valueOf(20557), Integer.valueOf(390));
        MOBS1.put(Integer.valueOf(20559), Integer.valueOf(420));
        MOBS1.put(Integer.valueOf(20560), Integer.valueOf(440));
        MOBS1.put(Integer.valueOf(20562), Integer.valueOf(485));
        MOBS1.put(Integer.valueOf(20573), Integer.valueOf(545));
        MOBS1.put(Integer.valueOf(20575), Integer.valueOf(645));
        MOBS1.put(Integer.valueOf(20630), Integer.valueOf(350));
        MOBS1.put(Integer.valueOf(20632), Integer.valueOf(475));
        MOBS1.put(Integer.valueOf(20634), Integer.valueOf(960));
        MOBS1.put(Integer.valueOf(20636), Integer.valueOf(495));
        MOBS1.put(Integer.valueOf(20638), Integer.valueOf(540));
        MOBS1.put(Integer.valueOf(20641), Integer.valueOf(680));
        MOBS1.put(Integer.valueOf(20643), Integer.valueOf(660));
        MOBS1.put(Integer.valueOf(20644), Integer.valueOf(645));
        MOBS1.put(Integer.valueOf(20659), Integer.valueOf(440));
        MOBS1.put(Integer.valueOf(20661), Integer.valueOf(575));
        MOBS1.put(Integer.valueOf(20663), Integer.valueOf(525));
        MOBS1.put(Integer.valueOf(20665), Integer.valueOf(680));
        MOBS1.put(Integer.valueOf(20667), Integer.valueOf(730));
        MOBS1.put(Integer.valueOf(20766), Integer.valueOf(210));
        MOBS1.put(Integer.valueOf(20781), Integer.valueOf(270));
        MOBS1.put(Integer.valueOf(20783), Integer.valueOf(140));
        MOBS1.put(Integer.valueOf(20784), Integer.valueOf(155));
        MOBS1.put(Integer.valueOf(20786), Integer.valueOf(170));
        MOBS1.put(Integer.valueOf(20788), Integer.valueOf(325));
        MOBS1.put(Integer.valueOf(20790), Integer.valueOf(390));
        MOBS1.put(Integer.valueOf(20792), Integer.valueOf(620));
        MOBS1.put(Integer.valueOf(20794), Integer.valueOf(635));
        MOBS1.put(Integer.valueOf(20796), Integer.valueOf(640));
        MOBS1.put(Integer.valueOf(20798), Integer.valueOf(850));
        MOBS1.put(Integer.valueOf(20800), Integer.valueOf(740));
        MOBS1.put(Integer.valueOf(20802), Integer.valueOf(900));
        MOBS1.put(Integer.valueOf(20804), Integer.valueOf(775));
        MOBS1.put(Integer.valueOf(20806), Integer.valueOf(805));
        MOBS1.put(Integer.valueOf(20833), Integer.valueOf(455));
        MOBS1.put(Integer.valueOf(20834), Integer.valueOf(680));
        MOBS1.put(Integer.valueOf(20836), Integer.valueOf(785));
        MOBS1.put(Integer.valueOf(20837), Integer.valueOf(835));
        MOBS1.put(Integer.valueOf(20839), Integer.valueOf(430));
        MOBS1.put(Integer.valueOf(20841), Integer.valueOf(460));
        MOBS1.put(Integer.valueOf(20845), Integer.valueOf(605));
        MOBS1.put(Integer.valueOf(20847), Integer.valueOf(570));
        MOBS1.put(Integer.valueOf(20849), Integer.valueOf(585));
        MOBS1.put(Integer.valueOf(20936), Integer.valueOf(290));
        MOBS1.put(Integer.valueOf(20937), Integer.valueOf(315));
        MOBS1.put(Integer.valueOf(20939), Integer.valueOf(385));
        MOBS1.put(Integer.valueOf(20940), Integer.valueOf(500));
        MOBS1.put(Integer.valueOf(20941), Integer.valueOf(460));
        MOBS1.put(Integer.valueOf(20943), Integer.valueOf(345));
        MOBS1.put(Integer.valueOf(20944), Integer.valueOf(335));
        MOBS1.put(Integer.valueOf(21100), Integer.valueOf(125));
        MOBS1.put(Integer.valueOf(21101), Integer.valueOf(155));
        MOBS1.put(Integer.valueOf(21103), Integer.valueOf(215));
        MOBS1.put(Integer.valueOf(21105), Integer.valueOf(310));
        MOBS1.put(Integer.valueOf(21107), Integer.valueOf(600));
        MOBS1.put(Integer.valueOf(21117), Integer.valueOf(120));
        MOBS1.put(Integer.valueOf(21023), Integer.valueOf(170));
        MOBS1.put(Integer.valueOf(21024), Integer.valueOf(175));
        MOBS1.put(Integer.valueOf(21025), Integer.valueOf(185));
        MOBS1.put(Integer.valueOf(21026), Integer.valueOf(200));
        MOBS1.put(Integer.valueOf(21034), Integer.valueOf(195));
        MOBS1.put(Integer.valueOf(21125), Integer.valueOf(12));
        MOBS1.put(Integer.valueOf(21263), Integer.valueOf(650));
        MOBS1.put(Integer.valueOf(21520), Integer.valueOf(880));
        MOBS1.put(Integer.valueOf(21526), Integer.valueOf(970));
        MOBS1.put(Integer.valueOf(21536), Integer.valueOf(985));
        MOBS1.put(Integer.valueOf(21602), Integer.valueOf(555));
        MOBS1.put(Integer.valueOf(21603), Integer.valueOf(750));
        MOBS1.put(Integer.valueOf(21605), Integer.valueOf(620));
        MOBS1.put(Integer.valueOf(21606), Integer.valueOf(875));
        MOBS1.put(Integer.valueOf(21611), Integer.valueOf(590));
        MOBS1.put(Integer.valueOf(21612), Integer.valueOf(835));
        MOBS1.put(Integer.valueOf(21617), Integer.valueOf(615));
        MOBS1.put(Integer.valueOf(21618), Integer.valueOf(875));
        MOBS1.put(Integer.valueOf(21635), Integer.valueOf(775));
        MOBS1.put(Integer.valueOf(21638), Integer.valueOf(165));
        MOBS1.put(Integer.valueOf(21639), Integer.valueOf(185));
        MOBS1.put(Integer.valueOf(21641), Integer.valueOf(195));
        MOBS1.put(Integer.valueOf(21644), Integer.valueOf(170));
        MOBS2.put(Integer.valueOf(20579), Integer.valueOf(420));
        MOBS2.put(Integer.valueOf(20639), Integer.valueOf(280));
        MOBS2.put(Integer.valueOf(20646), Integer.valueOf(145));
        MOBS2.put(Integer.valueOf(20648), Integer.valueOf(120));
        MOBS2.put(Integer.valueOf(20650), Integer.valueOf(460));
        MOBS2.put(Integer.valueOf(20651), Integer.valueOf(260));
        MOBS2.put(Integer.valueOf(20652), Integer.valueOf(335));
        MOBS2.put(Integer.valueOf(20657), Integer.valueOf(630));
        MOBS2.put(Integer.valueOf(20658), Integer.valueOf(570));
        MOBS2.put(Integer.valueOf(20808), Integer.valueOf(50));
        MOBS2.put(Integer.valueOf(20809), Integer.valueOf(865));
        MOBS2.put(Integer.valueOf(20832), Integer.valueOf(700));
        MOBS2.put(Integer.valueOf(20979), Integer.valueOf(980));
        MOBS2.put(Integer.valueOf(20991), Integer.valueOf(665));
        MOBS2.put(Integer.valueOf(20994), Integer.valueOf(590));
        MOBS2.put(Integer.valueOf(21261), Integer.valueOf(170));
        MOBS2.put(Integer.valueOf(21263), Integer.valueOf(795));
        MOBS2.put(Integer.valueOf(21508), Integer.valueOf(100));
        MOBS2.put(Integer.valueOf(21510), Integer.valueOf(280));
        MOBS2.put(Integer.valueOf(21511), Integer.valueOf(995));
        MOBS2.put(Integer.valueOf(21512), Integer.valueOf(995));
        MOBS2.put(Integer.valueOf(21514), Integer.valueOf(185));
        MOBS2.put(Integer.valueOf(21516), Integer.valueOf(495));
        MOBS2.put(Integer.valueOf(21517), Integer.valueOf(495));
        MOBS2.put(Integer.valueOf(21518), Integer.valueOf(255));
        MOBS2.put(Integer.valueOf(21636), Integer.valueOf(950));
        MOBS3.put(Integer.valueOf(20655), Integer.valueOf(110));
        MOBS3.put(Integer.valueOf(20656), Integer.valueOf(150));
        MOBS3.put(Integer.valueOf(20772), Integer.valueOf(105));
        MOBS3.put(Integer.valueOf(20810), Integer.valueOf(50));
        MOBS3.put(Integer.valueOf(20812), Integer.valueOf(490));
        MOBS3.put(Integer.valueOf(20814), Integer.valueOf(775));
        MOBS3.put(Integer.valueOf(20816), Integer.valueOf(875));
        MOBS3.put(Integer.valueOf(20819), Integer.valueOf(280));
        MOBS3.put(Integer.valueOf(20955), Integer.valueOf(670));
        MOBS3.put(Integer.valueOf(20978), Integer.valueOf(555));
        MOBS3.put(Integer.valueOf(21058), Integer.valueOf(355));
        MOBS3.put(Integer.valueOf(21060), Integer.valueOf(45));
        MOBS3.put(Integer.valueOf(21075), Integer.valueOf(110));
        MOBS3.put(Integer.valueOf(21078), Integer.valueOf(610));
        MOBS3.put(Integer.valueOf(21081), Integer.valueOf(955));
        MOBS3.put(Integer.valueOf(21264), Integer.valueOf(920));
        MOBS4.put(Integer.valueOf(20815), Integer.valueOf(205));
        MOBS4.put(Integer.valueOf(20822), Integer.valueOf(100));
        MOBS4.put(Integer.valueOf(20824), Integer.valueOf(665));
        MOBS4.put(Integer.valueOf(20825), Integer.valueOf(620));
        MOBS4.put(Integer.valueOf(20983), Integer.valueOf(205));
        MOBS4.put(Integer.valueOf(21314), Integer.valueOf(145));
        MOBS4.put(Integer.valueOf(21316), Integer.valueOf(235));
        MOBS4.put(Integer.valueOf(21318), Integer.valueOf(280));
        MOBS4.put(Integer.valueOf(21320), Integer.valueOf(355));
        MOBS4.put(Integer.valueOf(21322), Integer.valueOf(430));
        MOBS4.put(Integer.valueOf(21376), Integer.valueOf(280));
        MOBS4.put(Integer.valueOf(21378), Integer.valueOf(375));
        MOBS4.put(Integer.valueOf(21380), Integer.valueOf(375));
        MOBS4.put(Integer.valueOf(21387), Integer.valueOf(640));
        MOBS4.put(Integer.valueOf(21393), Integer.valueOf(935));
        MOBS4.put(Integer.valueOf(21395), Integer.valueOf(855));
        MOBS4.put(Integer.valueOf(21652), Integer.valueOf(375));
        MOBS4.put(Integer.valueOf(21655), Integer.valueOf(640));
        MOBS4.put(Integer.valueOf(21657), Integer.valueOf(935));
        MOBS5.put(Integer.valueOf(20828), Integer.valueOf(935));
        MOBS5.put(Integer.valueOf(21061), Integer.valueOf(530));
        MOBS5.put(Integer.valueOf(21069), Integer.valueOf(825));
        MOBS5.put(Integer.valueOf(21382), Integer.valueOf(125));
        MOBS5.put(Integer.valueOf(21384), Integer.valueOf(400));
        MOBS5.put(Integer.valueOf(21390), Integer.valueOf(750));
        MOBS5.put(Integer.valueOf(21654), Integer.valueOf(400));
        MOBS5.put(Integer.valueOf(21656), Integer.valueOf(750));
        MOBSspecial.put(Integer.valueOf(20829), new int[]{115, 6});
        MOBSspecial.put(Integer.valueOf(20859), new int[]{890, 8});
        MOBSspecial.put(Integer.valueOf(21066), new int[]{5, 5});
        MOBSspecial.put(Integer.valueOf(21068), new int[]{565, 11});
        MOBSspecial.put(Integer.valueOf(21071), new int[]{400, 12});
        setItemsIds(7586);
        addStartNpc(31562, 31563, 31564, 31565, 31566, 31567, 31568, 31569, 31570, 31571,
                31572, 31573, 31574, 31575, 31576, 31577, 31578, 31579, 31696, 31697,
                31989, 32007);
        addTalkId(31562, 31563, 31564, 31565, 31566, 31567, 31568, 31569, 31570, 31571,
                31572, 31573, 31574, 31575, 31576, 31577, 31578, 31579, 31696, 31697,
                31989, 32007);
        Iterator<Integer> iterator;
        for (iterator = MOBS1.keySet().iterator(); iterator.hasNext(); ) {
            int mob = iterator.next();
            addKillId(mob);
        }
        for (iterator = MOBS2.keySet().iterator(); iterator.hasNext(); ) {
            int mob = iterator.next();
            addKillId(mob);
        }
        for (iterator = MOBS3.keySet().iterator(); iterator.hasNext(); ) {
            int mob = iterator.next();
            addKillId(mob);
        }
        for (iterator = MOBS4.keySet().iterator(); iterator.hasNext(); ) {
            int mob = iterator.next();
            addKillId(mob);
        }
        for (iterator = MOBS5.keySet().iterator(); iterator.hasNext(); ) {
            int mob = iterator.next();
            addKillId(mob);
        }
        for (iterator = MOBSspecial.keySet().iterator(); iterator.hasNext(); ) {
            int mob = iterator.next();
            addKillId(mob);
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q426_QuestForFishingShot");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("03.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("08.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st = player.getQuestState("Q426_QuestForFishingShot");
        String htmltext = getNoQuestMsg();
        if (st == null)
            st = newQuestState(player);
        switch (st.getState()) {
            case 0:
                htmltext = "01.htm";
                break;
            case 1:
                htmltext = st.hasQuestItems(7586) ? "05.htm" : "04.htm";
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
        int drop = 0;
        int chance = 0;
        if (MOBS1.containsKey(Integer.valueOf(npcId))) {
            chance = MOBS1.get(Integer.valueOf(npcId));
        } else if (MOBS2.containsKey(Integer.valueOf(npcId))) {
            chance = MOBS2.get(Integer.valueOf(npcId));
            drop = 1;
        } else if (MOBS3.containsKey(Integer.valueOf(npcId))) {
            chance = MOBS3.get(Integer.valueOf(npcId));
            drop = 2;
        } else if (MOBS4.containsKey(Integer.valueOf(npcId))) {
            chance = MOBS4.get(Integer.valueOf(npcId));
            drop = 3;
        } else if (MOBS5.containsKey(Integer.valueOf(npcId))) {
            chance = MOBS5.get(Integer.valueOf(npcId));
            drop = 4;
        } else if (MOBSspecial.containsKey(Integer.valueOf(npcId))) {
            chance = ((int[]) MOBSspecial.get(Integer.valueOf(npcId)))[0];
            drop = ((int[]) MOBSspecial.get(Integer.valueOf(npcId)))[1];
        }
        if (Rnd.get(1000) <= chance)
            drop++;
        if (drop != 0) {
            st.playSound("ItemSound.quest_itemget");
            st.rewardItems(7586, drop);
        }
        return null;
    }
}
