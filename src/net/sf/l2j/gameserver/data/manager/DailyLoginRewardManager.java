package net.sf.l2j.gameserver.data.manager;

import enginemods.main.holders.RewardHolder;
import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DailyLoginRewardManager {
    private static final CLogger LOGGER = new CLogger(DailyLoginRewardManager.class.getName());

    private DailyLoginRewardManager() {
        loadSystemThread();
    }

    protected static void loadSystemThread() {
        long spawnMillis = 0L;
        Calendar c = Calendar.getInstance();
        String[] time = "24:00".split(":");
        c.set(Calendar.DATE, c.get(Calendar.DATE) + 1);
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        c.set(Calendar.SECOND, 0);
        spawnMillis = c.getTimeInMillis() - System.currentTimeMillis();
        ThreadPool.schedule(() -> {
            DailyLoginRewardManager.clearDBTable();
            DailyLoginRewardManager.loadSystemThread();
            LOGGER.info("[Daily Reward] Table cleaned and restart the thread.");
        }, spawnMillis);
    }

    protected static void clearDBTable() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("DELETE FROM reward_manager");
                try {
                    statement.execute();
                    statement.close();
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
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
        }
    }

    public static void claimDailyReward(Player player) {
        if (checkIfIPorHWIDExistInDB(player, "hwid")) {
            if (checkForLatestHWIDReward(player, "hwid")) {
                if (checkForLatestHWIDReward(player, "hwid")) {
                    CreatureSay cs = new CreatureSay(player.getObjectId(), 4, "SVR", "Join again in " + Cd(player, "hwid", false) + " to get your daily reward.");
                    player.sendPacket(cs);
                    player.sendPacket(new ExShowScreenMessage("Join again in " + Cd(player, "hwid", false) + " to get your daily reward.", 3000, 2, true));
                }
            } else {
                if (checkIfIPorHWIDExistInDB(player, "hwid"))
                    updateLastReward(player, "hwid");
                giveReward(player);
            }
        } else {
            insertNewParentOfPlayerIPHWID(player);
            giveReward(player);
        }
    }

    private static void giveReward(Player player) {
        player.sendMessage("You have received your daily reward, thanks for play here!");
        for (RewardHolder reward : Config.DAILY_LOG_REWARDS) {
            if (Rnd.get(100) <= reward.getRewardChance())
                player.addItem("Random Reward", reward.getRewardId(), Rnd.get(reward.getMin(), reward.getMax()), player, true);
        }
        player.sendPacket(new ExShowScreenMessage("You have received your daily reward, thanks for play here!", 3000, 2, true));
        MagicSkillUse MSU = new MagicSkillUse(player, player, 2024, 1, 1, 0);
        player.broadcastPacket(MSU);
    }

    private static boolean checkForLatestHWIDReward(Player activeChar, String mode) {
        return (Long.parseLong(Cd(activeChar, mode, true)) > System.currentTimeMillis());
    }

    private static void updateLastReward(Player player, String mode) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("UPDATE reward_manager SET expire_time=? WHERE " + mode + "=?");
                try {
                    statement.setLong(1, System.currentTimeMillis());
                    statement.setString(2, player.getHWID());
                    statement.execute();
                    statement.close();
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
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
        }
    }

    private static String Cd(Player player, String mode, boolean returnInTimestamp) {
        long CdMs = 0L;
        long voteDelay = 86400000L;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("SELECT expire_time FROM reward_manager WHERE " + mode + "=?");
                try {
                    statement.setString(1, player.getHWID());
                    ResultSet rset = statement.executeQuery();
                    try {
                        while (rset.next())
                            CdMs = rset.getLong("expire_time");
                        if (rset != null)
                            rset.close();
                    } catch (Throwable throwable) {
                        if (rset != null)
                            try {
                                rset.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (CdMs + voteDelay < System.currentTimeMillis())
                        CdMs = System.currentTimeMillis() - voteDelay;
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
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
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        if (returnInTimestamp)
            return String.valueOf(CdMs + voteDelay);
        Date resultdate = new Date(CdMs + voteDelay);
        return sdf.format(resultdate);
    }

    private static boolean checkIfIPorHWIDExistInDB(Player player, String mode) {
        boolean flag = false;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("SELECT * FROM reward_manager WHERE " + mode + "=?");
                try {
                    statement.setString(1, player.getHWID());
                    ResultSet rset = statement.executeQuery();
                    try {
                        if (rset.next())
                            flag = true;
                        if (rset != null)
                            rset.close();
                    } catch (Throwable throwable) {
                        if (rset != null)
                            try {
                                rset.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
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
        }
        return flag;
    }

    private static void insertNewParentOfPlayerIPHWID(Player player) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("INSERT INTO reward_manager (hwid,expire_time) VALUES (?,?)");
                try {
                    statement.setString(1, player.getHWID());
                    statement.setLong(2, System.currentTimeMillis());
                    statement.execute();
                    statement.close();
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
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
        }
    }

    public static DailyLoginRewardManager getInstance() {
        return SingletonHolder._instance;
    }

    public void load() {
        LOGGER.info("Daily Reward Manager: Loaded");
    }

    private static class SingletonHolder {
        protected static final DailyLoginRewardManager _instance = new DailyLoginRewardManager();
    }
}
