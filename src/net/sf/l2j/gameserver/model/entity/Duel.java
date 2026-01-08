/**/
package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.manager.DuelManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.TeamType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

public class Duel {
    private static final PlaySound B04_S01 = new PlaySound(1, "B04_S01");
    private final int _duelId;
    private final boolean _isPartyDuel;
    private final Calendar _duelEndTime;
    private final Player _playerA;
    private final Player _playerB;
    private final List<PlayerCondition> _playerConditions = new CopyOnWriteArrayList<>();
    protected Future<?> _startTask = null;
    protected Future<?> _checkTask = null;
    protected int _countdown = 5;
    private int _surrenderRequest;

    public Duel(Player playerA, Player playerB, boolean isPartyDuel, int duelId) {
        this._duelId = duelId;
        this._playerA = playerA;
        this._playerB = playerB;
        this._isPartyDuel = isPartyDuel;
        this._duelEndTime = Calendar.getInstance();
        this._duelEndTime.add(13, 120);
        if (this._isPartyDuel) {
            this._countdown = 35;
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE);
            this.broadcastToTeam1(sm);
            this.broadcastToTeam2(sm);

            for (Player partyPlayer : this._playerA.getParty().getMembers()) {
                partyPlayer.setInDuel(this._duelId);
            }

            for (Player partyPlayer : this._playerB.getParty().getMembers()) {
                partyPlayer.setInDuel(this._duelId);
            }
        } else {
            this._playerA.setInDuel(this._duelId);
            this._playerB.setInDuel(this._duelId);
        }

