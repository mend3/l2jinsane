package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q296_TarantulasSpiderSilk extends Quest {
    private static final String qn = "Q296_TarantulasSpiderSilk";

    private static final int MION = 30519;

    private static final int DEFENDER_NATHAN = 30548;

    private static final int TARANTULA_SPIDER_SILK = 1493;

    private static final int TARANTULA_SPINNERETTE = 1494;

    private static final int RING_OF_RACCOON = 1508;

    private static final int RING_OF_FIREFLY = 1509;

    public Q296_TarantulasSpiderSilk() {
        super(296, "Tarantula's Spider Silk");
        setItemsIds(1493, 1494);
        addStartNpc(30519);
        addTalkId(30519, 30548);
        addKillId(20394, 20403, 20508);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q296_TarantulasSpiderSilk");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30519-03.htm")) {
            if (st.hasAtLeastOneQuestItem(1508, 1509)) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "30519-03a.htm";
            }
        } else if (event.equalsIgnoreCase("30519-06.htm")) {
            st.takeItems(1493, -1);
            st.takeItems(1494, -1);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
        } else if (event.equalsIgnoreCase("30548-02.htm")) {
            int count = st.getQuestItemsCount(1494);
            if (count > 0) {
                htmltext = "30548-03.htm";
                st.takeItems(1494, -1);
                st.giveItems(1493, count * (15 + Rnd.get(10)));
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int count;
        QuestState st = player.getQuestState("Q296_TarantulasSpiderSilk");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 15) ? "30519-01.htm" : "30519-02.htm";
                break;
            case 1:
                switch (npc.getNpcId()) {
                    case 30519:
                        count = st.getQuestItemsCount(1493);
                        if (count == 0) {
                            htmltext = "30519-04.htm";
                            break;
                        }
                        htmltext = "30519-05.htm";
                        st.takeItems(1493, -1);
                        st.rewardItems(57, ((count >= 10) ? 2000 : 0) + count * 30);
                        break;
                    case 30548:
                        htmltext = "30548-01.htm";
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
        int rnd = Rnd.get(100);
        if (rnd > 95) {
            st.dropItemsAlways(1494, 1, 0);
        } else if (rnd > 45) {
            st.dropItemsAlways(1493, 1, 0);
        }
        return null;
    }
}
