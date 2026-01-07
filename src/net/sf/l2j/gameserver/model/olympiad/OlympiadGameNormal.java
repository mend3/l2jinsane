package net.sf.l2j.gameserver.model.olympiad;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.OlympiadType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.type.OlympiadStadiumZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadUserInfo;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public abstract class OlympiadGameNormal extends AbstractOlympiadGame {
    private static final String INSERT_RESULT = "INSERT INTO olympiad_fights (charOneId, charTwoId, charOneClass, charTwoClass, winner, start, time, classed) values(?,?,?,?,?,?,?,?)";

    protected int _damageP1 = 0;

    protected int _damageP2 = 0;

    protected Participant _playerOne;

    protected Participant _playerTwo;

    protected OlympiadGameNormal(int id, Participant[] opponents) {
        super(id);
        this._playerOne = opponents[0];
        this._playerTwo = opponents[1];
        this._playerOne.getPlayer().setOlympiadGameId(id);
        this._playerTwo.getPlayer().setOlympiadGameId(id);
    }

    protected static Participant[] createListOfParticipants(List<Integer> list) {
        if (list == null || list.isEmpty() || list.size() < 2)
            return null;
        int playerOneObjectId = 0;
        Player playerOne = null;
        Player playerTwo = null;
        while (list.size() > 1) {
            playerOneObjectId = list.remove(Rnd.get(list.size()));
            playerOne = World.getInstance().getPlayer(playerOneObjectId);
            if (playerOne == null || !playerOne.isOnline())
                continue;
            playerTwo = World.getInstance().getPlayer(list.remove(Rnd.get(list.size())));
            if (playerTwo == null || !playerTwo.isOnline()) {
                list.add(Integer.valueOf(playerOneObjectId));
                continue;
            }
            Participant[] result = new Participant[2];
            result[0] = new Participant(playerOne, 1);
            result[1] = new Participant(playerTwo, 2);
            return result;
        }
        return null;
    }

    protected static void saveResults(Participant one, Participant two, int _winner, long _startTime, long _fightTime, OlympiadType type) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO olympiad_fights (charOneId, charTwoId, charOneClass, charTwoClass, winner, start, time, classed) values(?,?,?,?,?,?,?,?)");
                try {
                    ps.setInt(1, one.getObjectId());
                    ps.setInt(2, two.getObjectId());
                    ps.setInt(3, one.getBaseClass());
                    ps.setInt(4, two.getBaseClass());
                    ps.setInt(5, _winner);
                    ps.setLong(6, _startTime);
                    ps.setLong(7, _fightTime);
                    ps.setInt(8, (type == OlympiadType.CLASSED) ? 1 : 0);
                    ps.execute();
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't save Olympiad results.", e);
        }
    }

    public final boolean containsParticipant(int objectId) {
        return (this._playerOne.getObjectId() == objectId || this._playerTwo.getObjectId() == objectId);
    }

    public final void sendOlympiadInfo(Creature player) {
        player.sendPacket(new ExOlympiadUserInfo(this._playerOne.getPlayer()));
        this._playerOne.getPlayer().updateEffectIcons();
        player.sendPacket(new ExOlympiadUserInfo(this._playerTwo.getPlayer()));
        this._playerTwo.getPlayer().updateEffectIcons();
    }

    public final void broadcastOlympiadInfo(OlympiadStadiumZone stadium) {
        stadium.broadcastPacket(new ExOlympiadUserInfo(this._playerOne.getPlayer()));
        this._playerOne.getPlayer().updateEffectIcons();
        stadium.broadcastPacket(new ExOlympiadUserInfo(this._playerTwo.getPlayer()));
        this._playerTwo.getPlayer().updateEffectIcons();
    }

    protected final void broadcastPacket(L2GameServerPacket packet) {
        this._playerOne.updatePlayer();
        if (this._playerOne.getPlayer() != null)
            this._playerOne.getPlayer().sendPacket(packet);
        this._playerTwo.updatePlayer();
        if (this._playerTwo.getPlayer() != null)
            this._playerTwo.getPlayer().sendPacket(packet);
    }

    protected final boolean portPlayersToArena(List<Location> spawns) {
        boolean result = true;
        result &= portPlayerToArena(this._playerOne, spawns.get(0), this._stadiumId);
        result &= portPlayerToArena(this._playerTwo, spawns.get(1), this._stadiumId);
        return result;
    }

    protected final void removals() {
        if (this._aborted)
            return;
        removals(this._playerOne.getPlayer(), true);
        removals(this._playerTwo.getPlayer(), true);
    }

    protected final void buffPlayers() {
        if (this._aborted)
            return;
        buffPlayer(this._playerOne.getPlayer());
        buffPlayer(this._playerTwo.getPlayer());
    }

    protected final void healPlayers() {
        if (this._aborted)
            return;
        healPlayer(this._playerOne.getPlayer());
        healPlayer(this._playerTwo.getPlayer());
    }

    protected final boolean makeCompetitionStart() {
        if (!super.makeCompetitionStart())
            return false;
        if (this._playerOne.getPlayer() == null || this._playerTwo.getPlayer() == null)
            return false;
        this._playerOne.getPlayer().setOlympiadStart(true);
        this._playerTwo.getPlayer().setOlympiadStart(true);
        return true;
    }

    protected final void cleanEffects() {
        if (this._playerOne.getPlayer() != null && !this._playerOne.isDefecting() && !this._playerOne.isDisconnected() && this._playerOne.getPlayer().getOlympiadGameId() == this._stadiumId)
            cleanEffects(this._playerOne.getPlayer());
        if (this._playerTwo.getPlayer() != null && !this._playerTwo.isDefecting() && !this._playerTwo.isDisconnected() && this._playerTwo.getPlayer().getOlympiadGameId() == this._stadiumId)
            cleanEffects(this._playerTwo.getPlayer());
    }

    protected final void portPlayersBack() {
        if (this._playerOne.getPlayer() != null && !this._playerOne.isDefecting() && !this._playerOne.isDisconnected())
            portPlayerBack(this._playerOne.getPlayer());
        if (this._playerTwo.getPlayer() != null && !this._playerTwo.isDefecting() && !this._playerTwo.isDisconnected())
            portPlayerBack(this._playerTwo.getPlayer());
    }

    protected final void playersStatusBack() {
        if (this._playerOne.getPlayer() != null && !this._playerOne.isDefecting() && !this._playerOne.isDisconnected() && this._playerOne.getPlayer().getOlympiadGameId() == this._stadiumId)
            playerStatusBack(this._playerOne.getPlayer());
        if (this._playerTwo.getPlayer() != null && !this._playerTwo.isDefecting() && !this._playerTwo.isDisconnected() && this._playerTwo.getPlayer().getOlympiadGameId() == this._stadiumId)
            playerStatusBack(this._playerTwo.getPlayer());
    }

    protected final void clearPlayers() {
        this._playerOne.setPlayer(null);
        this._playerOne = null;
        this._playerTwo.setPlayer(null);
        this._playerTwo = null;
    }

    protected final void handleDisconnect(Player player) {
        if (player.getObjectId() == this._playerOne.getObjectId()) {
            this._playerOne.setDisconnection(true);
        } else if (player.getObjectId() == this._playerTwo.getObjectId()) {
            this._playerTwo.setDisconnection(true);
        }
    }

    protected final boolean checkBattleStatus() {
        if (this._aborted)
            return false;
        if (this._playerOne.getPlayer() == null || this._playerOne.isDisconnected())
            return false;
        return this._playerTwo.getPlayer() != null && !this._playerTwo.isDisconnected();
    }

    protected final boolean haveWinner() {
        if (!checkBattleStatus())
            return true;
        boolean playerOneLost = true;
        try {
            if (this._playerOne.getPlayer().getOlympiadGameId() == this._stadiumId)
                playerOneLost = this._playerOne.getPlayer().isDead();
        } catch (Exception e) {
            playerOneLost = true;
        }
        boolean playerTwoLost = true;
        try {
            if (this._playerTwo.getPlayer().getOlympiadGameId() == this._stadiumId)
                playerTwoLost = this._playerTwo.getPlayer().isDead();
        } catch (Exception e) {
            playerTwoLost = true;
        }
        return (playerOneLost || playerTwoLost);
    }

    protected void validateWinner(OlympiadStadiumZone stadium) {
        if (this._aborted)
            return;
        boolean _pOneCrash = (this._playerOne.getPlayer() == null || this._playerOne.isDisconnected());
        boolean _pTwoCrash = (this._playerTwo.getPlayer() == null || this._playerTwo.isDisconnected());
        int playerOnePoints = this._playerOne.getStats().getInteger("olympiad_points");
        int playerTwoPoints = this._playerTwo.getStats().getInteger("olympiad_points");
        int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / getDivider();
        if (pointDiff <= 0) {
            pointDiff = 1;
        } else if (pointDiff > Config.ALT_OLY_MAX_POINTS) {
            pointDiff = Config.ALT_OLY_MAX_POINTS;
        }
        if (this._playerOne.isDefecting() || this._playerTwo.isDefecting()) {
            if (this._playerOne.isDefecting()) {
                int points = Math.min(playerOnePoints / 3, Config.ALT_OLY_MAX_POINTS);
                removePointsFromParticipant(this._playerOne, points);
            }
            if (this._playerTwo.isDefecting()) {
                int points = Math.min(playerTwoPoints / 3, Config.ALT_OLY_MAX_POINTS);
                removePointsFromParticipant(this._playerTwo, points);
            }
            return;
        }
        if (_pOneCrash || _pTwoCrash) {
            if (_pTwoCrash && !_pOneCrash) {
                stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(this._playerOne.getName()));
                this._playerOne.updateStat("competitions_won", 1);
                addPointsToParticipant(this._playerOne, pointDiff);
                this._playerTwo.updateStat("competitions_lost", 1);
                removePointsFromParticipant(this._playerTwo, pointDiff);
                rewardParticipant(this._playerOne.getPlayer(), getReward());
            } else if (_pOneCrash && !_pTwoCrash) {
                stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(this._playerTwo.getName()));
                this._playerTwo.updateStat("competitions_won", 1);
                addPointsToParticipant(this._playerTwo, pointDiff);
                this._playerOne.updateStat("competitions_lost", 1);
                removePointsFromParticipant(this._playerOne, pointDiff);
                rewardParticipant(this._playerTwo.getPlayer(), getReward());
            } else if (_pOneCrash && _pTwoCrash) {
                stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));
                this._playerOne.updateStat("competitions_lost", 1);
                removePointsFromParticipant(this._playerOne, pointDiff);
                this._playerTwo.updateStat("competitions_lost", 1);
                removePointsFromParticipant(this._playerTwo, pointDiff);
            }
            this._playerOne.updateStat("competitions_done", 1);
            this._playerTwo.updateStat("competitions_done", 1);
            return;
        }
        long _fightTime = System.currentTimeMillis() - this._startTime;
        double playerOneHp = 0.0D;
        if (this._playerOne.getPlayer() != null && !this._playerOne.getPlayer().isDead()) {
            playerOneHp = this._playerOne.getPlayer().getCurrentHp() + this._playerOne.getPlayer().getCurrentCp();
            if (playerOneHp < 0.5D)
                playerOneHp = 0.0D;
        }
        double playerTwoHp = 0.0D;
        if (this._playerTwo.getPlayer() != null && !this._playerTwo.getPlayer().isDead()) {
            playerTwoHp = this._playerTwo.getPlayer().getCurrentHp() + this._playerTwo.getPlayer().getCurrentCp();
            if (playerTwoHp < 0.5D)
                playerTwoHp = 0.0D;
        }
        this._playerOne.updatePlayer();
        this._playerTwo.updatePlayer();
        if ((this._playerOne.getPlayer() == null || !this._playerOne.getPlayer().isOnline()) && (this._playerTwo.getPlayer() == null || !this._playerTwo.getPlayer().isOnline())) {
            this._playerOne.updateStat("competitions_drawn", 1);
            this._playerTwo.updateStat("competitions_drawn", 1);
            stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));
        } else if (this._playerTwo.getPlayer() == null || !this._playerTwo.getPlayer().isOnline() || (playerTwoHp == 0.0D && playerOneHp != 0.0D) || (this._damageP1 > this._damageP2 && playerTwoHp != 0.0D && playerOneHp != 0.0D)) {
            stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(this._playerOne.getName()));
            this._playerOne.updateStat("competitions_won", 1);
            this._playerTwo.updateStat("competitions_lost", 1);
            addPointsToParticipant(this._playerOne, pointDiff);
            removePointsFromParticipant(this._playerTwo, pointDiff);
            saveResults(this._playerOne, this._playerTwo, 1, this._startTime, _fightTime, getType());
            rewardParticipant(this._playerOne.getPlayer(), getReward());
        } else if (this._playerOne.getPlayer() == null || !this._playerOne.getPlayer().isOnline() || (playerOneHp == 0.0D && playerTwoHp != 0.0D) || (this._damageP2 > this._damageP1 && playerOneHp != 0.0D && playerTwoHp != 0.0D)) {
            stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(this._playerTwo.getName()));
            this._playerTwo.updateStat("competitions_won", 1);
            this._playerOne.updateStat("competitions_lost", 1);
            addPointsToParticipant(this._playerTwo, pointDiff);
            removePointsFromParticipant(this._playerOne, pointDiff);
            saveResults(this._playerOne, this._playerTwo, 2, this._startTime, _fightTime, getType());
            rewardParticipant(this._playerTwo.getPlayer(), getReward());
        } else {
            saveResults(this._playerOne, this._playerTwo, 0, this._startTime, _fightTime, getType());
            stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));
            removePointsFromParticipant(this._playerOne, Math.min(playerOnePoints / getDivider(), Config.ALT_OLY_MAX_POINTS));
            removePointsFromParticipant(this._playerTwo, Math.min(playerTwoPoints / getDivider(), Config.ALT_OLY_MAX_POINTS));
        }
        this._playerOne.updateStat("competitions_done", 1);
        this._playerTwo.updateStat("competitions_done", 1);
    }

    protected final void addDamage(Player player, int damage) {
        if (this._playerOne.getPlayer() == null || this._playerTwo.getPlayer() == null)
            return;
        if (player == this._playerOne.getPlayer()) {
            this._damageP1 += damage;
        } else if (player == this._playerTwo.getPlayer()) {
            this._damageP2 += damage;
        }
    }

    public final String[] getPlayerNames() {
        return new String[]{this._playerOne

                .getName(), this._playerTwo
                .getName()};
    }

    public boolean checkDefection() {
        this._playerOne.updatePlayer();
        this._playerTwo.updatePlayer();
        SystemMessage reason = checkDefection(this._playerOne.getPlayer());
        if (reason != null) {
            this._playerOne.setDefection(true);
            if (this._playerTwo.getPlayer() != null)
                this._playerTwo.getPlayer().sendPacket(reason);
        }
        reason = checkDefection(this._playerTwo.getPlayer());
        if (reason != null) {
            this._playerTwo.setDefection(true);
            if (this._playerOne.getPlayer() != null)
                this._playerOne.getPlayer().sendPacket(reason);
        }
        return (this._playerOne.isDefecting() || this._playerTwo.isDefecting());
    }

    public final void resetDamage() {
        this._damageP1 = 0;
        this._damageP2 = 0;
    }
}
