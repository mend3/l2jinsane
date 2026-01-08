package enginemods.main.util;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.enums.actors.Sex;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.SessionKey;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class UtilPlayer {
    private static final String CREATE_ACCOUNTS = "INSERT INTO accounts (login, password, lastactive, access_level) values (?, ?, ?, ?)";

    private static final String UPDATE_CHARACTER = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=? WHERE obj_id=?";

    private static final ReentrantLock _locker = new ReentrantLock();

    public static Player createPlayer(Player admin, String playerName, String accountName, PlayerTemplate template, int lvl, Sex sex, byte hairStyle, byte face, List<Integer> items, int offSet) {
        _locker.lock();
        int objectId = IdFactory.getInstance().getNextId();
        Player newChar = null;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO accounts (login, password, lastactive, access_level) values (?, ?, ?, ?)");
                try {
                    ps.setString(1, accountName);
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    byte[] raw = "123456Abc!".getBytes(StandardCharsets.UTF_8);
                    String hashBase64 = Base64.getEncoder().encodeToString(md.digest(raw));
                    ps.setString(2, hashBase64);
                    ps.setLong(3, System.currentTimeMillis());
                    ps.setInt(4, 0);
                    ps.execute();
                    byte hairColor = 0;
                    newChar = Player.create(objectId, template, accountName, playerName, hairStyle, hairColor, face, sex);
                    World.getInstance().addObject(newChar);
                    newChar.setXYZInvisible(admin.getX() + Rnd.get(-offSet, offSet), admin.getY() + Rnd.get(-offSet, offSet), admin.getZ());
                    newChar.getPosition().setHeading(Rnd.get(65536));
                    long oldExp = newChar.getStat().getExp();
                    long newExp = newChar.getStat().getExpForLevel(lvl);
                    newChar.getStat().addExp(newExp - oldExp);
                    newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
                    for (int itemId : items) {
                        if (itemId == 0)
                            continue;
                        ItemInstance item = newChar.getInventory().addItem("Init", itemId, 1, newChar, null);
                        if (item.isEquipable())
                            if (newChar.getActiveWeaponItem() == null || item.getItem().getType2() == 0)
                                newChar.getInventory().equipItemAndRecord(item);
                    }
                    newChar.rewardSkills();
                    newChar.deleteMe();
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
            e.printStackTrace();
            return null;
        } finally {
            _locker.unlock();
        }
        return newChar;
    }

    public static Player spawnPlayer(int objectId) {
        _locker.lock();
        Player player = null;
        try {
            GameClient client = new GameClient(null);
            player = Player.restore(objectId);
            player.setOnlineStatus(true, true);
            player.setClient(client);
            client.setPlayer(player);
            client.setState(GameClient.GameClientState.IN_GAME);
            client.setAccountName(player.getAccountNamePlayer());
            client.setDetached(true);
            client.setSessionId(new SessionKey(0, 0, 0, 0));
            LoginServerThread.getInstance().addClient(player.getAccountName(), client);
            World.getInstance().addPlayer(player);
            player.spawnMe(player.getX(), player.getY(), player.getZ());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            _locker.unlock();
        }
        return player;
    }

    public static void storeCharBase(Player player) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                long exp = player.getStat().getExp();
                int level = player.getStat().getLevel();
                int sp = player.getStat().getSp();
                PreparedStatement statement = con.prepareStatement("UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=? WHERE obj_id=?");
                statement.setInt(1, level);
                statement.setInt(2, player.getMaxHp());
                statement.setDouble(3, player.getCurrentHp());
                statement.setInt(4, player.getMaxCp());
                statement.setDouble(5, player.getCurrentCp());
                statement.setInt(6, player.getMaxMp());
                statement.setDouble(7, player.getCurrentMp());
                statement.setInt(8, player.getAppearance().getFace());
                statement.setInt(9, player.getAppearance().getHairStyle());
                statement.setInt(10, player.getAppearance().getHairColor());
                statement.setInt(11, player.getAppearance().getSex().ordinal());
                statement.setInt(12, player.getHeading());
                statement.setInt(13, player.getX());
                statement.setInt(14, player.getY());
                statement.setInt(15, player.getZ());
                statement.setLong(16, exp);
                statement.setLong(17, player.getExpBeforeDeath());
                statement.setInt(18, sp);
                statement.setInt(19, player.getKarma());
                statement.setInt(20, player.getPvpKills());
                statement.setInt(21, player.getPkKills());
                statement.setInt(22, player.getClanId());
                statement.setInt(23, player.getRace().ordinal());
                statement.setInt(24, player.getClassId().getId());
                statement.setLong(25, player.getDeleteTimer());
                statement.setString(26, player.getTitle());
                statement.setInt(27, player.getAccessLevel().getLevel());
                statement.setInt(28, player.isOnlineInt());
                statement.setInt(29, player.isIn7sDungeon() ? 1 : 0);
                statement.setInt(30, player.getClanPrivileges());
                statement.setInt(31, player.wantsPeace() ? 1 : 0);
                statement.setInt(32, player.getBaseClass());
                statement.setLong(33, player.getOnlineTime());
                statement.setInt(34, player.getPunishment().getType().ordinal());
                statement.setLong(35, player.getPunishment().getTimer());
                statement.setInt(36, player.isNoble() ? 1 : 0);
                statement.setLong(37, player.getPowerGrade());
                statement.setInt(38, player.getPledgeType());
                statement.setInt(39, player.getLvlJoinedAcademy());
                statement.setLong(40, player.getApprentice());
                statement.setLong(41, player.getSponsor());
                statement.setInt(42, player.getAllianceWithVarkaKetra());
                statement.setLong(43, player.getClanJoinExpiryTime());
                statement.setLong(44, player.getClanCreateExpiryTime());
                statement.setString(45, player.getName());
                statement.setLong(46, player.getDeathPenaltyBuffLevel());
                statement.setInt(47, player.getObjectId());
                statement.execute();
                statement.close();
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
            e.printStackTrace();
        }
    }
}
