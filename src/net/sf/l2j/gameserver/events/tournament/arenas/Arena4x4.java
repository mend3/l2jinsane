/**/
package net.sf.l2j.gameserver.events.tournament.arenas;

import enginemods.main.data.ConfigData;
import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

import java.util.*;

public class Arena4x4 implements Runnable {
    public static List<Arena4x4.Pair> registered;
    int free;
    Arena4x4.Arena[] arenas;
    Map<Integer, String> fights;

    public Arena4x4() {
        this.free = Config.ARENA_EVENT_COUNT_4X4;
        this.arenas = new Arena4x4.Arena[Config.ARENA_EVENT_COUNT_4X4];
        this.fights = new HashMap(Config.ARENA_EVENT_COUNT_4X4);
        registered = new ArrayList();

        for (int i = 0; i < Config.ARENA_EVENT_COUNT_4X4; ++i) {
            int[] coord = Config.ARENA_EVENT_LOCS_4X4[i];
            this.arenas[i] = new Arena(this, i, coord[0], coord[1], coord[2]);
        }

    }

    public static Arena4x4 getInstance() {
        return Arena4x4.SingletonHolder.INSTANCE;
    }

    public boolean register(Player player, Player assist, Player assist2, Player assist3) {
        Iterator var5 = registered.iterator();

        while (var5.hasNext()) {
            Arena4x4.Pair p = (Arena4x4.Pair) var5.next();
            if (p.getLeader() != player && p.getAssist() != player) {
                if (p.getLeader() != assist && p.getAssist() != assist) {
                    if (p.getLeader() != assist2 && p.getAssist2() != assist2) {
                        if (p.getLeader() != assist3 && p.getAssist3() != assist3) {
                            continue;
                        }

                        player.sendMessage("Tournament: " + assist3.getName() + " already registered!");
                        return false;
                    }

                    player.sendMessage("Tournament: " + assist2.getName() + " already registered!");
                    return false;
                }

                player.sendMessage("Tournament: " + assist.getName() + " already registered!");
                return false;
            }

            player.sendMessage("Tournament: You already registered!");
            return false;
        }

        return registered.add(new Arena4x4.Pair(player, assist, assist2, assist3));
    }

    public boolean isRegistered(Player player) {
        Iterator var2 = registered.iterator();

        Arena4x4.Pair p;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            p = (Arena4x4.Pair) var2.next();
        } while (p.getLeader() != player && p.getAssist() != player && p.getAssist2() != player && p.getAssist3() != player);

