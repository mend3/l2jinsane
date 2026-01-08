/**/
package net.sf.l2j.gameserver.events.tournament.arenas;

import enginemods.main.data.ConfigData;
import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

import java.util.*;
import java.util.logging.Logger;

public class Arena2x2 implements Runnable {
    protected static final Logger _log = Logger.getLogger(Arena2x2.class.getName());
    public static List<Arena2x2.Pair> registered;
    int free;
    Arena2x2.Arena[] arenas;
    Map<Integer, String> fights;

    public Arena2x2() {
        this.free = Config.ARENA_EVENT_COUNT;
        this.arenas = new Arena2x2.Arena[Config.ARENA_EVENT_COUNT];
        this.fights = new HashMap<>(Config.ARENA_EVENT_COUNT);
        registered = new ArrayList<>();

        for (int i = 0; i < Config.ARENA_EVENT_COUNT; ++i) {
            int[] coord = Config.ARENA_EVENT_LOCS[i];
            this.arenas[i] = new Arena(this, i, coord[0], coord[1], coord[2]);
        }

    }

    public static Arena2x2 getInstance() {
        return Arena2x2.SingletonHolder.INSTANCE;
    }

    public boolean register(Player player, Player assist) {

        for (Pair p : registered) {
            if (p.getLeader() != player && p.getAssist() != player) {
                if (p.getLeader() != assist && p.getAssist() != assist) {
                    continue;
                }

                player.sendMessage("Tournament: Your partner already registered!");
                return false;
            }

            player.sendMessage("Tournament: You already registered!");
            return false;
        }

        return registered.add(new Arena2x2.Pair(player, assist));
    }

    public boolean isRegistered(Player player) {
        Iterator<Pair> var2 = registered.iterator();

        Arena2x2.Pair p;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            p = var2.next();
        } while (p.getLeader() != player && p.getAssist() != player);

