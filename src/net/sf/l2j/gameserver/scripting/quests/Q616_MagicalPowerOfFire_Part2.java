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

public class Q616_MagicalPowerOfFire_Part2 extends Quest {
    private static final String qn = "Q616_MagicalPowerOfFire_Part2";

    private static final int SOUL_OF_FIRE_NASTRON = 25306;

    private static final int UDAN_MARDUI = 31379;

    private static final int KETRAS_HOLY_ALTAR = 31558;

    private static final int RED_TOTEM = 7243;

    private static final int FIRE_HEART_OF_NASTRON = 7244;

    private static final int CHECK_INTERVAL = 600000;

    private static final int IDLE_INTERVAL = 2;

    private Npc _npc = null;

    private int _status = -1;

    public Q616_MagicalPowerOfFire_Part2() {
        super(616, "Magical Power of Fire - Part 2");
        setItemsIds(7244);
        addStartNpc(31379);
        addTalkId(31379, 31558);
        addAttackId(25306);
        addKillId(25306);
        switch (RaidBossManager.getInstance().getStatus(25306)) {
            case ALIVE:
                spawnNpc();
            case DEAD:
                startQuestTimer("check", 600000L, null, null, true);
                break;
        }
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equals("check")) {
            BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(25306);
            if (bs != null && bs.getStatus() == BossStatus.ALIVE) {
                Npc raid = bs.getBoss();
                if (this._status >= 0 && this._status-- == 0)
                    despawnRaid(raid);
                spawnNpc();
            }
            return null;
        }
        String htmltext = event;
        QuestState st = player.getQuestState("Q616_MagicalPowerOfFire_Part2");
        if (st == null)
            return htmltext;
        if (event.equalsIgnoreCase("31379-04.htm")) {
            if (st.hasQuestItems(7243)) {
                st.setState((byte) 1);
                st.set("cond", "1");
                st.playSound("ItemSound.quest_accept");
            } else {
                htmltext = "31379-02.htm";
            }
        } else if (event.equalsIgnoreCase("31379-08.htm")) {
            if (st.hasQuestItems(7244)) {
                st.takeItems(7244, 1);
                st.rewardExpAndSp(10000L, 0);
                st.playSound("ItemSound.quest_finish");
                st.exitQuest(true);
            } else {
                htmltext = "31379-09.htm";
            }
        } else if (event.equalsIgnoreCase("31558-02.htm")) {
            if (st.hasQuestItems(7243)) {
                if (this._status < 0) {
                    if (spawnRaid()) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(7243, 1);
                    }
                } else {
                    htmltext = "31558-04.htm";
                }
            } else {
                htmltext = "31558-03.htm";
            }
        }
        return htmltext;
    }

    public String onTalk(Npc npc, Player player) {
        int cond;
        String htmltext = getNoQuestMsg();
        QuestState st = player.getQuestState("Q616_MagicalPowerOfFire_Part2");
        if (st == null)
            return htmltext;
        switch (st.getState()) {
            case 0:
                if (!st.hasQuestItems(7243)) {
                    htmltext = "31379-02.htm";
                    break;
                }
                if (player.getLevel() < 75 && player.getAllianceWithVarkaKetra() > -2) {
                    htmltext = "31379-03.htm";
                    break;
                }
                htmltext = "31379-01.htm";
                break;
            case 1:
                cond = st.getInt("cond");
                switch (npc.getNpcId()) {
                    case 31379:
                        if (cond == 1) {
                            htmltext = "31379-05.htm";
                            break;
                        }
                        if (cond == 2) {
                            htmltext = "31379-06.htm";
                            break;
                        }
                        htmltext = "31379-07.htm";
                        break;
                    case 31558:
                        if (cond == 1) {
                            htmltext = "31558-01.htm";
                            break;
                        }
                        if (cond == 2)
                            htmltext = "31558-05.htm";
                        break;
                }
                break;
        }
        return htmltext;
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        Player player = attacker.getActingPlayer();
        if (player != null)
            this._status = 2;
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        Player player = killer.getActingPlayer();
        if (player != null)
            for (QuestState st : getPartyMembers(player, npc, "cond", "2")) {
                st.set("cond", "3");
                st.playSound("ItemSound.quest_middle");
                st.giveItems(7244, 1);
            }
        despawnRaid(npc);
        if (this._npc != null) {
            this._npc.deleteMe();
            this._npc = null;
        }
        return null;
    }

    private void spawnNpc() {
        if (this._npc == null)
            this._npc = addSpawn(31558, 142368, -82512, -6487, 58000, false, 0L, false);
    }

    private boolean spawnRaid() {
        BossSpawn bs = RaidBossManager.getInstance().getBossSpawn(25306);
        if (bs != null && bs.getStatus() == BossStatus.ALIVE) {
            Npc raid = bs.getBoss();
            raid.getSpawn().setLoc(142624, -82285, -6491, Rnd.get(65536));
            raid.teleportTo(142624, -82285, -6491, 100);
            this._status = 2;
            return true;
        }
        return false;
    }

    private void despawnRaid(Npc raid) {
        raid.getSpawn().setLoc(-105300, -252700, -15542, 0);
        if (!raid.isDead())
            raid.teleportTo(-105300, -252700, -15542, 0);
        this._status = -1;
    }
}
