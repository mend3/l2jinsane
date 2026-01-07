package net.sf.l2j.gameserver.model.clanhall;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.SiegeStatus;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.zone.type.SiegeZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;

public final class SiegableHall extends ClanHall {
    private static final String UPDATE_CLANHALL = "UPDATE clanhall SET ownerId=?, endDate=? WHERE id=?";

    private final long _siegeLength;
    private final int[] _scheduleConfig;

    private Calendar _nextSiege;

    private SiegeStatus _status = SiegeStatus.REGISTRATION_OPENED;
    private final SiegeZone _siegeZone;

    private Siege _siege;

    public SiegableHall(StatSet set) {
        super(set);

        _siegeLength = set.getLong("siegeLength");
        _scheduleConfig = set.getIntegerArray("scheduleConfig");

        _siegeZone = ZoneManager.getInstance().getAllZones(SiegeZone.class).stream().filter(z -> z.getId() == getId()).findFirst().orElse(null);
    }

    @Override
    public void updateDb() {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_CLANHALL)) {
            ps.setInt(1, getOwnerId());
            ps.setLong(2, getNextSiegeTime());
            ps.setInt(3, getId());
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Siege getSiege() {
        return _siege;
    }

    public void setSiege(final Siege siegable) {
        _siege = siegable;
    }

    public Calendar getSiegeDate() {
        return _nextSiege;
    }

    public long getSiegeLength() {
        return _siegeLength;
    }

    public long getNextSiegeTime() {
        return _nextSiege.getTimeInMillis();
    }

    public void setNextSiegeDate(long date) {
        _nextSiege.setTimeInMillis(date);
    }

    public void setNextSiegeDate(final Calendar c) {
        _nextSiege = c;
    }

    public void updateNextSiege() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, _scheduleConfig[0]);
        c.add(Calendar.MONTH, _scheduleConfig[1]);
        c.add(Calendar.YEAR, _scheduleConfig[2]);
        c.set(Calendar.HOUR_OF_DAY, _scheduleConfig[3]);
        c.set(Calendar.MINUTE, _scheduleConfig[4]);
        c.set(Calendar.SECOND, 0);

        setNextSiegeDate(c);

        updateDb();
    }

    public void addAttacker(final Clan clan) {
        if (getSiege() != null)
            getSiege().getAttackerClans().add(clan);
    }

    public void removeAttacker(final Clan clan) {
        if (getSiege() != null)
            getSiege().getAttackerClans().remove(clan);
    }

    public boolean isRegistered(Clan clan) {
        return getSiege() != null && getSiege().getAttackerClans().contains(clan);
    }

    public SiegeStatus getSiegeStatus() {
        return _status;
    }

    public boolean isRegistering() {
        return _status == SiegeStatus.REGISTRATION_OPENED;
    }

    public boolean isInSiege() {
        return _status == SiegeStatus.IN_PROGRESS;
    }

    public boolean isWaitingBattle() {
        return _status == SiegeStatus.REGISTRATION_OVER;
    }

    public void updateSiegeStatus(SiegeStatus status) {
        _status = status;
    }

    public SiegeZone getSiegeZone() {
        return _siegeZone;
    }

    public void spawnDoor() {
        spawnDoor(false);
    }

    /**
     * Respawn doors associated to that clan hall.
     *
     * @param isDoorWeak if true, spawn doors with 50% max HPs.
     */
    public void spawnDoor(boolean isDoorWeak) {
        for (Door door : getDoors()) {
            if (door.isDead())
                door.doRevive();

            door.closeMe();
        }
    }

    /**
     * Test the {@link Clan} and {@link Player} set as parameters to register into this {@link SiegableHall} siege.
     *
     * @param clan   : The {@link Clan} to test.
     * @param player : The {@link Player} to test.
     */
    public void registerClan(Clan clan, Player player) {
        if (clan == null || clan.getLevel() < Config.MINIMUM_CLAN_LEVEL)
            player.sendPacket(SystemMessageId.ONLY_CLAN_LEVEL_4_ABOVE_MAY_SIEGE);
        else if (isWaitingBattle())
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED).addFortId(getId()));
        else if (isInSiege())
            player.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
        else if (getOwnerId() == clan.getClanId())
            player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
        else if (clan.getCastleId() != 0 || clan.getClanHallId() != 0)
            player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
        else if (getSiege().getAttackerClans().contains(clan))
            player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
        else if (ClanHallManager.getInstance().isClanParticipating(clan))
            player.sendPacket(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
        else if (getSiege().getAttackerClans().size() >= Config.MAX_ATTACKERS_NUMBER)
            player.sendPacket(SystemMessageId.ATTACKER_SIDE_FULL);
        else
            addAttacker(clan);
    }

    /**
     * Unregister the given {@link Clan} set as parameter.
     *
     * @param clan : The {@link Clan} to test.
     */
    public void unregisterClan(Clan clan) {
        if (!isRegistering())
            return;

        removeAttacker(clan);
    }
}