        return true;
    }

    public Map<Integer, String> getFights() {
        return this.fights;
    }

    public boolean remove(Player player) {
        Iterator var2 = registered.iterator();

        Arena4x4.Pair p;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            p = (Arena4x4.Pair) var2.next();
        } while (p.getLeader() != player && p.getAssist() != player && p.getAssist2() != player && p.getAssist3() != player);

        p.removeMessage();
        registered.remove(p);
        return true;
    }

    public synchronized void run() {
        while (true) {
            if (registered.size() >= 2 && this.free != 0) {
                List<Arena4x4.Pair> opponents = this.selectOpponents();
                if (opponents != null && opponents.size() == 2) {
                    Thread T = new Thread(new Arena4x4.EvtArenaTask(opponents));
                    T.setDaemon(true);
                    T.start();
                }

                try {
                    Thread.sleep((long) (Config.ARENA_CALL_INTERVAL * 1000L));
                } catch (InterruptedException var3) {
                }
            } else {
                try {
                    Thread.sleep((long) (Config.ARENA_CALL_INTERVAL * 1000L));
                } catch (InterruptedException var4) {
                }
            }
        }
    }

    private List<Arena4x4.Pair> selectOpponents() {
        List<Arena4x4.Pair> opponents = new ArrayList();
        Arena4x4.Pair pairOne = null;
        Arena4x4.Pair pairTwo = null;
        int tries = 3;

        do {
            int first = 0;
            int second = 0;
            if (this.getRegisteredCount() < 2) {
                return opponents;
            }

            first = Rnd.get(this.getRegisteredCount());
            pairOne = registered.get(first);
            if (!pairOne.check()) {
                pairOne = null;
                registered.remove(first);
                return null;
            }

            opponents.add(0, pairOne);
            registered.remove(first);

            second = Rnd.get(this.getRegisteredCount());
            pairTwo = registered.get(second);
            if (!pairTwo.check()) {
                pairTwo = null;
                registered.remove(second);
                return null;
            }

            opponents.add(1, pairTwo);
            registered.remove(second);

            break;

        } while (tries > 0);

        return opponents;
    }

    public int getRegisteredCount() {
        return registered.size();
    }

    private static class SingletonHolder {
        protected static final Arena4x4 INSTANCE = new Arena4x4();
    }

    private static class Arena {
        protected int x;
        protected int y;
        protected int z;
        protected boolean isFree = true;
        int id;

        public Arena(final Arena4x4 param1, int id, int x, int y, int z) {
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
        Player assist2;
        Player assist3;

        public Pair(Player leader, Player assist, Player assist2, Player assist3) {
            this.leader = leader;
            this.assist = assist;
            this.assist2 = assist2;
            this.assist3 = assist3;
        }

        public Player getAssist() {
            return this.assist;
        }

        public Player getAssist2() {
            return this.assist2;
        }

        public Player getAssist3() {
            return this.assist3;
        }

        public Player getLeader() {
            return this.leader;
        }

        public boolean check() {
            if (this.leader != null && this.leader.isOnline()) {
                if (this.assist == null || !this.assist.isOnline() || this.assist2 == null || !this.assist2.isOnline() || this.assist3 == null || !this.assist3.isOnline()) {
                    this.leader.sendMessage("Tournament: You participation in Event was Canceled.");
                    if (this.assist != null || this.assist.isOnline()) {
                        this.assist.sendMessage("Tournament: You participation in Event was Canceled.");
                    }

                    if (this.assist2 != null || this.assist2.isOnline()) {
                        this.assist2.sendMessage("Tournament: You participation in Event was Canceled.");
                    }

                    if (this.assist3 != null || this.assist3.isOnline()) {
                        this.assist3.sendMessage("Tournament: You participation in Event was Canceled.");
                    }

                    return false;
                } else {
                    return true;
                }
            } else {
                if (this.assist != null || this.assist.isOnline()) {
                    this.assist.sendMessage("Tournament: You participation in Event was Canceled.");
                }

                if (this.assist2 != null || this.assist2.isOnline()) {
                    this.assist2.sendMessage("Tournament: You participation in Event was Canceled.");
                }

                if (this.assist3 != null || this.assist3.isOnline()) {
                    this.assist3.sendMessage("Tournament: You participation in Event was Canceled.");
                }

                return false;
            }
        }

        public boolean isDead() {
            if (this.leader != null && !this.leader.isDead() && this.leader.isOnline() && this.leader.isInsideZone(ZoneId.ARENA_EVENT) && this.leader.isArenaAttack() || this.assist != null && !this.assist.isDead() && this.assist.isOnline() && this.assist.isInsideZone(ZoneId.ARENA_EVENT) && this.assist.isArenaAttack() || this.assist2 != null && !this.assist2.isDead() && this.assist2.isOnline() && this.assist2.isInsideZone(ZoneId.ARENA_EVENT) && this.assist2.isArenaAttack() || this.assist3 != null && !this.assist3.isDead() && this.assist3.isOnline() && this.assist3.isInsideZone(ZoneId.ARENA_EVENT) && this.assist3.isArenaAttack()) {
                return !this.leader.isDead() || !this.assist.isDead() || !this.assist2.isDead() || !this.assist3.isDead();
            } else {
                return false;
            }
        }

        public boolean isAlive() {
            if (this.leader != null && !this.leader.isDead() && this.leader.isOnline() && this.leader.isInsideZone(ZoneId.ARENA_EVENT) && this.leader.isArenaAttack() || this.assist != null && !this.assist.isDead() && this.assist.isOnline() && this.assist.isInsideZone(ZoneId.ARENA_EVENT) && this.assist.isArenaAttack() || this.assist2 != null && !this.assist2.isDead() && this.assist2.isOnline() && this.assist2.isInsideZone(ZoneId.ARENA_EVENT) && this.assist2.isArenaAttack() || this.assist3 != null && !this.assist3.isDead() && this.assist3.isOnline() && this.assist3.isInsideZone(ZoneId.ARENA_EVENT) && this.assist3.isArenaAttack()) {
                return !this.leader.isDead() || !this.assist.isDead() || !this.assist2.isDead() || !this.assist3.isDead();
            } else {
                return false;
            }
        }

        public void teleportTo(int x, int y, int z) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.getAppearance().getInvisible();
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
                this.assist.getAppearance().getInvisible();
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

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.getAppearance().getInvisible();
                this.assist2.setCurrentCp(this.assist2.getMaxCp());
                this.assist2.setCurrentHp(this.assist2.getMaxHp());
                this.assist2.setCurrentMp(this.assist2.getMaxMp());
                if (this.assist2.isInObserverMode()) {
                    this.assist2.setLastCords(x, y - 100, z);
                    this.assist2.leaveOlympiadObserverMode();
                } else {
                    this.assist2.teleportTo(x, y - 100, z, 0);
                }

                this.assist2.broadcastUserInfo();
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.getAppearance().getInvisible();
                this.assist3.setCurrentCp(this.assist3.getMaxCp());
                this.assist3.setCurrentHp(this.assist3.getMaxHp());
                this.assist3.setCurrentMp(this.assist3.getMaxMp());
                if (this.assist3.isInObserverMode()) {
                    this.assist3.setLastCords(x, y - 50, z);
                    this.assist3.leaveOlympiadObserverMode();
                } else {
                    this.assist3.teleportTo(x, y - 50, z, 0);
                }

                this.assist3.broadcastUserInfo();
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

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.setTitle(title);
                this.assist2.getAppearance().setTitleColor(Integer.decode("0x" + color));
                this.assist2.broadcastUserInfo();
                this.assist2.broadcastTitleInfo();
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.setTitle(title);
                this.assist3.getAppearance().setTitleColor(Integer.decode("0x" + color));
                this.assist3.broadcastUserInfo();
                this.assist3.broadcastTitleInfo();
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

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2._originalTitleColorTournament = this.assist2.getAppearance().getTitleColor();
                this.assist2._originalTitleTournament = this.assist2.getTitle();
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3._originalTitleColorTournament = this.assist3.getAppearance().getTitleColor();
                this.assist3._originalTitleTournament = this.assist3.getTitle();
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

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.setTitle(this.assist2._originalTitleTournament);
                this.assist2.getAppearance().setTitleColor(this.assist2._originalTitleColorTournament);
                this.assist2.broadcastUserInfo();
                this.assist2.broadcastTitleInfo();
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.setTitle(this.assist3._originalTitleTournament);
                this.assist3.getAppearance().setTitleColor(this.assist3._originalTitleColorTournament);
                this.assist3.broadcastUserInfo();
                this.assist3.broadcastTitleInfo();
            }

        }

        public void rewards() {
            if (this.leader != null && this.leader.isOnline()) {
                if (this.leader.isVip()) {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_4X4 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.leader, true);
                } else {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_4X4, this.leader, true);
                }
            }

            if (this.assist != null && this.assist.isOnline()) {
                if (this.assist.isVip()) {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_4X4 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist, true);
                } else {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_4X4, this.assist, true);
                }
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                if (this.assist2.isVip()) {
                    this.assist2.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_4X4 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist2, true);
                } else {
                    this.assist2.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_4X4, this.assist2, true);
                }
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                if (this.assist3.isVip()) {
                    this.assist3.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_4X4 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist3, true);
                } else {
                    this.assist3.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_4X4, this.assist3, true);
                }
            }

            this.sendPacket("Congratulations, your team won the event!", 5);
        }

        public void rewardsLost() {
            if (this.leader != null && this.leader.isOnline()) {
                if (this.leader.isVip()) {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_4X4 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.leader, true);
                } else {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_4X4, this.leader, true);
                }
            }

            if (this.assist != null && this.assist.isOnline()) {
                if (this.assist.isVip()) {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_4X4 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist, true);
                } else {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_4X4, this.assist, true);
                }
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                if (this.assist2.isVip()) {
                    this.assist2.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_4X4 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist2, true);
                } else {
                    this.assist2.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_4X4, this.assist2, true);
                }
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                if (this.assist3.isVip()) {
                    this.assist3.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_4X4 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist3, true);
                } else {
                    this.assist3.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_4X4, this.assist3, true);
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

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.setInArenaEvent(val);
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.setInArenaEvent(val);
            }

        }

        public void removeMessage() {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.sendMessage("Tournament: Your participation has been removed.");
                this.leader.setArenaProtection(false);
                this.leader.setArena4x4(false);
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.sendMessage("Tournament: Your participation has been removed.");
                this.assist.setArenaProtection(false);
                this.assist.setArena4x4(false);
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.sendMessage("Tournament: Your participation has been removed.");
                this.assist2.setArenaProtection(false);
                this.assist2.setArena4x4(false);
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.sendMessage("Tournament: Your participation has been removed.");
                this.assist3.setArenaProtection(false);
                this.assist3.setArena4x4(false);
            }

        }

        public void setArenaProtection(boolean val) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.setArenaProtection(val);
                this.leader.setArena4x4(val);
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.setArenaProtection(val);
                this.assist.setArena4x4(val);
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.setArenaProtection(val);
                this.assist2.setArena4x4(val);
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.setArenaProtection(val);
                this.assist3.setArena4x4(val);
            }

        }

        public void revive() {
            if (this.leader != null && this.leader.isOnline() && this.leader.isDead()) {
                this.leader.doRevive();
            }

            if (this.assist != null && this.assist.isOnline() && this.assist.isDead()) {
                this.assist.doRevive();
            }

            if (this.assist2 != null && this.assist2.isOnline() && this.assist2.isDead()) {
                this.assist2.doRevive();
            }

            if (this.assist3 != null && this.assist3.isOnline() && this.assist3.isDead()) {
                this.assist3.doRevive();
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

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.setIsInvul(val);
                this.assist2.setStopArena(val);
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.setIsInvul(val);
                this.assist3.setStopArena(val);
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

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.setArenaAttack(val);
                this.assist2.broadcastUserInfo();
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.setArenaAttack(val);
                this.assist3.broadcastUserInfo();
            }

        }

        public void removePet() {
            Summon summon;
            if (this.leader != null && this.leader.isOnline()) {
                if (this.leader.getSummon() != null) {
                    summon = this.leader.getSummon();
                    summon.unSummon(summon.getOwner());

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
                    summon.unSummon(summon.getOwner());

                    if (summon instanceof Pet) {
                        summon.unSummon(this.assist);
                    }
                }

                if (this.assist.getMountType() == 1 || this.assist.getMountType() == 2) {
                    this.assist.dismount();
                }
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                if (this.assist2.getSummon() != null) {
                    summon = this.assist2.getSummon();
                    summon.unSummon(summon.getOwner());

                    if (summon instanceof Pet) {
                        summon.unSummon(this.assist2);
                    }
                }

                if (this.assist2.getMountType() == 1 || this.assist2.getMountType() == 2) {
                    this.assist2.dismount();
                }
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                if (this.assist3.getSummon() != null) {
                    summon = this.assist3.getSummon();
                    summon.unSummon(summon.getOwner());

                    if (summon instanceof Pet) {
                        summon.unSummon(this.assist3);
                    }
                }

                if (this.assist3.getMountType() == 1 || this.assist3.getMountType() == 2) {
                    this.assist3.dismount();
                }
            }

        }

        public void removeSkills() {
            L2Effect[] var1;
            int var2;
            int var3;
            L2Effect effect;
            if (this.leader.getClassId() != ClassId.SHILLIEN_ELDER && this.leader.getClassId() != ClassId.SHILLIEN_SAINT && this.leader.getClassId() != ClassId.BISHOP && this.leader.getClassId() != ClassId.CARDINAL && this.leader.getClassId() != ClassId.ELVEN_ELDER && this.leader.getClassId() != ClassId.EVAS_SAINT) {
                var1 = this.leader.getAllEffects();
                var2 = var1.length;

                for (var3 = 0; var3 < var2; ++var3) {
                    effect = var1[var3];
                    if (Config.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId())) {
                        this.leader.stopSkillEffects(effect.getSkill().getId());
                    }
                }
            }

            if (this.assist.getClassId() != ClassId.SHILLIEN_ELDER && this.assist.getClassId() != ClassId.SHILLIEN_SAINT && this.assist.getClassId() != ClassId.BISHOP && this.assist.getClassId() != ClassId.CARDINAL && this.assist.getClassId() != ClassId.ELVEN_ELDER && this.assist.getClassId() != ClassId.EVAS_SAINT) {
                var1 = this.assist.getAllEffects();
                var2 = var1.length;

                for (var3 = 0; var3 < var2; ++var3) {
                    effect = var1[var3];
                    if (Config.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId())) {
                        this.assist.stopSkillEffects(effect.getSkill().getId());
                    }
                }
            }

            if (this.assist2.getClassId() != ClassId.SHILLIEN_ELDER && this.assist2.getClassId() != ClassId.SHILLIEN_SAINT && this.assist2.getClassId() != ClassId.BISHOP && this.assist2.getClassId() != ClassId.CARDINAL && this.assist2.getClassId() != ClassId.ELVEN_ELDER && this.assist2.getClassId() != ClassId.EVAS_SAINT) {
                var1 = this.assist2.getAllEffects();
                var2 = var1.length;

                for (var3 = 0; var3 < var2; ++var3) {
                    effect = var1[var3];
                    if (Config.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId())) {
                        this.assist2.stopSkillEffects(effect.getSkill().getId());
                    }
                }
            }

            if (this.assist3.getClassId() != ClassId.SHILLIEN_ELDER && this.assist3.getClassId() != ClassId.SHILLIEN_SAINT && this.assist3.getClassId() != ClassId.BISHOP && this.assist3.getClassId() != ClassId.CARDINAL && this.assist3.getClassId() != ClassId.ELVEN_ELDER && this.assist3.getClassId() != ClassId.EVAS_SAINT) {
                var1 = this.assist3.getAllEffects();
                var2 = var1.length;

                for (var3 = 0; var3 < var2; ++var3) {
                    effect = var1[var3];
                    if (Config.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId())) {
                        this.assist3.stopSkillEffects(effect.getSkill().getId());
                    }
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

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.sendPacket(new ExShowScreenMessage(message, duration * 1000));
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.sendPacket(new ExShowScreenMessage(message, duration * 1000));
            }

        }

        public void inicarContagem(int duration) {
            if (this.leader != null && this.leader.isOnline()) {
                ThreadPool.schedule(Arena4x4.this.new countdown(this.leader, duration), 0L);
            }

            if (this.assist != null && this.assist.isOnline()) {
                ThreadPool.schedule(Arena4x4.this.new countdown(this.assist, duration), 0L);
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                ThreadPool.schedule(Arena4x4.this.new countdown(this.assist2, duration), 0L);
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                ThreadPool.schedule(Arena4x4.this.new countdown(this.assist3, duration), 0L);
            }

        }

        public void sendPacketinit(String message, int duration) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
                if ((this.leader.getClassId() == ClassId.SHILLIEN_ELDER || this.leader.getClassId() == ClassId.SHILLIEN_SAINT || this.leader.getClassId() == ClassId.BISHOP || this.leader.getClassId() == ClassId.CARDINAL || this.leader.getClassId() == ClassId.ELVEN_ELDER || this.leader.getClassId() == ClassId.EVAS_SAINT) && Config.bs_COUNT_4X4 == 0) {
                    ThreadPool.schedule(new Runnable() {
                        public void run() {
                            Pair.this.leader.getClient().closeNow();
                        }
                    }, 100L);
                }
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
                if ((this.assist.getClassId() == ClassId.SHILLIEN_ELDER || this.assist.getClassId() == ClassId.SHILLIEN_SAINT || this.assist.getClassId() == ClassId.BISHOP || this.assist.getClassId() == ClassId.CARDINAL || this.assist.getClassId() == ClassId.ELVEN_ELDER || this.assist.getClassId() == ClassId.EVAS_SAINT) && Config.bs_COUNT_4X4 == 0) {
                    ThreadPool.schedule(new Runnable() {
                        public void run() {
                            Pair.this.assist.getClient().closeNow();
                        }
                    }, 100L);
                }
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
                if ((this.assist2.getClassId() == ClassId.SHILLIEN_ELDER || this.assist2.getClassId() == ClassId.SHILLIEN_SAINT || this.assist2.getClassId() == ClassId.BISHOP || this.assist2.getClassId() == ClassId.CARDINAL || this.assist2.getClassId() == ClassId.ELVEN_ELDER || this.assist2.getClassId() == ClassId.EVAS_SAINT) && Config.bs_COUNT_4X4 == 0) {
                    ThreadPool.schedule(new Runnable() {
                        public void run() {
                            Pair.this.assist2.getClient().closeNow();
                        }
                    }, 100L);
                }
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
                if ((this.assist3.getClassId() == ClassId.SHILLIEN_ELDER || this.assist3.getClassId() == ClassId.SHILLIEN_SAINT || this.assist3.getClassId() == ClassId.BISHOP || this.assist3.getClassId() == ClassId.CARDINAL || this.assist3.getClassId() == ClassId.ELVEN_ELDER || this.assist3.getClassId() == ClassId.EVAS_SAINT) && Config.bs_COUNT_4X4 == 0) {
                    ThreadPool.schedule(new Runnable() {
                        public void run() {
                            Pair.this.assist3.getClient().closeNow();
                        }
                    }, 100L);
                }
            }

        }
    }

    private class EvtArenaTask implements Runnable {
        private final Arena4x4.Pair pairOne;
        private final Arena4x4.Pair pairTwo;
        private final int pOneX;
        private final int pOneY;
        private final int pOneZ;
        private final int pTwoX;
        private final int pTwoY;
        private final int pTwoZ;
        private Arena4x4.Arena arena;

        public EvtArenaTask(List<Arena4x4.Pair> opponents) {
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
            --Arena4x4.this.free;
            this.pairOne.saveTitle();
            this.pairTwo.saveTitle();
            this.portPairsToArena();
            this.pairOne.inicarContagem(Config.ARENA_WAIT_INTERVAL_4X4);
            this.pairTwo.inicarContagem(Config.ARENA_WAIT_INTERVAL_4X4);

            try {
                Thread.sleep((long) (Config.ARENA_WAIT_INTERVAL_4X4 * 1000L));
            } catch (InterruptedException var3) {
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
                } catch (InterruptedException var2) {
                }
            }

            this.finishDuel();
            Arena4x4 this$2 = Arena4x4.this;
            ++this$2.free;
        }

        private void finishDuel() {
            Arena4x4.this.fights.remove(this.arena.id);
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
            String var10000;
            Player leader1;
            Player leader2;
            if (this.pairOne.isAlive() && !this.pairTwo.isAlive()) {
                leader1 = this.pairOne.getLeader();
                leader2 = this.pairTwo.getLeader();
                if (leader1.getClan() != null && leader2.getClan() != null && Config.TOURNAMENT_EVENT_ANNOUNCE) {
                    var10000 = leader1.getClan().getName();
                    World.announceToOnlinePlayers("[4x4]: (" + var10000 + " VS " + leader2.getClan().getName() + ") ~> " + leader1.getClan().getName() + " win!");
                }

                this.pairOne.rewards();
                this.pairTwo.rewardsLost();
            } else if (this.pairTwo.isAlive() && !this.pairOne.isAlive()) {
                leader1 = this.pairTwo.getLeader();
                leader2 = this.pairOne.getLeader();
                if (leader1.getClan() != null && leader2.getClan() != null && Config.TOURNAMENT_EVENT_ANNOUNCE) {
                    var10000 = leader1.getClan().getName();
                    World.announceToOnlinePlayers("[4x4]: (" + var10000 + " VS " + leader2.getClan().getName() + ") ~> " + leader1.getClan().getName() + " win!");
                }

                this.pairTwo.rewards();
                this.pairOne.rewardsLost();
            }

        }

        private boolean check() {
            return this.pairOne.isDead() && this.pairTwo.isDead();
        }

        private void portPairsToArena() {
            Arena4x4.Arena[] var1 = Arena4x4.this.arenas;
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                Arena4x4.Arena arena = var1[var3];
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
                    Map var10000 = Arena4x4.this.fights;
                    Integer var10001 = this.arena.id;
                    String var10002 = this.pairOne.getLeader().getName();
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
                        this._player.sendMessage(this._time + " second(s) to start the battle!");

                        this._player.setIsParalyzed(false);
                        break;
                    case 2, 5, 4, 3:
                        this._player.sendMessage(this._time + " second(s) to start the battle!");

                        this._player.setIsParalyzed(true);
                        break;

                    case 10:
                        this._player.sendMessage(this._time + " second(s) to start the battle!");

                        this._player.setIsParalyzed(true);
                        this._player.broadcastPacket(new SocialAction(this._player, 5));
                        break;
                    case 15:
                        this._player.sendPacket(new ExShowScreenMessage(this._time + " ..", 3000));
                        this._player.sendMessage(this._time + " second(s) to start the battle!");
                        this._player.setIsParalyzed(true);
                        this._player.broadcastPacket(new SocialAction(this._player, 9));
                        break;
                    case 20:
                        this._player.sendPacket(new ExShowScreenMessage(this._time + " ..", 3000));
                        this._player.sendMessage(this._time + " second(s) to start the battle!");
                        this._player.setIsParalyzed(true);
                        break;
                    case 27:
                        this._player.sendPacket(new ExShowScreenMessage("The battle starts in 30 second(s)..", 4000));
                        this._player.sendMessage("30 second(s) to start the battle!");
                        this._player.setIsParalyzed(true);
                        this._player.broadcastPacket(new SocialAction(this._player, 2));
                        break;
                    case 45:
                        this._player.sendPacket(new ExShowScreenMessage(this._time + " ..", 3000));
                        this._player.sendMessage(this._time + " second(s) to start the battle!");
                        this._player.setIsParalyzed(true);
                        this._player.broadcastPacket(new SocialAction(this._player, 1));
                        break;
                    case 60:
                    case 120:
                    case 180:
                    case 240:
                    case 300:
                        this._player.sendPacket(new ExShowScreenMessage("The battle starts in " + this._time + " second(s)..", 4000));
                        this._player.sendMessage(this._time + " second(s) to start the battle.");
                        this._player.setIsParalyzed(true);
                }

                if (this._time > 1) {
                    ThreadPool.schedule(Arena4x4.this.new countdown(this._player, this._time - 1), 1000L);
                }
            }

        }
    }
}