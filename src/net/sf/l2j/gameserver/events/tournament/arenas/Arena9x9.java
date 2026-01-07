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

public class Arena9x9 implements Runnable {
    public static List<Arena9x9.Pair> registered;
    int free;
    Arena9x9.Arena[] arenas;
    Map<Integer, String> fights;

    public Arena9x9() {
        this.free = Config.ARENA_EVENT_COUNT_9X9;
        this.arenas = new Arena9x9.Arena[Config.ARENA_EVENT_COUNT_9X9];
        this.fights = new HashMap(Config.ARENA_EVENT_COUNT_9X9);
        registered = new ArrayList();

        for (int i = 0; i < Config.ARENA_EVENT_COUNT_9X9; ++i) {
            int[] coord = Config.ARENA_EVENT_LOCS_9X9[i];
            this.arenas[i] = new Arena(this, i, coord[0], coord[1], coord[2]);
        }

    }

    public static Arena9x9 getInstance() {
        return Arena9x9.SingletonHolder.INSTANCE;
    }

    public boolean register(Player player, Player assist, Player assist2, Player assist3, Player assist4, Player assist5, Player assist6, Player assist7, Player assist8) {
        Iterator var10 = registered.iterator();

        while (var10.hasNext()) {
            Arena9x9.Pair p = (Arena9x9.Pair) var10.next();
            if (p.getLeader() != player && p.getAssist() != player) {
                if (p.getLeader() != assist && p.getAssist() != assist) {
                    if (p.getLeader() != assist2 && p.getAssist2() != assist2) {
                        if (p.getLeader() != assist3 && p.getAssist3() != assist3) {
                            if (p.getLeader() != assist4 && p.getAssist4() != assist4) {
                                if (p.getLeader() != assist5 && p.getAssist5() != assist5) {
                                    if (p.getLeader() != assist6 && p.getAssist6() != assist6) {
                                        if (p.getLeader() != assist7 && p.getAssist7() != assist7) {
                                            if (p.getLeader() != assist8 && p.getAssist8() != assist8) {
                                                continue;
                                            }

                                            player.sendMessage("Tournament: " + assist8.getName() + " already registered!");
                                            return false;
                                        }

                                        player.sendMessage("Tournament: " + assist7.getName() + " already registered!");
                                        return false;
                                    }

                                    player.sendMessage("Tournament: " + assist6.getName() + " already registered!");
                                    return false;
                                }

                                player.sendMessage("Tournament: " + assist5.getName() + " already registered!");
                                return false;
                            }

                            player.sendMessage("Tournament: " + assist4.getName() + " already registered!");
                            return false;
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

        return registered.add(new Arena9x9.Pair(player, assist, assist2, assist3, assist4, assist5, assist6, assist7, assist8));
    }

    public boolean isRegistered(Player player) {
        Iterator var2 = registered.iterator();

        Arena9x9.Pair p;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            p = (Arena9x9.Pair) var2.next();
        } while (p.getLeader() != player && p.getAssist() != player && p.getAssist2() != player && p.getAssist3() != player && p.getAssist4() != player && p.getAssist5() != player && p.getAssist6() != player && p.getAssist7() != player && p.getAssist8() != player);

        return true;
    }

    public Map<Integer, String> getFights() {
        return this.fights;
    }

    public boolean remove(Player player) {
        Iterator var2 = registered.iterator();

        Arena9x9.Pair p;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            p = (Arena9x9.Pair) var2.next();
        } while (p.getLeader() != player && p.getAssist() != player && p.getAssist2() != player && p.getAssist3() != player && p.getAssist4() != player && p.getAssist5() != player && p.getAssist6() != player && p.getAssist7() != player && p.getAssist8() != player);

        p.removeMessage();
        registered.remove(p);
        return true;
    }

    public synchronized void run() {
        while (true) {
            if (registered.size() >= 2 && this.free != 0) {
                List<Arena9x9.Pair> opponents = this.selectOpponents();
                if (opponents != null && opponents.size() == 2) {
                    Thread T = new Thread(new Arena9x9.EvtArenaTask(opponents));
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

    private List<Arena9x9.Pair> selectOpponents() {
        List<Arena9x9.Pair> opponents = new ArrayList();
        Arena9x9.Pair pairOne = null;
        Arena9x9.Pair pairTwo = null;
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
        protected static final Arena9x9 INSTANCE = new Arena9x9();
    }

    private static class Arena {
        protected int x;
        protected int y;
        protected int z;
        protected boolean isFree = true;
        int id;

        public Arena(final Arena9x9 param1, int id, int x, int y, int z) {
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
        private final Player leader;
        private final Player assist;
        private final Player assist2;
        private final Player assist3;
        private final Player assist4;
        private final Player assist5;
        private final Player assist6;
        private final Player assist7;
        private final Player assist8;

        public Pair(Player leader, Player assist, Player assist2, Player assist3, Player assist4, Player assist5, Player assist6, Player assist7, Player assist8) {
            this.leader = leader;
            this.assist = assist;
            this.assist2 = assist2;
            this.assist3 = assist3;
            this.assist4 = assist4;
            this.assist5 = assist5;
            this.assist6 = assist6;
            this.assist7 = assist7;
            this.assist8 = assist8;
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

        public Player getAssist4() {
            return this.assist4;
        }

        public Player getAssist5() {
            return this.assist5;
        }

        public Player getAssist6() {
            return this.assist6;
        }

        public Player getAssist7() {
            return this.assist7;
        }

        public Player getAssist8() {
            return this.assist8;
        }

        public Player getLeader() {
            return this.leader;
        }

        public boolean check() {
            if (this.leader != null && this.leader.isOnline()) {
                if (this.assist != null && this.assist.isOnline() && this.assist2 != null && this.assist2.isOnline() && this.assist3 != null && this.assist3.isOnline() && this.assist4 != null && this.assist4.isOnline() && this.assist5 != null && this.assist5.isOnline() && this.assist6 != null && this.assist6.isOnline() && this.assist7 != null && this.assist7.isOnline() && this.assist8 != null && this.assist8.isOnline()) {
                    return true;
                } else {
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

                    if (this.assist4 != null || this.assist4.isOnline()) {
                        this.assist4.sendMessage("Tournament: You participation in Event was Canceled.");
                    }

                    if (this.assist5 != null || this.assist5.isOnline()) {
                        this.assist5.sendMessage("Tournament: You participation in Event was Canceled.");
                    }

                    if (this.assist6 != null || this.assist6.isOnline()) {
                        this.assist6.sendMessage("Tournament: You participation in Event was Canceled.");
                    }

                    if (this.assist7 != null || this.assist7.isOnline()) {
                        this.assist7.sendMessage("Tournament: You participation in Event was Canceled.");
                    }

                    if (this.assist8 != null || this.assist8.isOnline()) {
                        this.assist8.sendMessage("Tournament: You participation in Event was Canceled.");
                    }

                    return false;
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

                if (this.assist4 != null || this.assist4.isOnline()) {
                    this.assist4.sendMessage("Tournament: You participation in Event was Canceled.");
                }

                if (this.assist5 != null || this.assist5.isOnline()) {
                    this.assist5.sendMessage("Tournament: You participation in Event was Canceled.");
                }

                if (this.assist6 != null || this.assist6.isOnline()) {
                    this.assist6.sendMessage("Tournament: You participation in Event was Canceled.");
                }

                if (this.assist7 != null || this.assist7.isOnline()) {
                    this.assist7.sendMessage("Tournament: You participation in Event was Canceled.");
                }

                if (this.assist8 != null || this.assist8.isOnline()) {
                    this.assist8.sendMessage("Tournament: You participation in Event was Canceled.");
                }

                return false;
            }
        }

        public boolean isDead() {
            if ((this.leader == null || this.leader.isDead() || !this.leader.isOnline() || !this.leader.isInsideZone(ZoneId.ARENA_EVENT) || !this.leader.isArenaAttack()) && (this.assist == null || this.assist.isDead() || !this.assist.isOnline() || !this.assist.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist.isArenaAttack()) && (this.assist2 == null || this.assist2.isDead() || !this.assist2.isOnline() || !this.assist2.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist2.isArenaAttack()) && (this.assist3 == null || this.assist3.isDead() || !this.assist3.isOnline() || !this.assist3.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist3.isArenaAttack()) && (this.assist4 == null || this.assist4.isDead() || !this.assist4.isOnline() || !this.assist4.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist4.isArenaAttack()) && (this.assist5 == null || this.assist5.isDead() || !this.assist5.isOnline() || !this.assist5.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist5.isArenaAttack()) && (this.assist6 == null || this.assist6.isDead() || !this.assist6.isOnline() || !this.assist6.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist6.isArenaAttack()) && (this.assist7 == null || this.assist7.isDead() || !this.assist7.isOnline() || !this.assist7.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist7.isArenaAttack()) && (this.assist8 == null || this.assist8.isDead() || !this.assist8.isOnline() || !this.assist8.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist8.isArenaAttack())) {
                return false;
            } else {
                return !this.leader.isDead() || !this.assist.isDead() || !this.assist2.isDead() || !this.assist3.isDead() || !this.assist4.isDead() || !this.assist5.isDead() || !this.assist6.isDead() || !this.assist7.isDead() || !this.assist8.isDead();
            }
        }

        public boolean isAlive() {
            if ((this.leader == null || this.leader.isDead() || !this.leader.isOnline() || !this.leader.isInsideZone(ZoneId.ARENA_EVENT) || !this.leader.isArenaAttack()) && (this.assist == null || this.assist.isDead() || !this.assist.isOnline() || !this.assist.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist.isArenaAttack()) && (this.assist2 == null || this.assist2.isDead() || !this.assist2.isOnline() || !this.assist2.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist2.isArenaAttack()) && (this.assist3 == null || this.assist3.isDead() || !this.assist3.isOnline() || !this.assist3.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist3.isArenaAttack()) && (this.assist4 == null || this.assist4.isDead() || !this.assist4.isOnline() || !this.assist4.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist4.isArenaAttack()) && (this.assist5 == null || this.assist5.isDead() || !this.assist5.isOnline() || !this.assist5.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist5.isArenaAttack()) && (this.assist6 == null || this.assist6.isDead() || !this.assist6.isOnline() || !this.assist6.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist6.isArenaAttack()) && (this.assist7 == null || this.assist7.isDead() || !this.assist7.isOnline() || !this.assist7.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist7.isArenaAttack()) && (this.assist8 == null || this.assist8.isDead() || !this.assist8.isOnline() || !this.assist8.isInsideZone(ZoneId.ARENA_EVENT) || !this.assist8.isArenaAttack())) {
                return false;
            } else {
                return !this.leader.isDead() || !this.assist.isDead() || !this.assist2.isDead() || !this.assist3.isDead() || !this.assist4.isDead() || !this.assist5.isDead() || !this.assist6.isDead() || !this.assist7.isDead() || !this.assist8.isDead();
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
                    this.assist.setLastCords(x, y + 200, z);
                    this.assist.leaveOlympiadObserverMode();
                } else {
                    this.assist.teleportTo(x, y + 200, z, 0);
                }

                this.assist.broadcastUserInfo();
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.getAppearance().getInvisible();
                this.assist2.setCurrentCp(this.assist2.getMaxCp());
                this.assist2.setCurrentHp(this.assist2.getMaxHp());
                this.assist2.setCurrentMp(this.assist2.getMaxMp());
                if (this.assist2.isInObserverMode()) {
                    this.assist2.setLastCords(x, y + 150, z);
                    this.assist2.leaveOlympiadObserverMode();
                } else {
                    this.assist2.teleportTo(x, y + 150, z, 0);
                }

                this.assist2.broadcastUserInfo();
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.getAppearance().getInvisible();
                this.assist3.setCurrentCp(this.assist3.getMaxCp());
                this.assist3.setCurrentHp(this.assist3.getMaxHp());
                this.assist3.setCurrentMp(this.assist3.getMaxMp());
                if (this.assist3.isInObserverMode()) {
                    this.assist3.setLastCords(x, y + 100, z);
                    this.assist3.leaveOlympiadObserverMode();
                } else {
                    this.assist3.teleportTo(x, y + 100, z, 0);
                }

                this.assist3.broadcastUserInfo();
            }

            if (this.assist4 != null && this.assist4.isOnline()) {
                this.assist4.getAppearance().getInvisible();
                this.assist4.setCurrentCp(this.assist4.getMaxCp());
                this.assist4.setCurrentHp(this.assist4.getMaxHp());
                this.assist4.setCurrentMp(this.assist4.getMaxMp());
                if (this.assist4.isInObserverMode()) {
                    this.assist4.setLastCords(x, y + 50, z);
                    this.assist4.leaveOlympiadObserverMode();
                } else {
                    this.assist4.teleportTo(x, y + 50, z, 0);
                }

                this.assist4.broadcastUserInfo();
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                this.assist5.getAppearance().getInvisible();
                this.assist5.setCurrentCp(this.assist5.getMaxCp());
                this.assist5.setCurrentHp(this.assist5.getMaxHp());
                this.assist5.setCurrentMp(this.assist5.getMaxMp());
                if (this.assist5.isInObserverMode()) {
                    this.assist5.setLastCords(x, y - 200, z);
                    this.assist5.leaveOlympiadObserverMode();
                } else {
                    this.assist5.teleportTo(x, y - 200, z, 0);
                }

                this.assist5.broadcastUserInfo();
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                this.assist6.getAppearance().getInvisible();
                this.assist6.setCurrentCp(this.assist6.getMaxCp());
                this.assist6.setCurrentHp(this.assist6.getMaxHp());
                this.assist6.setCurrentMp(this.assist6.getMaxMp());
                if (this.assist6.isInObserverMode()) {
                    this.assist6.setLastCords(x, y - 150, z);
                    this.assist6.leaveOlympiadObserverMode();
                } else {
                    this.assist6.teleportTo(x, y - 150, z, 0);
                }

                this.assist6.broadcastUserInfo();
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                this.assist7.getAppearance().getInvisible();
                this.assist7.setCurrentCp(this.assist7.getMaxCp());
                this.assist7.setCurrentHp(this.assist7.getMaxHp());
                this.assist7.setCurrentMp(this.assist7.getMaxMp());
                if (this.assist7.isInObserverMode()) {
                    this.assist7.setLastCords(x, y - 100, z);
                    this.assist7.leaveOlympiadObserverMode();
                } else {
                    this.assist7.teleportTo(x, y - 100, z, 0);
                }

                this.assist7.broadcastUserInfo();
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                this.assist8.getAppearance().getInvisible();
                this.assist8.setCurrentCp(this.assist8.getMaxCp());
                this.assist8.setCurrentHp(this.assist8.getMaxHp());
                this.assist8.setCurrentMp(this.assist8.getMaxMp());
                if (this.assist8.isInObserverMode()) {
                    this.assist8.setLastCords(x, y - 50, z);
                    this.assist8.leaveOlympiadObserverMode();
                } else {
                    this.assist8.teleportTo(x, y - 50, z, 0);
                }

                this.assist8.broadcastUserInfo();
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

            if (this.assist4 != null && this.assist4.isOnline()) {
                this.assist4.setTitle(title);
                this.assist4.getAppearance().setTitleColor(Integer.decode("0x" + color));
                this.assist4.broadcastUserInfo();
                this.assist4.broadcastTitleInfo();
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                this.assist5.setTitle(title);
                this.assist5.getAppearance().setTitleColor(Integer.decode("0x" + color));
                this.assist5.broadcastUserInfo();
                this.assist5.broadcastTitleInfo();
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                this.assist6.setTitle(title);
                this.assist6.getAppearance().setTitleColor(Integer.decode("0x" + color));
                this.assist6.broadcastUserInfo();
                this.assist6.broadcastTitleInfo();
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                this.assist7.setTitle(title);
                this.assist7.getAppearance().setTitleColor(Integer.decode("0x" + color));
                this.assist7.broadcastUserInfo();
                this.assist7.broadcastTitleInfo();
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                this.assist8.setTitle(title);
                this.assist8.getAppearance().setTitleColor(Integer.decode("0x" + color));
                this.assist8.broadcastUserInfo();
                this.assist8.broadcastTitleInfo();
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

            if (this.assist4 != null && this.assist4.isOnline()) {
                this.assist4._originalTitleColorTournament = this.assist4.getAppearance().getTitleColor();
                this.assist4._originalTitleTournament = this.assist4.getTitle();
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                this.assist5._originalTitleColorTournament = this.assist5.getAppearance().getTitleColor();
                this.assist5._originalTitleTournament = this.assist5.getTitle();
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                this.assist6._originalTitleColorTournament = this.assist6.getAppearance().getTitleColor();
                this.assist6._originalTitleTournament = this.assist6.getTitle();
                this.assist6._originalTitleTournament = this.assist6.getTitle();
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                this.assist7._originalTitleColorTournament = this.assist7.getAppearance().getTitleColor();
                this.assist7._originalTitleTournament = this.assist7.getTitle();
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                this.assist8._originalTitleColorTournament = this.assist8.getAppearance().getTitleColor();
                this.assist8._originalTitleTournament = this.assist8.getTitle();
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

            if (this.assist4 != null && this.assist4.isOnline()) {
                this.assist4.setTitle(this.assist4._originalTitleTournament);
                this.assist4.getAppearance().setTitleColor(this.assist4._originalTitleColorTournament);
                this.assist4.broadcastUserInfo();
                this.assist4.broadcastTitleInfo();
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                this.assist5.setTitle(this.assist5._originalTitleTournament);
                this.assist5.getAppearance().setTitleColor(this.assist5._originalTitleColorTournament);
                this.assist5.broadcastUserInfo();
                this.assist5.broadcastTitleInfo();
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                this.assist6.setTitle(this.assist6._originalTitleTournament);
                this.assist6.getAppearance().setTitleColor(this.assist6._originalTitleColorTournament);
                this.assist6.broadcastUserInfo();
                this.assist6.broadcastTitleInfo();
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                this.assist7.setTitle(this.assist7._originalTitleTournament);
                this.assist7.getAppearance().setTitleColor(this.assist7._originalTitleColorTournament);
                this.assist7.broadcastUserInfo();
                this.assist7.broadcastTitleInfo();
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                this.assist8.setTitle(this.assist8._originalTitleTournament);
                this.assist8.getAppearance().setTitleColor(this.assist8._originalTitleColorTournament);
                this.assist8.broadcastUserInfo();
                this.assist8.broadcastTitleInfo();
            }

        }

        public void rewards() {
            if (this.leader != null && this.leader.isOnline()) {
                if (this.leader.isVip()) {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.leader, true);
                } else {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9, this.leader, true);
                }
            }

            if (this.assist != null && this.assist.isOnline()) {
                if (this.assist.isVip()) {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist, true);
                } else {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9, this.assist, true);
                }
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                if (this.assist2.isVip()) {
                    this.assist2.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist2, true);
                } else {
                    this.assist2.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9, this.assist2, true);
                }
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                if (this.assist3.isVip()) {
                    this.assist3.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist3, true);
                } else {
                    this.assist3.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9, this.assist3, true);
                }
            }

            if (this.assist4 != null && this.assist4.isOnline()) {
                if (this.assist4.isVip()) {
                    this.assist4.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist4, true);
                } else {
                    this.assist4.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9, this.assist4, true);
                }
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                if (this.assist5.isVip()) {
                    this.assist5.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist5, true);
                } else {
                    this.assist5.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9, this.assist5, true);
                }
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                if (this.assist6.isVip()) {
                    this.assist6.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist6, true);
                } else {
                    this.assist6.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9, this.assist6, true);
                }
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                if (this.assist7.isVip()) {
                    this.assist7.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist7, true);
                } else {
                    this.assist7.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9, this.assist7, true);
                }
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                if (this.assist8.isVip()) {
                    this.assist8.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist8, true);
                } else {
                    this.assist8.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_WIN_REWARD_COUNT_9X9, this.assist8, true);
                }
            }

            this.sendPacket("Congratulations, your team won the event!", 5);
        }

        public void rewardsLost() {
            if (this.leader != null && this.leader.isOnline()) {
                if (this.leader.isVip()) {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.leader, true);
                } else {
                    this.leader.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9, this.leader, true);
                }
            }

            if (this.assist != null && this.assist.isOnline()) {
                if (this.assist.isVip()) {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist, true);
                } else {
                    this.assist.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9, this.assist, true);
                }
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                if (this.assist2.isVip()) {
                    this.assist2.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist2, true);
                } else {
                    this.assist2.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9, this.assist2, true);
                }
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                if (this.assist3.isVip()) {
                    this.assist3.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist3, true);
                } else {
                    this.assist3.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9, this.assist3, true);
                }
            }

            if (this.assist4 != null && this.assist4.isOnline()) {
                if (this.assist4.isVip()) {
                    this.assist4.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist4, true);
                } else {
                    this.assist4.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9, this.assist4, true);
                }
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                if (this.assist5.isVip()) {
                    this.assist5.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist5, true);
                } else {
                    this.assist5.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9, this.assist5, true);
                }
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                if (this.assist6.isVip()) {
                    this.assist6.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist6, true);
                } else {
                    this.assist6.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9, this.assist6, true);
                }
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                if (this.assist7.isVip()) {
                    this.assist7.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist7, true);
                } else {
                    this.assist7.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9, this.assist7, true);
                }
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                if (this.assist8.isVip()) {
                    this.assist8.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9 * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, this.assist8, true);
                } else {
                    this.assist8.addItem("Arena_Event", Config.ARENA_REWARD_ID, Config.ARENA_LOST_REWARD_COUNT_9X9, this.assist8, true);
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

            if (this.assist4 != null && this.assist4.isOnline()) {
                this.assist4.setInArenaEvent(val);
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                this.assist5.setInArenaEvent(val);
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                this.assist6.setInArenaEvent(val);
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                this.assist7.setInArenaEvent(val);
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                this.assist8.setInArenaEvent(val);
            }

        }

        public void removeMessage() {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.sendMessage("Tournament: Your participation has been removed.");
                this.leader.setArenaProtection(false);
                this.leader.setArena9x9(false);
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.sendMessage("Tournament: Your participation has been removed.");
                this.assist.setArenaProtection(false);
                this.assist.setArena9x9(false);
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.sendMessage("Tournament: Your participation has been removed.");
                this.assist2.setArenaProtection(false);
                this.assist2.setArena9x9(false);
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.sendMessage("Tournament: Your participation has been removed.");
                this.assist3.setArenaProtection(false);
                this.assist3.setArena9x9(false);
            }

            if (this.assist4 != null && this.assist4.isOnline()) {
                this.assist4.sendMessage("Tournament: Your participation has been removed.");
                this.assist4.setArenaProtection(false);
                this.assist4.setArena9x9(false);
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                this.assist5.sendMessage("Tournament: Your participation has been removed.");
                this.assist5.setArenaProtection(false);
                this.assist5.setArena9x9(false);
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                this.assist6.sendMessage("Tournament: Your participation has been removed.");
                this.assist6.setArenaProtection(false);
                this.assist6.setArena9x9(false);
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                this.assist7.sendMessage("Tournament: Your participation has been removed.");
                this.assist7.setArenaProtection(false);
                this.assist7.setArena9x9(false);
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                this.assist8.sendMessage("Tournament: Your participation has been removed.");
                this.assist8.setArenaProtection(false);
                this.assist8.setArena9x9(false);
            }

        }

        public void setArenaProtection(boolean val) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.setArenaProtection(val);
                this.leader.setArena9x9(val);
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.setArenaProtection(val);
                this.assist.setArena9x9(val);
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.setArenaProtection(val);
                this.assist2.setArena9x9(val);
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.setArenaProtection(val);
                this.assist3.setArena9x9(val);
            }

            if (this.assist4 != null && this.assist4.isOnline()) {
                this.assist4.setArenaProtection(val);
                this.assist4.setArena9x9(val);
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                this.assist5.setArenaProtection(val);
                this.assist5.setArena9x9(val);
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                this.assist6.setArenaProtection(val);
                this.assist6.setArena9x9(val);
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                this.assist7.setArenaProtection(val);
                this.assist7.setArena9x9(val);
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                this.assist8.setArenaProtection(val);
                this.assist8.setArena9x9(val);
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

            if (this.assist4 != null && this.assist4.isOnline() && this.assist4.isDead()) {
                this.assist4.doRevive();
            }

            if (this.assist5 != null && this.assist5.isOnline() && this.assist5.isDead()) {
                this.assist5.doRevive();
            }

            if (this.assist6 != null && this.assist6.isOnline() && this.assist6.isDead()) {
                this.assist6.doRevive();
            }

            if (this.assist7 != null && this.assist7.isOnline() && this.assist7.isDead()) {
                this.assist7.doRevive();
            }

            if (this.assist8 != null && this.assist8.isOnline() && this.assist8.isDead()) {
                this.assist8.doRevive();
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

            if (this.assist4 != null && this.assist4.isOnline()) {
                this.assist4.setIsInvul(val);
                this.assist4.setStopArena(val);
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                this.assist5.setIsInvul(val);
                this.assist5.setStopArena(val);
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                this.assist6.setIsInvul(val);
                this.assist6.setStopArena(val);
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                this.assist7.setIsInvul(val);
                this.assist7.setStopArena(val);
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                this.assist8.setIsInvul(val);
                this.assist8.setStopArena(val);
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

            if (this.assist4 != null && this.assist4.isOnline()) {
                this.assist4.setArenaAttack(val);
                this.assist4.broadcastUserInfo();
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                this.assist5.setArenaAttack(val);
                this.assist5.broadcastUserInfo();
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                this.assist6.setArenaAttack(val);
                this.assist6.broadcastUserInfo();
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                this.assist7.setArenaAttack(val);
                this.assist7.broadcastUserInfo();
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                this.assist8.setArenaAttack(val);
                this.assist8.broadcastUserInfo();
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

            if (this.assist4 != null && this.assist4.isOnline()) {
                if (this.assist4.getSummon() != null) {
                    summon = this.assist4.getSummon();
                    summon.unSummon(summon.getOwner());

                    if (summon instanceof Pet) {
                        summon.unSummon(this.assist4);
                    }
                }

                if (this.assist4.getMountType() == 1 || this.assist4.getMountType() == 2) {
                    this.assist4.dismount();
                }
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                if (this.assist5.getSummon() != null) {
                    summon = this.assist5.getSummon();
                    summon.unSummon(summon.getOwner());

                    if (summon instanceof Pet) {
                        summon.unSummon(this.assist5);
                    }
                }

                if (this.assist5.getMountType() == 1 || this.assist5.getMountType() == 2) {
                    this.assist5.dismount();
                }
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                if (this.assist6.getSummon() != null) {
                    summon = this.assist6.getSummon();
                    summon.unSummon(summon.getOwner());

                    if (summon instanceof Pet) {
                        summon.unSummon(this.assist6);
                    }
                }

                if (this.assist6.getMountType() == 1 || this.assist6.getMountType() == 2) {
                    this.assist6.dismount();
                }
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                if (this.assist7.getSummon() != null) {
                    summon = this.assist7.getSummon();
                    summon.unSummon(summon.getOwner());

                    if (summon instanceof Pet) {
                        summon.unSummon(this.assist7);
                    }
                }

                if (this.assist7.getMountType() == 1 || this.assist7.getMountType() == 2) {
                    this.assist7.dismount();
                }
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                if (this.assist8.getSummon() != null) {
                    summon = this.assist8.getSummon();
                    summon.unSummon(summon.getOwner());

                    if (summon instanceof Pet) {
                        summon.unSummon(this.assist8);
                    }
                }

                if (this.assist8.getMountType() == 1 || this.assist8.getMountType() == 2) {
                    this.assist8.dismount();
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

            if (this.assist4.getClassId() != ClassId.SHILLIEN_ELDER && this.assist4.getClassId() != ClassId.SHILLIEN_SAINT && this.assist4.getClassId() != ClassId.BISHOP && this.assist4.getClassId() != ClassId.CARDINAL && this.assist4.getClassId() != ClassId.ELVEN_ELDER && this.assist4.getClassId() != ClassId.EVAS_SAINT) {
                var1 = this.assist4.getAllEffects();
                var2 = var1.length;

                for (var3 = 0; var3 < var2; ++var3) {
                    effect = var1[var3];
                    if (Config.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId())) {
                        this.assist4.stopSkillEffects(effect.getSkill().getId());
                    }
                }
            }

            if (this.assist5.getClassId() != ClassId.SHILLIEN_ELDER && this.assist5.getClassId() != ClassId.SHILLIEN_SAINT && this.assist5.getClassId() != ClassId.BISHOP && this.assist5.getClassId() != ClassId.CARDINAL && this.assist5.getClassId() != ClassId.ELVEN_ELDER && this.assist5.getClassId() != ClassId.EVAS_SAINT) {
                var1 = this.assist5.getAllEffects();
                var2 = var1.length;

                for (var3 = 0; var3 < var2; ++var3) {
                    effect = var1[var3];
                    if (Config.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId())) {
                        this.assist5.stopSkillEffects(effect.getSkill().getId());
                    }
                }
            }

            if (this.assist6.getClassId() != ClassId.SHILLIEN_ELDER && this.assist6.getClassId() != ClassId.SHILLIEN_SAINT && this.assist6.getClassId() != ClassId.BISHOP && this.assist6.getClassId() != ClassId.CARDINAL && this.assist6.getClassId() != ClassId.ELVEN_ELDER && this.assist6.getClassId() != ClassId.EVAS_SAINT) {
                var1 = this.assist6.getAllEffects();
                var2 = var1.length;

                for (var3 = 0; var3 < var2; ++var3) {
                    effect = var1[var3];
                    if (Config.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId())) {
                        this.assist6.stopSkillEffects(effect.getSkill().getId());
                    }
                }
            }

            if (this.assist7.getClassId() != ClassId.SHILLIEN_ELDER && this.assist7.getClassId() != ClassId.SHILLIEN_SAINT && this.assist7.getClassId() != ClassId.BISHOP && this.assist7.getClassId() != ClassId.CARDINAL && this.assist7.getClassId() != ClassId.ELVEN_ELDER && this.assist7.getClassId() != ClassId.EVAS_SAINT) {
                var1 = this.assist7.getAllEffects();
                var2 = var1.length;

                for (var3 = 0; var3 < var2; ++var3) {
                    effect = var1[var3];
                    if (Config.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId())) {
                        this.assist7.stopSkillEffects(effect.getSkill().getId());
                    }
                }
            }

            if (this.assist8.getClassId() != ClassId.SHILLIEN_ELDER && this.assist8.getClassId() != ClassId.SHILLIEN_SAINT && this.assist8.getClassId() != ClassId.BISHOP && this.assist8.getClassId() != ClassId.CARDINAL && this.assist8.getClassId() != ClassId.ELVEN_ELDER && this.assist8.getClassId() != ClassId.EVAS_SAINT) {
                var1 = this.assist8.getAllEffects();
                var2 = var1.length;

                for (var3 = 0; var3 < var2; ++var3) {
                    effect = var1[var3];
                    if (Config.ARENA_STOP_SKILL_LIST.contains(effect.getSkill().getId())) {
                        this.assist8.stopSkillEffects(effect.getSkill().getId());
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

            if (this.assist4 != null && this.assist4.isOnline()) {
                this.assist4.sendPacket(new ExShowScreenMessage(message, duration * 1000));
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                this.assist5.sendPacket(new ExShowScreenMessage(message, duration * 1000));
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                this.assist6.sendPacket(new ExShowScreenMessage(message, duration * 1000));
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                this.assist7.sendPacket(new ExShowScreenMessage(message, duration * 1000));
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                this.assist8.sendPacket(new ExShowScreenMessage(message, duration * 1000));
            }

        }

        public void inicarContagem(int duration) {
            if (this.leader != null && this.leader.isOnline()) {
                ThreadPool.schedule(Arena9x9.this.new countdown(this.leader, duration), 0L);
            }

            if (this.assist != null && this.assist.isOnline()) {
                ThreadPool.schedule(Arena9x9.this.new countdown(this.assist, duration), 0L);
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                ThreadPool.schedule(Arena9x9.this.new countdown(this.assist2, duration), 0L);
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                ThreadPool.schedule(Arena9x9.this.new countdown(this.assist3, duration), 0L);
            }

            if (this.assist4 != null && this.assist4.isOnline()) {
                ThreadPool.schedule(Arena9x9.this.new countdown(this.assist4, duration), 0L);
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                ThreadPool.schedule(Arena9x9.this.new countdown(this.assist5, duration), 0L);
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                ThreadPool.schedule(Arena9x9.this.new countdown(this.assist6, duration), 0L);
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                ThreadPool.schedule(Arena9x9.this.new countdown(this.assist7, duration), 0L);
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                ThreadPool.schedule(Arena9x9.this.new countdown(this.assist8, duration), 0L);
            }

        }

        public void sendPacketinit(String message, int duration) {
            if (this.leader != null && this.leader.isOnline()) {
                this.leader.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
            }

            if (this.assist != null && this.assist.isOnline()) {
                this.assist.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
            }

            if (this.assist2 != null && this.assist2.isOnline()) {
                this.assist2.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
            }

            if (this.assist3 != null && this.assist3.isOnline()) {
                this.assist3.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
            }

            if (this.assist4 != null && this.assist4.isOnline()) {
                this.assist4.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
            }

            if (this.assist5 != null && this.assist5.isOnline()) {
                this.assist5.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
            }

            if (this.assist6 != null && this.assist6.isOnline()) {
                this.assist6.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
            }

            if (this.assist7 != null && this.assist7.isOnline()) {
                this.assist7.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
            }

            if (this.assist8 != null && this.assist8.isOnline()) {
                this.assist8.sendPacket(new ExShowScreenMessage(message, duration * 1000, SMPOS.MIDDLE_LEFT, false));
            }

        }
    }

    private class EvtArenaTask implements Runnable {
        private final Arena9x9.Pair pairOne;
        private final Arena9x9.Pair pairTwo;
        private final int pOneX;
        private final int pOneY;
        private final int pOneZ;
        private final int pTwoX;
        private final int pTwoY;
        private final int pTwoZ;
        private Arena9x9.Arena arena;

        public EvtArenaTask(List<Arena9x9.Pair> opponents) {
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
            --Arena9x9.this.free;
            this.pairOne.saveTitle();
            this.pairTwo.saveTitle();
            this.portPairsToArena();
            this.pairOne.inicarContagem(Config.ARENA_WAIT_INTERVAL_9X9);
            this.pairTwo.inicarContagem(Config.ARENA_WAIT_INTERVAL_9X9);

            try {
                Thread.sleep((long) (Config.ARENA_WAIT_INTERVAL_9X9 * 1000L));
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
            Arena9x9 this$2 = Arena9x9.this;
            ++this$2.free;
        }

        private void finishDuel() {
            Arena9x9.this.fights.remove(this.arena.id);
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
                    World.announceToOnlinePlayers("[9x9]: (" + var10000 + " VS " + leader2.getClan().getName() + ") ~> " + leader1.getClan().getName() + " win!");
                }

                this.pairOne.rewards();
                this.pairTwo.rewardsLost();
            } else if (this.pairTwo.isAlive() && !this.pairOne.isAlive()) {
                leader1 = this.pairTwo.getLeader();
                leader2 = this.pairOne.getLeader();
                if (leader1.getClan() != null && leader2.getClan() != null && Config.TOURNAMENT_EVENT_ANNOUNCE) {
                    var10000 = leader1.getClan().getName();
                    World.announceToOnlinePlayers("[9x9]: (" + var10000 + " VS " + leader2.getClan().getName() + ") ~> " + leader1.getClan().getName() + " win!");
                }

                this.pairTwo.rewards();
                this.pairOne.rewardsLost();
            }

        }

        private boolean check() {
            return this.pairOne.isDead() && this.pairTwo.isDead();
        }

        private void portPairsToArena() {
            Arena9x9.Arena[] var1 = Arena9x9.this.arenas;
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                Arena9x9.Arena arena = var1[var3];
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
                    Map var10000 = Arena9x9.this.fights;
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
                    ThreadPool.schedule(Arena9x9.this.new countdown(this._player, this._time - 1), 1000L);
                }
            }

        }
    }
}