        return true;
    }

    public Map<Integer, String> getFights() {
        return this.fights;
    }

    public boolean remove(Player player) {
        Iterator<Pair> var2 = registered.iterator();

        Arena2x2.Pair p;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            p = var2.next();
        } while (p.getLeader() != player && p.getAssist() != player);

        p.removeMessage();
        registered.remove(p);
        return true;
    }

    public synchronized void run() {
        while (true) {
            if (registered.size() >= 2 && this.free != 0) {
                List<Arena2x2.Pair> opponents = this.selectOpponents();
                if (opponents != null && opponents.size() == 2) {
                    Thread T = new Thread(new Arena2x2.EvtArenaTask(opponents));
                    T.setDaemon(true);
                    T.start();
                }

                try {
                    Thread.sleep((long) (Config.ARENA_CALL_INTERVAL * 1000L));
                } catch (InterruptedException ignored) {
                }
            } else {
                try {
                    Thread.sleep((long) (Config.ARENA_CALL_INTERVAL * 1000L));
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private List<Arena2x2.Pair> selectOpponents() {
        List<Arena2x2.Pair> opponents = new ArrayList<>();
        Arena2x2.Pair pairOne = null;
        Arena2x2.Pair pairTwo = null;
        int tries = 3;

        do {
            int first = 0;
            int second = 0;
            if (this.getRegisteredCount() < 2) {
                return opponents;
            }

            if (pairOne == null) {
                first = Rnd.get(this.getRegisteredCount());
                pairOne = registered.get(first);
                if (!pairOne.check()) {
                    pairOne = null;
                    registered.remove(first);
                    return null;
                }

                opponents.add(0, pairOne);
                registered.remove(first);
            }

            if (pairTwo == null) {
                second = Rnd.get(this.getRegisteredCount());
                pairTwo = registered.get(second);
                if (!pairTwo.check()) {
                    pairTwo = null;
                    registered.remove(second);
                    return null;
                }

                opponents.add(1, pairTwo);
                registered.remove(second);
            }

            if (pairOne != null && pairTwo != null) {
                break;
            }

            --tries;
        } while (tries > 0);

        return opponents;
    }

    public int getRegisteredCount() {
        return registered.size();
    }

    private static class SingletonHolder {
        protected static final Arena2x2 INSTANCE = new Arena2x2();
    }

    private static class Arena {
        protected int x;
        protected int y;
        protected int z;
        protected boolean isFree = true;
        int id;

        public Arena(final Arena2x2 param1, int param2, int param3, int param4, int param5) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void setFree(boolean val) {
            this.isFree = val;
        }
    }

    private class Pair {
        Player leader;
        Player assist;

        public Pair(Player param2, Player param3) {
            this.leader = leader;
            this.assist = assist;
        }

        public Player getAssist() {
            return this.assist;
        }

        public Player getLeader() {
            return this.leader;
        }

        public boolean check() {
            if (this.leader != null && this.leader.isOnline() || this.assist == null && !this.assist.isOnline()) {
                if (this.assist != null && this.assist.isOnline() || this.leader == null && !this.leader.isOnline()) {
                    return true;
                } else {
                    this.leader.sendMessage("Tournament: You participation in Event was Canceled.");
                    return false;
                }
            } else {
                this.assist.sendMessage("Tournament: You participation in Event was Canceled.");
                return false;
            }
        }

        public boolean isDead() {
            if ((this.leader == null || this.leader.isDead() || !this.leader.isOnline() || !this.leader.isInsideZone(ZoneId.ARENA_EVENT) || !this.leader.isArenaAttack()) && (this.assist == null || this.assist.isDead() || !this.assist.isOnline() || !this.assist.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist.isArenaAttack())) {
                return false;
            } else {
                return !this.leader.isDead() || !this.assist.isDead();
            }
        }

        public boolean isAlive() {
            if ((this.leader == null || this.leader.isDead() || !this.leader.isOnline() || !this.leader.isInsideZone(ZoneId.ARENA_EVENT) || !this.leader.isArenaAttack()) && (this.assist == null || this.assist.isDead() || !this.assist.isOnline() || !this.assist.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist.isArenaAttack())) {
                return false;
            } else {
                return !this.leader.isDead() || !this.assist.isDead();
            }
        }

        public void teleportTo(int x, int y, int z) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.setCurrentCp(this.leader.getMaxCp());
                this.leader.setCurrentHp(this.leader.getMaxHp());
                this.leader.setCurrentMp(this.leader.getMaxMp());
                if (this.leader.isInObserverMode()) {
                    this.leader.setLastCords(x, y, z);
                    this.leader.leaveOlympiadObserverMode();
                } else {
                    this.leader.teleportTo(x, y, z, 0);
                }

                this.leader.broadcastUserInfo();
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.setCurrentCp(this.assist.getMaxCp());
                this.assist.setCurrentHp(this.assist.getMaxHp());
                this.assist.setCurrentMp(this.assist.getMaxMp());
                if (this.assist.isInObserverMode()) {
                    this.assist.setLastCords(x, y + 50, z);
                    this.assist.leaveOlympiadObserverMode();
                } else {
                    this.assist.teleportTo(x, y + 50, z, 0);
                }

                this.assist.broadcastUserInfo();
            }

        }

        public void EventTitle(String title, String color) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.setTitle(title);
                this.leader.getAppearance().setTitleColor(Integer.decode("0x" + color));
                this.leader.broadcastUserInfo();
                this.leader.broadcastTitleInfo();
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.setTitle(title);
                this.assist.getAppearance().setTitleColor(Integer.decode("0x" + color));
                this.assist.broadcastUserInfo();
                this.assist.broadcastTitleInfo();
            }

        }

        public void saveTitle() {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader._originalTitleColorTournament = this.leader.getAppearance().getTitleColor();
                this.leader._originalTitleTournament = this.leader.getTitle();
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist._originalTitleColorTournament = this.assist.getAppearance().getTitleColor();
                this.assist._originalTitleTournament = this.assist.getTitle();
            }

        }

        public void backTitle() {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.setTitle(this.leader._originalTitleTournament);
                this.leader.getAppearance().setTitleColor(this.leader._originalTitleColorTournament);
                this.leader.broadcastUserInfo();
                this.leader.broadcastTitleInfo();
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.setTitle(this.assist._originalTitleTournament);
                this.assist.getAppearance().setTitleColor(this.assist._originalTitleColorTournament);
                this.assist.broadcastUserInfo();
                this.assist.broadcastTitleInfo();
            }

        }

        public void rewards() {
            if (this.leader != null && this.leader.isOnline()) {
                if (this.leader.isVip()) {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.leader, true);
                } else {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT, this.leader, true);
                }
            }

            if (this.assist != null && this.assist.isOnline()) {
                if (this.leader.isVip()) {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist, true);
                } else {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT, this.assist, true);
                }
            }

            this.sendPacket("Congratulations, your team won the event!", 5);
        }

        public void rewardsLost() {
            if (this.leader != null && this.leader.isOnline()) {
                if (this.leader.isVip()) {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.leader, true);
                } else {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT, this.leader, true);
                }
            }

            if (this.assist != null && this.assist.isOnline()) {
                if (this.leader.isVip()) {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist, true);
                } else {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT, this.assist, true);
                }
            }

            this.sendPacket("your team lost the event! =(", 5);
        }

        public void setInTournamentEvent(boolean val) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.setInArenaEvent(val);
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.setInArenaEvent(val);
            }

        }

        public void removeMessage() {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.sendMessage("Tournament: Your participation has been removed.");
                this.leader.setArenaProtection(false);
                this.leader.setArena2x2(false);
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.sendMessage("Tournament: Your participation has been removed.");
                this.assist.setArenaProtection(false);
                this.leader.setArena2x2(false);
            }

        }

        public void setArenaProtection(boolean val) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.setArenaProtection(val);
                this.leader.setArena2x2(val);
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.setArenaProtection(val);
                this.leader.setArena2x2(val);
            }

        }

        public void revive() {
            if (this.leader != null && this.leader.isOnline() && this.leader.isDead()) {
                this.leader.doRevive();
            }

            if (this.assist != null && this.assist.isOnline() && this.assist.isDead()) {
                this.assist.doRevive();
            }

        }

        public void setImobilised(boolean val) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.setIsInvul(val);
                this.leader.setStopArena(val);
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.setIsInvul(val);
                this.assist.setStopArena(val);
            }

        }

        public void setArenaAttack(boolean val) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.setArenaAttack(val);
                this.leader.broadcastUserInfo();
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.setArenaAttack(val);
                this.assist.broadcastUserInfo();
            }

        }

        public void removePet() {
            Summon summon;
            if (this.leader != null && this.leader.isOnline()) {
                if (this.leader.getSummon() != null) {
                    summon = this.leader.getSummon();
                    if (summon != null) {
                        summon.unSummon(summon.getOwner());
                    }

                    if (summon instanceof Pet) {
                        summon.unSummon(this.leader);
                    }
                }

                if (this.leader.getMountType() == 1 || this.leader.getMountType() == 2) {
                    this.leader.dismount();
                }
            }

            if (this.assist != null && this.assist.isOnline()) {
                if (this.assist.getSummon() != null) {
                    summon = this.assist.getSummon();
                    if (summon != null) {
                        summon.unSummon(summon.getOwner());
                    }

                    if (summon instanceof Pet) {
                        summon.unSummon(this.assist);
                    }
                }

                if (this.assist.getMountType() == 1 || this.assist.getMountType() == 2) {
                    this.assist.dismount();
                }
            }

        }

        public void removeSkills() {
            L2Effect[] var1 = this.leader.getAllEffects();
            int var2 = var1.length;

            int var3;
            L2Effect effect;
            for (var3 = 0; var3 < var2; ++var3) {
                effect = var1[var3];
                if (effect.getSkill().getId() == 406 || effect.getSkill().getId() == 139 || effect.getSkill().getId() == 176 || effect.getSkill().getId() == 420) {
                    this.leader.stopSkillEffects(effect.getSkill().getId());
                    this.leader.enableSkill(effect.getSkill());
                }
            }

            var1 = this.assist.getAllEffects();
            var2 = var1.length;

            for (var3 = 0; var3 < var2; ++var3) {
                effect = var1[var3];
                if (effect.getSkill().getId() == 406 || effect.getSkill().getId() == 139 || effect.getSkill().getId() == 176 || effect.getSkill().getId() == 420) {
                    this.assist.stopSkillEffects(effect.getSkill().getId());
                    this.assist.enableSkill(effect.getSkill());
                }
            }

            if (Config.ARENA_SKILL_PROTECT && this.leader != null && this.leader.isOnline()) {
                var1 = this.leader.getAllEffects();
                var2 = var1.length;

                for (var3 = 0; var3 < var2; ++var3) {
                    effect = var1[var3];
                    if (Config.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId())) {
                        this.leader.stopSkillEffects(effect.getSkill().getId());
                    }
                }

                if (this.leader.getMountType() == 2) {
                    this.leader.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
                    this.leader.enteredNoLanding(5);
                }
            }

            if (Config.ARENA_SKILL_PROTECT && this.assist != null && this.assist.isOnline()) {
                var1 = this.assist.getAllEffects();
                var2 = var1.length;

                for (var3 = 0; var3 < var2; ++var3) {
                    effect = var1[var3];
                    if (Config.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId())) {
                        this.assist.stopSkillEffects(effect.getSkill().getId());
                    }
                }

                if (this.assist.getMountType() == 2) {
                    this.assist.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
                    this.assist.enteredNoLanding(5);
                }
            }

        }

        public void sendPacket(String message, int duration) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.sendPacket(new ExShowScreenMessage(message, duration * 1000));
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.sendPacket(new ExShowScreenMessage(message, duration * 1000));
            }

        }

        public void inicarContagem(int duration) {
            if (this.leader != null && this.leader.isOnline()) {
                ThreadPool.schedule(Arena2x2.this.new countdown(this.leader, duration), 0L);
            }

            if (this.assist != null && this.assist.isOnline()) {
                ThreadPool.schedule(Arena2x2.this.new countdown(this.assist, duration), 0L);
            }

        }

        public void sendPacketinit(String message, int duration) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
            }

            if (this.leader.getClassId() == ClassId.SHILLIEN_ELDER || this.leader.getClassId() == ClassId.SHILLIEN_SAINT || this.leader.getClassId() == ClassId.BISHOP || this.leader.getClassId() == ClassId.CARDINAL || this.leader.getClassId() == ClassId.ELVEN_ELDER || this.leader.getClassId() == ClassId.EVAS_SAINT) {
                ThreadPool.schedule(() -> Pair.this.leader.getClient().closeNow(), 100L);
            }

            if (this.assist.getClassId() == ClassId.SHILLIEN_ELDER || this.assist.getClassId() == ClassId.SHILLIEN_SAINT || this.assist.getClassId() == ClassId.BISHOP || this.assist.getClassId() == ClassId.CARDINAL || this.assist.getClassId() == ClassId.ELVEN_ELDER || this.assist.getClassId() == ClassId.EVAS_SAINT) {
                ThreadPool.schedule(() -> Pair.this.assist.getClient().closeNow(), 100L);
            }

        }
    }

    private class EvtArenaTask implements Runnable {
        private final Arena2x2.Pair pairOne;
        private final Arena2x2.Pair pairTwo;
        private final int pOneX;
        private final int pOneY;
        private final int pOneZ;
        private final int pTwoX;
        private final int pTwoY;
        private final int pTwoZ;
        private Arena2x2.Arena arena;

        public EvtArenaTask(List<Arena2x2.Pair> opponents) {
            this.pairOne = opponents.get(0);
            this.pairTwo = opponents.get(1);
            Player leader = this.pairOne.getLeader();
            this.pOneX = leader.getX();
            this.pOneY = leader.getY();
            this.pOneZ = leader.getZ();
            leader = this.pairTwo.getLeader();
            this.pTwoX = leader.getX();
            this.pTwoY = leader.getY();
            this.pTwoZ = leader.getZ();
        }

        public void run() {
            --Arena2x2.this.free;
            this.pairOne.saveTitle();
            this.pairTwo.saveTitle();
            this.portPairsToArena();
            this.pairOne.inicarContagem(Config.ARENA_WAIT_INTERVAL);
            this.pairTwo.inicarContagem(Config.ARENA_WAIT_INTERVAL);

            try {
                Thread.sleep((long) (Config.ARENA_WAIT_INTERVAL * 1000L));
            } catch (InterruptedException ignored) {
            }

            this.pairOne.sendPacketinit("Started. Good Fight!", 3);
            this.pairTwo.sendPacketinit("Started. Good Fight!", 3);
            this.pairOne.EventTitle(Config.MSG_TEAM1, Config.TITLE_COLOR_TEAM1);
            this.pairTwo.EventTitle(Config.MSG_TEAM2, Config.TITLE_COLOR_TEAM2);
            this.pairOne.setImobilised(false);
            this.pairTwo.setImobilised(false);
            this.pairOne.setArenaAttack(true);
            this.pairTwo.setArenaAttack(true);

            while (this.check()) {
                try {
                    Thread.sleep((long) Config.ARENA_CHECK_INTERVAL);
                } catch (InterruptedException var3) {
                    break;
                }
            }

            this.finishDuel();
            Arena2x2 this$2 = Arena2x2.this;
            ++this$2.free;
        }

        private void finishDuel() {
            Arena2x2.this.fights.remove(this.arena.id);
            this.rewardWinner();
            this.pairOne.revive();
            this.pairTwo.revive();
            this.pairOne.teleportTo(this.pOneX, this.pOneY, this.pOneZ);
            this.pairTwo.teleportTo(this.pTwoX, this.pTwoY, this.pTwoZ);
            this.pairOne.backTitle();
            this.pairTwo.backTitle();
            this.pairOne.setInTournamentEvent(false);
            this.pairTwo.setInTournamentEvent(false);
            this.pairOne.setArenaProtection(false);
            this.pairTwo.setArenaProtection(false);
            this.pairOne.setArenaAttack(false);
            this.pairTwo.setArenaAttack(false);
            this.arena.setFree(true);
        }

        private void rewardWinner() {
            if (this.pairOne.isAlive() && !this.pairTwo.isAlive()) {
                this.pairOne.rewards();
                this.pairTwo.rewardsLost();
            } else if (this.pairTwo.isAlive() && !this.pairOne.isAlive()) {
                this.pairTwo.rewards();
                this.pairOne.rewardsLost();
            }

        }

        private boolean check() {
            return this.pairOne.isDead() && this.pairTwo.isDead();
        }

        private void portPairsToArena() {
            Arena2x2.Arena[] var1 = Arena2x2.this.arenas;
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                Arena2x2.Arena arena = var1[var3];
                if (arena.isFree) {
                    this.arena = arena;
                    arena.setFree(false);
                    this.pairOne.removePet();
                    this.pairTwo.removePet();
                    this.pairOne.teleportTo(arena.x - 850, arena.y, arena.z);
                    this.pairTwo.teleportTo(arena.x + 850, arena.y, arena.z);
                    this.pairOne.setImobilised(true);
                    this.pairTwo.setImobilised(true);
                    this.pairOne.setInTournamentEvent(true);
                    this.pairTwo.setInTournamentEvent(true);
                    this.pairOne.removeSkills();
                    this.pairTwo.removeSkills();
                    Map<Integer, String> var10000 = Arena2x2.this.fights;
                    Integer var10001 = this.arena.id;
                    String var10002 = this.pairOne.getLeader().getName();
                    var10000.put(var10001, var10002 + " vs " + this.pairTwo.getLeader().getName());
                    var10000 = Arena2x2.this.fights;
                    var10001 = this.arena.id;
                    var10002 = this.pairOne.getLeader().getName();
                    var10000.put(var10001, var10002 + " vs " + this.pairTwo.getLeader().getName());
                    break;
                }
            }

        }
    }

    protected class countdown implements Runnable {
        private final Player _player;
        private final int _time;

        public countdown(Player player, int time) {
            this._time = time;
            this._player = player;
        }

        public void run() {
            if (this._player.isOnline()) {
                switch (this._time) {
                    case 1:
                        if (this._player.isOnline()) {
                            this._player.sendMessage(this._time + " second(s) to start the battle!");
                        }

                        this._player.setIsParalyzed(false);
                        break;
                    case 2:
                        if (this._player.isOnline()) {
                            this._player.sendMessage(this._time + " second(s) to start the battle!");
                        }

                        this._player.setIsParalyzed(true);
                        break;
                    case 3:
                        if (this._player.isOnline()) {
                            this._player.sendMessage(this._time + " second(s) to start the battle!");
                        }

                        this._player.setIsParalyzed(true);
                        break;
                    case 4:
                        if (this._player.isOnline()) {
                            this._player.sendMessage(this._time + " second(s) to start the battle!");
                        }

                        this._player.setIsParalyzed(true);
                        break;
                    case 5:
                        if (this._player.isOnline()) {
                            this._player.sendMessage(this._time + " second(s) to start the battle!");
                        }

                        this._player.setIsParalyzed(true);
                        break;
                    case 10:
                        if (this._player.isOnline()) {
                            this._player.sendMessage(this._time + " second(s) to start the battle!");
                        }

                        this._player.setIsParalyzed(true);
                        this._player.broadcastPacket(new SocialAction(this._player, 5));
                        break;
                    case 15:
                        if (this._player.isOnline()) {
                            this._player.sendPacket(new ExShowScreenMessage(this._time + " ..", 3000));
                            this._player.sendMessage(this._time + " second(s) to start the battle!");
                            this._player.setIsParalyzed(true);
                            this._player.broadcastPacket(new SocialAction(this._player, 9));
                        }
                        break;
                    case 20:
                        if (this._player.isOnline()) {
                            this._player.sendPacket(new ExShowScreenMessage(this._time + " ..", 3000));
                            this._player.sendMessage(this._time + " second(s) to start the battle!");
                            this._player.setIsParalyzed(true);
                        }
                        break;
                    case 27:
                        if (this._player.isOnline()) {
                            this._player.sendPacket(new ExShowScreenMessage("The battle starts in 30 second(s)..", 4000));
                            this._player.sendMessage("30 second(s) to start the battle!");
                            this._player.setIsParalyzed(true);
                            this._player.broadcastPacket(new SocialAction(this._player, 2));
                        }
                        break;
                    case 45:
                        if (this._player.isOnline()) {
                            this._player.sendPacket(new ExShowScreenMessage(this._time + " ..", 3000));
                            this._player.sendMessage(this._time + " second(s) to start the battle!");
                            this._player.setIsParalyzed(true);
                            this._player.broadcastPacket(new SocialAction(this._player, 1));
                        }
                        break;
                    case 60:
                    case 120:
                    case 180:
                    case 240:
                    case 300:
                        if (this._player.isOnline()) {
                            this._player.sendPacket(new ExShowScreenMessage("The battle starts in " + this._time + " second(s)..", 4000));
                            this._player.sendMessage(this._time + " second(s) to start the battle.");
                            this._player.setIsParalyzed(true);
                        }
                }

                if (this._time > 1) {
                    ThreadPool.schedule(Arena2x2.this.new countdown(this._player, this._time - 1), 1000L);
                }
            }

        }
    }
}