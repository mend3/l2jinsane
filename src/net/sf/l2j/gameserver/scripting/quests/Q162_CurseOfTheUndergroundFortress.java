package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.HashMap;
import java.util.Map;

public class Q162_CurseOfTheUndergroundFortress extends Quest {
    private static final String qn = "Q162_CurseOfTheUndergroundFortress";

    private static final int SHADE_HORROR = 20033;

    private static final int DARK_TERROR = 20345;

    private static final int MIST_TERROR = 20371;

    private static final int DUNGEON_SKELETON_ARCHER = 20463;

    private static final int DUNGEON_SKELETON = 20464;

    private static final int DREAD_SOLDIER = 20504;

    private static final int BONE_FRAGMENT = 1158;

    private static final int ELF_SKULL = 1159;

    private static final int BONE_SHIELD = 625;

    private static final Map<Integer, Integer> CHANCES = new HashMap<>();

    public Q162_CurseOfTheUndergroundFortress() {
        super(162, "Curse of the Underground Fortress");
        CHANCES.put(20033, 250000);
        CHANCES.put(20345, 260000);
        CHANCES.put(20371, 230000);
        CHANCES.put(20463, 250000);
        CHANCES.put(20464, 230000);
        CHANCES.put(20504, 260000);
        setItemsIds(1158, 1159);
        addStartNpc(30147);
        addTalkId(30147);
        addKillId(20033, 20345, 20371, 20463, 20464, 20504);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        String htmltext = event;
        QuestState st = player.getQuestState("Q162_CurseOfTheUndergroundFortress");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("30147-04.htm")) {
            st.setState((byte) 1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        QuestState st = player.getQuestState("Q162_CurseOfTheUndergroundFortress");
        String htmltext = getNoQuestMsg();
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (player.getRace() == ClassRace.DARK_ELF) {
                    htmltext = "30147-00.htm";
                    break;
                }
                if (player.getLevel() < 12) {
                    htmltext = "30147-01.htm";
                    break;
                }
                htmltext = "30147-02.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                if (cond == 1) {
                    htmltext = "30147-05.htm";
                    break;
                }
                if (cond == 2) {
                    htmltext = "30147-06.htm";
                    st.takeItems(1159, -1);
                    st.takeItems(1158, -1);
                    st.giveItems(625, 1);
                    st.rewardItems(57, 24000);
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
        QuestState st = checkPlayerCondition(player, npc, "cond", "1");
        if (st == null)
            return null;
        int npcId = npc.getNpcId();
        switch (npcId) {
            case 20463:
            case 20464:
            case 20504:
                if (st.dropItems(1158, 1, 10, CHANCES.get(npcId)) && st.getQuestItemsCount(1159) >= 3)
                    st.set("cond", "2");
                break;
            case 20033:
            case 20345:
            case 20371:
                if (st.dropItems(1159, 1, 3, CHANCES.get(npcId)) && st.getQuestItemsCount(1158) >= 10)
                    st.set("cond", "2");
                break;
        }
        return null;
    }
}