        this.savePlayerConditions();
        this._startTask = ThreadPool.scheduleAtFixedRate(new StartTask(), 1000L, 1000L);
        this._checkTask = ThreadPool.scheduleAtFixedRate(new CheckTask(), 1000L, 1000L);
    }

    protected void stopFighting() {
        if (this._isPartyDuel) {
            for (Player partyPlayer : this._playerA.getParty().getMembers()) {
                partyPlayer.abortCast();
                partyPlayer.getAI().setIntention(IntentionType.ACTIVE);
                partyPlayer.setTarget(null);
                partyPlayer.sendPacket(ActionFailed.STATIC_PACKET);
            }

            for (Player partyPlayer : this._playerB.getParty().getMembers()) {
                partyPlayer.abortCast();
                partyPlayer.getAI().setIntention(IntentionType.ACTIVE);
                partyPlayer.setTarget(null);
                partyPlayer.sendPacket(ActionFailed.STATIC_PACKET);
            }
        } else {
            this._playerA.abortCast();
            this._playerB.abortCast();
            this._playerA.getAI().setIntention(IntentionType.ACTIVE);
            this._playerA.setTarget(null);
            this._playerB.getAI().setIntention(IntentionType.ACTIVE);
            this._playerB.setTarget(null);
            this._playerA.sendPacket(ActionFailed.STATIC_PACKET);
            this._playerB.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    protected void startDuel() {
        if (this._isPartyDuel) {
            for (Player partyPlayer : this._playerA.getParty().getMembers()) {
                partyPlayer.cancelActiveTrade();
                partyPlayer.setDuelState(Duel.DuelState.DUELLING);
                partyPlayer.setTeam(TeamType.BLUE);
                partyPlayer.broadcastUserInfo();
                Summon summon = partyPlayer.getSummon();
                if (summon != null) {
                    summon.updateAbnormalEffect();
                }

                this.broadcastToTeam2(new ExDuelUpdateUserInfo(partyPlayer));
            }

            for (Player partyPlayer : this._playerB.getParty().getMembers()) {
                partyPlayer.cancelActiveTrade();
                partyPlayer.setDuelState(Duel.DuelState.DUELLING);
                partyPlayer.setTeam(TeamType.RED);
                partyPlayer.broadcastUserInfo();
                Summon summon = partyPlayer.getSummon();
                if (summon != null) {
                    summon.updateAbnormalEffect();
                }

                this.broadcastToTeam1(new ExDuelUpdateUserInfo(partyPlayer));
            }

            ExDuelReady ready = new ExDuelReady(true);
            ExDuelStart start = new ExDuelStart(true);
            this.broadcastToTeam1(ready);
            this.broadcastToTeam2(ready);
            this.broadcastToTeam1(start);
            this.broadcastToTeam2(start);
        } else {
            this._playerA.setDuelState(Duel.DuelState.DUELLING);
            this._playerA.setTeam(TeamType.BLUE);
            this._playerB.setDuelState(Duel.DuelState.DUELLING);
            this._playerB.setTeam(TeamType.RED);
            ExDuelReady ready = new ExDuelReady(false);
            ExDuelStart start = new ExDuelStart(false);
            this.broadcastToTeam1(ready);
            this.broadcastToTeam2(ready);
            this.broadcastToTeam1(start);
            this.broadcastToTeam2(start);
            this.broadcastToTeam1(new ExDuelUpdateUserInfo(this._playerB));
            this.broadcastToTeam2(new ExDuelUpdateUserInfo(this._playerA));
            this._playerA.broadcastUserInfo();
            Summon summon = this._playerA.getSummon();
            if (summon != null) {
                summon.updateAbnormalEffect();
            }

            this._playerB.broadcastUserInfo();
            summon = this._playerB.getSummon();
            if (summon != null) {
                summon.updateAbnormalEffect();
            }
        }

        this.broadcastToTeam1(B04_S01);
        this.broadcastToTeam2(B04_S01);
    }

    private void savePlayerConditions() {
        if (this._isPartyDuel) {
            for (Player partyPlayer : this._playerA.getParty().getMembers()) {
                this._playerConditions.add(new PlayerCondition(partyPlayer, this._isPartyDuel));
            }

            for (Player partyPlayer : this._playerB.getParty().getMembers()) {
                this._playerConditions.add(new PlayerCondition(partyPlayer, this._isPartyDuel));
            }
        } else {
            this._playerConditions.add(new PlayerCondition(this._playerA, this._isPartyDuel));
            this._playerConditions.add(new PlayerCondition(this._playerB, this._isPartyDuel));
        }

    }

    private void restorePlayerConditions(boolean abnormalEnd) {
        if (this._isPartyDuel) {
            for (Player partyPlayer : this._playerA.getParty().getMembers()) {
                partyPlayer.setInDuel(0);
                partyPlayer.setTeam(TeamType.NONE);
                partyPlayer.broadcastUserInfo();
                Summon summon = partyPlayer.getSummon();
                if (summon != null) {
                    summon.updateAbnormalEffect();
                }
            }

            for (Player partyPlayer : this._playerB.getParty().getMembers()) {
                partyPlayer.setInDuel(0);
                partyPlayer.setTeam(TeamType.NONE);
                partyPlayer.broadcastUserInfo();
                Summon summon = partyPlayer.getSummon();
                if (summon != null) {
                    summon.updateAbnormalEffect();
                }
            }
        } else {
            this._playerA.setInDuel(0);
            this._playerA.setTeam(TeamType.NONE);
            this._playerA.broadcastUserInfo();
            Summon summon = this._playerA.getSummon();
            if (summon != null) {
                summon.updateAbnormalEffect();
            }

            this._playerB.setInDuel(0);
            this._playerB.setTeam(TeamType.NONE);
            this._playerB.broadcastUserInfo();
            summon = this._playerB.getSummon();
            if (summon != null) {
                summon.updateAbnormalEffect();
            }
        }

        if (!this._isPartyDuel && !abnormalEnd || this._isPartyDuel) {
            for (PlayerCondition cond : this._playerConditions) {
                cond.restoreCondition(abnormalEnd);
            }
        }

    }

    public int getId() {
        return this._duelId;
    }

    public int getRemainingTime() {
        return (int) (this._duelEndTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
    }

    public Player getPlayerA() {
        return this._playerA;
    }

    public Player getPlayerB() {
        return this._playerB;
    }

    public boolean isPartyDuel() {
        return this._isPartyDuel;
    }

    protected void teleportPlayers(int x, int y, int z) {
        if (this._isPartyDuel) {
            int offset = 0;

            for (Player partyPlayer : this._playerA.getParty().getMembers()) {
                partyPlayer.teleportTo(x + offset - 180, y - 150, z, 0);
                offset += 40;
            }

            offset = 0;

            for (Player partyPlayer : this._playerB.getParty().getMembers()) {
                partyPlayer.teleportTo(x + offset - 180, y + 150, z, 0);
                offset += 40;
            }

        }
    }

    public void broadcastToTeam1(L2GameServerPacket packet) {
        if (this._isPartyDuel && this._playerA.getParty() != null) {
            for (Player partyPlayer : this._playerA.getParty().getMembers()) {
                partyPlayer.sendPacket(packet);
            }
        } else {
            this._playerA.sendPacket(packet);
        }

    }

    public void broadcastToTeam2(L2GameServerPacket packet) {
        if (this._isPartyDuel && this._playerB.getParty() != null) {
            for (Player partyPlayer : this._playerB.getParty().getMembers()) {
                partyPlayer.sendPacket(packet);
            }
        } else {
            this._playerB.sendPacket(packet);
        }

    }

    protected void playAnimations() {
        if (this._playerA.isOnline()) {
            if (this._playerA.getDuelState() == Duel.DuelState.WINNER) {
                if (this._isPartyDuel && this._playerA.getParty() != null) {
                    for (Player partyPlayer : this._playerA.getParty().getMembers()) {
                        partyPlayer.broadcastPacket(new SocialAction(partyPlayer, 3));
                    }
                } else {
                    this._playerA.broadcastPacket(new SocialAction(this._playerA, 3));
                }
            } else if (this._playerA.getDuelState() == Duel.DuelState.DEAD) {
                if (this._isPartyDuel && this._playerA.getParty() != null) {
                    for (Player partyPlayer : this._playerA.getParty().getMembers()) {
                        partyPlayer.broadcastPacket(new SocialAction(partyPlayer, 7));
                    }
                } else {
                    this._playerA.broadcastPacket(new SocialAction(this._playerA, 7));
                }
            }
        }

        if (this._playerB.isOnline()) {
            if (this._playerB.getDuelState() == Duel.DuelState.WINNER) {
                if (this._isPartyDuel && this._playerB.getParty() != null) {
                    for (Player partyPlayer : this._playerB.getParty().getMembers()) {
                        partyPlayer.broadcastPacket(new SocialAction(partyPlayer, 3));
                    }
                } else {
                    this._playerB.broadcastPacket(new SocialAction(this._playerB, 3));
                }
            } else if (this._playerB.getDuelState() == Duel.DuelState.DEAD) {
                if (this._isPartyDuel && this._playerB.getParty() != null) {
                    for (Player partyPlayer : this._playerB.getParty().getMembers()) {
                        partyPlayer.broadcastPacket(new SocialAction(partyPlayer, 7));
                    }
                } else {
                    this._playerB.broadcastPacket(new SocialAction(this._playerB, 7));
                }
            }
        }

    }

    protected void endDuel(DuelResult result) {
        SystemMessage sm = null;
        switch (result.ordinal()) {
            case 3:
                sm = SystemMessage.getSystemMessage(this._isPartyDuel ? SystemMessageId.SINCE_S1_PARTY_WITHDREW_FROM_THE_DUEL_S2_PARTY_HAS_WON : SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON).addString(this._playerA.getName()).addString(this._playerB.getName());
                this.broadcastToTeam1(sm);
                this.broadcastToTeam2(sm);
            case 2:
                sm = SystemMessage.getSystemMessage(this._isPartyDuel ? SystemMessageId.S1_PARTY_HAS_WON_THE_DUEL : SystemMessageId.S1_HAS_WON_THE_DUEL).addString(this._playerB.getName());
                break;
            case 4:
                sm = SystemMessage.getSystemMessage(this._isPartyDuel ? SystemMessageId.SINCE_S1_PARTY_WITHDREW_FROM_THE_DUEL_S2_PARTY_HAS_WON : SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON).addString(this._playerB.getName()).addString(this._playerA.getName());
                this.broadcastToTeam1(sm);
                this.broadcastToTeam2(sm);
            case 1:
                sm = SystemMessage.getSystemMessage(this._isPartyDuel ? SystemMessageId.S1_PARTY_HAS_WON_THE_DUEL : SystemMessageId.S1_HAS_WON_THE_DUEL).addString(this._playerA.getName());
                break;
            case 5:
            case 6:
                sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);
        }

        this.broadcastToTeam1(sm);
        this.broadcastToTeam2(sm);
        this.restorePlayerConditions(result == Duel.DuelResult.CANCELED);
        ExDuelEnd duelEnd = new ExDuelEnd(this._isPartyDuel);
        this.broadcastToTeam1(duelEnd);
        this.broadcastToTeam2(duelEnd);
        this._playerConditions.clear();
        DuelManager.getInstance().removeDuel(this._duelId);
    }

    protected DuelResult checkEndDuelCondition() {
        if (!this._playerA.isOnline() && !this._playerB.isOnline()) {
            return Duel.DuelResult.CANCELED;
        } else if (!this._playerA.isOnline()) {
            this.onPlayerDefeat(this._playerA);
            return Duel.DuelResult.TEAM_1_SURRENDER;
        } else if (!this._playerB.isOnline()) {
            this.onPlayerDefeat(this._playerB);
            return Duel.DuelResult.TEAM_2_SURRENDER;
        } else if (this._surrenderRequest != 0) {
            return this._surrenderRequest == 1 ? Duel.DuelResult.TEAM_1_SURRENDER : Duel.DuelResult.TEAM_2_SURRENDER;
        } else if (this.getRemainingTime() <= 0) {
            return Duel.DuelResult.TIMEOUT;
        } else if (this._playerA.getDuelState() == Duel.DuelState.WINNER) {
            return Duel.DuelResult.TEAM_1_WIN;
        } else if (this._playerB.getDuelState() == Duel.DuelState.WINNER) {
            return Duel.DuelResult.TEAM_2_WIN;
        } else {
            if (!this._isPartyDuel) {
                if (this._playerA.getDuelState() == Duel.DuelState.INTERRUPTED || this._playerB.getDuelState() == Duel.DuelState.INTERRUPTED) {
                    return Duel.DuelResult.CANCELED;
                }

                if (!this._playerA.isInsideRadius(this._playerB, 2000, false, false)) {
                    return Duel.DuelResult.CANCELED;
                }

                if (this._playerA.getPvpFlag() != 0 || this._playerB.getPvpFlag() != 0) {
                    return Duel.DuelResult.CANCELED;
                }

                if (this._playerA.isInsideZone(ZoneId.PEACE) || this._playerB.isInsideZone(ZoneId.PEACE) || this._playerA.isInsideZone(ZoneId.SIEGE) || this._playerB.isInsideZone(ZoneId.SIEGE) || this._playerA.isInsideZone(ZoneId.PVP) || this._playerB.isInsideZone(ZoneId.PVP)) {
                    return Duel.DuelResult.CANCELED;
                }
            } else {
                if (this._playerA.getParty() != null) {
                    for (Player partyMember : this._playerA.getParty().getMembers()) {
                        if (partyMember.getDuelState() == Duel.DuelState.INTERRUPTED) {
                            return Duel.DuelResult.CANCELED;
                        }

                        if (!partyMember.isInsideRadius(this._playerB, 2000, false, false)) {
                            return Duel.DuelResult.CANCELED;
                        }

                        if (partyMember.getPvpFlag() != 0) {
                            return Duel.DuelResult.CANCELED;
                        }

                        if (partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(ZoneId.SIEGE)) {
                            return Duel.DuelResult.CANCELED;
                        }
                    }
                }

                if (this._playerB.getParty() != null) {
                    for (Player partyMember : this._playerB.getParty().getMembers()) {
                        if (partyMember.getDuelState() == Duel.DuelState.INTERRUPTED) {
                            return Duel.DuelResult.CANCELED;
                        }

                        if (!partyMember.isInsideRadius(this._playerA, 2000, false, false)) {
                            return Duel.DuelResult.CANCELED;
                        }

                        if (partyMember.getPvpFlag() != 0) {
                            return Duel.DuelResult.CANCELED;
                        }

                        if (partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(ZoneId.SIEGE)) {
                            return Duel.DuelResult.CANCELED;
                        }
                    }
                }
            }

            return Duel.DuelResult.CONTINUE;
        }
    }

    public void doSurrender(Player player) {
        if (this._surrenderRequest == 0) {
            if (this._isPartyDuel) {
                if (this._playerA.getParty().containsPlayer(player)) {
                    this._surrenderRequest = 1;

                    for (Player partyPlayer : this._playerA.getParty().getMembers()) {
                        partyPlayer.setDuelState(Duel.DuelState.DEAD);
                    }

                    for (Player partyPlayer : this._playerB.getParty().getMembers()) {
                        partyPlayer.setDuelState(Duel.DuelState.WINNER);
                    }
                } else if (this._playerB.getParty().containsPlayer(player)) {
                    this._surrenderRequest = 2;

                    for (Player partyPlayer : this._playerB.getParty().getMembers()) {
                        partyPlayer.setDuelState(Duel.DuelState.DEAD);
                    }

                    for (Player partyPlayer : this._playerA.getParty().getMembers()) {
                        partyPlayer.setDuelState(Duel.DuelState.WINNER);
                    }
                }
            } else if (player == this._playerA) {
                this._surrenderRequest = 1;
                this._playerA.setDuelState(Duel.DuelState.DEAD);
                this._playerB.setDuelState(Duel.DuelState.WINNER);
            } else if (player == this._playerB) {
                this._surrenderRequest = 2;
                this._playerB.setDuelState(Duel.DuelState.DEAD);
                this._playerA.setDuelState(Duel.DuelState.WINNER);
            }

        }
    }

    public void onPlayerDefeat(Player player) {
        player.setDuelState(Duel.DuelState.DEAD);
        if (this._isPartyDuel) {
            boolean teamDefeated = true;

            for (Player partyPlayer : player.getParty().getMembers()) {
                if (partyPlayer.getDuelState() == Duel.DuelState.DUELLING) {
                    teamDefeated = false;
                    break;
                }
            }

            if (teamDefeated) {
                Player winner = this._playerA;
                if (this._playerA.getParty().containsPlayer(player)) {
                    winner = this._playerB;
                }

                for (Player partyPlayer : winner.getParty().getMembers()) {
                    partyPlayer.setDuelState(Duel.DuelState.WINNER);
                }
            }
        } else if (this._playerA == player) {
            this._playerB.setDuelState(Duel.DuelState.WINNER);
        } else {
            this._playerA.setDuelState(Duel.DuelState.WINNER);
        }

    }

    public void onPartyEdit() {
        if (this._isPartyDuel) {
            for (PlayerCondition cond : this._playerConditions) {
                cond.teleportBack();
                cond.getPlayer().setInDuel(0);
            }

            this.endDuel(Duel.DuelResult.CANCELED);
        }
    }

    public void onBuff(Player player, L2Effect effect) {
        for (PlayerCondition cond : this._playerConditions) {
            if (cond.getPlayer() == player) {
                cond.registerDebuff(effect);
                return;
            }
        }

    }

    public static enum DuelState {
        NO_DUEL,
        ON_COUNTDOWN,
        DUELLING,
        DEAD,
        WINNER,
        INTERRUPTED;
    }

    private static enum DuelResult {
        CONTINUE,
        TEAM_1_WIN,
        TEAM_2_WIN,
        TEAM_1_SURRENDER,
        TEAM_2_SURRENDER,
        CANCELED,
        TIMEOUT;
    }

    private static class PlayerCondition {
        private Player _player;
        private double _hp;
        private double _mp;
        private double _cp;
        private int _x;
        private int _y;
        private int _z;
        private List<L2Effect> _debuffs;

        public PlayerCondition(Player player, boolean partyDuel) {
            if (player != null) {
                this._player = player;
                this._hp = this._player.getCurrentHp();
                this._mp = this._player.getCurrentMp();
                this._cp = this._player.getCurrentCp();
                if (partyDuel) {
                    this._x = this._player.getX();
                    this._y = this._player.getY();
                    this._z = this._player.getZ();
                }

            }
        }

        public void restoreCondition(boolean abnormalEnd) {
            this.teleportBack();
            if (!abnormalEnd) {
                this._player.setCurrentHp(this._hp);
                this._player.setCurrentMp(this._mp);
                this._player.setCurrentCp(this._cp);
                if (this._debuffs != null) {
                    for (L2Effect skill : this._debuffs) {
                        if (skill != null) {
                            skill.exit();
                        }
                    }
                }

            }
        }

        public void registerDebuff(L2Effect debuff) {
            if (this._debuffs == null) {
                this._debuffs = new CopyOnWriteArrayList<>();
            }

            this._debuffs.add(debuff);
        }

        public void teleportBack() {
            if (this._x != 0 && this._y != 0) {
                this._player.teleportTo(this._x, this._y, this._z, 0);
            }

        }

        public Player getPlayer() {
            return this._player;
        }
    }

    private class StartTask implements Runnable {
        public StartTask() {
        }

        public void run() {
            if (Duel.this._countdown < 0) {
                Duel.this._startTask.cancel(true);
                Duel.this._startTask = null;
            }
            SystemMessage sm;
            switch (Duel.this._countdown) {
                case 0:
                    sm = SystemMessage.getSystemMessage(SystemMessageId.LET_THE_DUEL_BEGIN);
                    Duel.this.broadcastToTeam1(sm);
                    Duel.this.broadcastToTeam2(sm);
                    Duel.this.startDuel();
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 10:
                case 15:
                case 20:
                case 30:
                    sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addNumber(Duel.this._countdown);
                    Duel.this.broadcastToTeam1(sm);
                    Duel.this.broadcastToTeam2(sm);
                    break;
                case 33:
                    Duel.this.teleportPlayers(-83760, -238825, -3331);
                case 6:
                case 7:
                case 8:
                case 9:
                case 11:
                case 12:
                case 13:
                case 14:
                case 16:
                case 17:
                case 18:
                case 19:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 29:
                case 31:
                case 32:
                default:
                    break;
            }

            --Duel.this._countdown;
        }
    }

    private class CheckTask implements Runnable {
        public CheckTask() {
        }

        public void run() {
            DuelResult status = Duel.this.checkEndDuelCondition();
            if (status != Duel.DuelResult.CONTINUE) {
                if (Duel.this._startTask != null) {
                    Duel.this._startTask.cancel(true);
                    Duel.this._startTask = null;
                }

                if (Duel.this._checkTask != null) {
                    Duel.this._checkTask.cancel(false);
                    Duel.this._checkTask = null;
                }

                Duel.this.stopFighting();
                if (status != Duel.DuelResult.CANCELED) {
                    Duel.this.playAnimations();
                }

                Duel.this.endDuel(status);
            }

        }
    }
}
