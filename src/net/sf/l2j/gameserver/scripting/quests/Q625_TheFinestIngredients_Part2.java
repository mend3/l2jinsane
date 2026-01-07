package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.RaidBossManager;
import net.sf.l2j.gameserver.enums.BossStatus;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.spawn.BossSpawn;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q625_TheFinestIngredients_Part2 extends Quest {
    private static final String qn = "Q625_TheFinestIngredients_Part2";

    private static final int ICICLE_EMPEROR_BUMBALUMP = 25296;

    private static final int JEREMY = 31521;

    private static final int YETI_TABLE = 31542;

    private static final int SOY_SAUCE_JAR = 7205;

    private static final int FOOD_FOR_BUMBALUMP = 7209;

    private static final int SPECIAL_YETI_MEAT = 7210;

    private static final int[] REWARDS = new int[]{4589, 4590, 4591, 4592, 4593, 4594};

    private static final int CHECK_INTERVAL = 600000;

    private static final int IDLE_INTERVAL = 3;

    private Npc _npc = null;

    private int _status = -1;

    public Q625_TheFinestIngredients_Part2() {
        super(625, "The Finest Ingredients - Part 2");
        setItemsIds(7209, 7210);
        addStartNpc(31521);
        addTalkId(31521, 31542);
        addAttackId(25296);
        addKillId(25296);
        switch (RaidBossManager.getInstance().getStatus(25296)) {
            case ALIVE:
                spawnNpc();
            case DEAD:
                startQuestTimer("check", 600000L, null, null, true);
                break;
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equals("check")) {
            BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(25296);
            if (bs != null && bs.getStatus() == BossStatus.ALIVE) {
                Npc raid = bs.getBoss();
                if (this._status >= 0 && this._status-- == 0)
                    despawnRaid(raid);
                spawnNpc();
            }
            return null;
        }
        String htmltext = event;
        QuestState st = player.getQuestState("Q625_TheFinestIngredients_Part2");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31521-03.htm")) {
            if (st.hasQuestItems(7205)) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
                st.takeItems(7205, 1);
                st.giveItems(7209, 1);
            } else {
                htmltext = "31521-04.htm";
            }
        } else if (event.equalsIgnoreCase("31521-08.htm")) {
            if (st.hasQuestItems(7210)) {
                st.takeItems(7210, 1);
                st.rewardItems(Rnd.get(REWARDS), 5);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                htmltext = "31521-09.htm";
            }
        } else if (event.equalsIgnoreCase("31542-02.htm")) {
            if (st.hasQuestItems(7209)) {
                if (this._status < 0) {
                    if (spawnRaid()) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(7209, 1);
                    }
                } else {
                    htmltext = "31542-04.htm";
                }
            } else {
                htmltext = "31542-03.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q625_TheFinestIngredients_Part2");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                htmltext = (player.getLevel() < 73) ? "31521-02.htm" : "31521-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31521:
                        if (cond == 1) {
                            htmltext = "31521-05.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "31521-06.htm";
                            break;
                        }
                        htmltext = "31521-07.htm";
                        break;
                    case 31542:
                        if (cond == 1) {
                            htmltext = "31542-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31542-05.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        Player player = attacker.getActingPlayer();
        if (player != null)
            this._status = 3;
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        if (player != null)
            for (QuestState st : getPartyMembers(player, npc, "cond", "2")) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
                st.giveItems(7210, 1);
            }
        npc.broadcastNpcSay("Oooh!");
        despawnRaid(npc);
        if (this._npc != null) {
            this._npc.deleteMe();
            this._npc = null;
        }
        return null;
    }

    private void spawnNpc() {
        if (this._npc == null)
            this._npc = addSpawn(31542, 157136, -121456, -2363, 40000, false, 0L, false);
    }

    private boolean spawnRaid() {
        BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(25296);
        if (bs != null && bs.getStatus() == BossStatus.ALIVE) {
            Npc raid = bs.getBoss();
            raid.getSpawn().setLoc(157117, -121939, -2397, Rnd.get(65536));
            raid.teleportTo(157117, -121939, -2397, 100);
            raid.broadcastNpcSay("I smell something delicious...");
            this._status = 3;
            return true;
        }
        return false;
    }

    private void despawnRaid(Npc raid) {
        raid.getSpawn().setLoc(-104700, -252700, -15542, 0);
        if (!raid.isDead())
            raid.teleportTo(-104700, -252700, -15542, 0);
        this._status = -1;
    }
}
