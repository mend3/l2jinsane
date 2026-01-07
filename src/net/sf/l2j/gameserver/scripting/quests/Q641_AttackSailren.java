package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public final class Q641_AttackSailren extends Quest {
    private static final String qn = "Q641_AttackSailren";

    private static final int STATUE = 32109;

    private static final int GAZKH_FRAGMENT = 8782;

    private static final int GAZKH = 8784;

    public Q641_AttackSailren() {
        super(641, "Attack Sailren!");
        setItemsIds(8782);
        addStartNpc(32109);
        addTalkId(32109);
        addKillId(22196, 22197, 22198, 22199, 22218, 22223);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q641_AttackSailren");
        if (st == null)
            return null;
        if (event.equalsIgnoreCase("32109-5.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        } else if (event.equalsIgnoreCase("32109-8.htm")) {
            if (st.getQuestItemsCount(8782) >= 30) {
                npc.broadcastPacket(new MagicSkillUse(npc, player, 5089, 1, 3000, 0));
                st.takeItems(8782, -1);
                st.giveItems(8784, 1);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                htmltext = "32109-6.htm";
                st.set("cond", "1");
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        QuestState st2;
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q641_AttackSailren");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getLevel() < 77) {
                    htmltext = "32109-3.htm";
                    break;
                }
                st2 = player.getQuestState("Q126_TheNameOfEvil_2");
                htmltext = (st2 != null && st2.isCompleted()) ? "32109-1.htm" : "32109-2.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "32109-5.htm";
                    break;
                }
                if (cond == 2)
                    htmltext = "32109-7.htm";
                break;
        }
        return htmltext;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        QuestState st = getRandomPartyMember(player, npc, "cond", "1");
        if (st == null)
            return null;
        if (st.dropItems(8782, 1, 30, 50000))
            st.set("cond", "2");
        return null;
    }
}
