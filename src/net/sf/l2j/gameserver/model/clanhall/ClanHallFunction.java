/**/
package net.sf.l2j.gameserver.model.clanhall;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.pledge.Clan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.ScheduledFuture;

public class ClanHallFunction {
    private static final CLogger LOGGER = new CLogger(ClanHallFunction.class.getName());
    private static final String UPDATE_FUNCTION = "REPLACE INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)";
    private static final String DELETE_FUNCTION = "DELETE FROM clanhall_functions WHERE hall_id=? AND type=?";
    private final ClanHall _ch;
    private final int _type;
    private final long _rate;
    private ScheduledFuture<?> _feeTask;
    private int _lvl;
    private int _fee;
    private long _endDate;

    public ClanHallFunction(ClanHall ch, int type, int lvl, int fee, long rate, long endDate) {
        this._ch = ch;
        this._type = type;
        this._lvl = lvl;
        this._fee = fee;
        this._rate = rate;
        this._endDate = endDate;
        if (!this._ch.isFree()) {
            long currentTime = System.currentTimeMillis();
            if (this._endDate > currentTime) {
                this._feeTask = ThreadPool.schedule(new ClanHallFunction.FunctionTask(), this._endDate - currentTime);
            } else {
                ThreadPool.execute(new ClanHallFunction.FunctionTask());
            }

        }
    }

    public int getType() {
        return this._type;
    }

    public int getLvl() {
        return this._lvl;
    }

    public int getLease() {
        return this._fee;
    }

    public long getRate() {
        return this._rate;
    }

    public long getEndTime() {
        return this._endDate;
    }

    public void refreshEndTime() {
        this._endDate = System.currentTimeMillis() + this.getRate();
    }

    public void stopFeeTask() {
        if (this._feeTask != null) {
            this._feeTask.cancel(false);
            this._feeTask = null;
        }

    }

    public void dbSave() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("REPLACE INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");

                try {
                    ps.setInt(1, this._ch.getId());
                    ps.setInt(2, this.getType());
                    ps.setInt(3, this.getLvl());
                    ps.setInt(4, this.getLease());
                    ps.setLong(5, this.getRate());
                    ps.setLong(6, this.getEndTime());
                    ps.execute();
                } catch (Throwable var7) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var8) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var5) {
                        var8.addSuppressed(var5);
                    }
                }

                throw var8;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var9) {
            LOGGER.error("Couldn't save clan hall function.", var9);
        }

    }

    public void removeFunction() {
        this.stopFeeTask();
        this._ch.getFunctions().remove(this.getType());

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?");

                try {
                    ps.setInt(1, this._ch.getId());
                    ps.setInt(2, this.getType());
                    ps.execute();
                } catch (Throwable var7) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var8) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var5) {
                        var8.addSuppressed(var5);
                    }
                }

                throw var8;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var9) {
            LOGGER.error("Couldn't remove clan hall function.", var9);
        }

    }

    public void refreshFunction(int fee, int lvl) {
        this.stopFeeTask();
        this._fee = fee;
        this._lvl = lvl;
        this.refreshEndTime();
        this.dbSave();
        this._feeTask = ThreadPool.schedule(new ClanHallFunction.FunctionTask(), this.getRate());
    }

    private class FunctionTask implements Runnable {
        public FunctionTask() {
        }

        public void run() {
            if (!ClanHallFunction.this._ch.isFree()) {
                Clan clan = ClanTable.getInstance().getClan(ClanHallFunction.this._ch.getOwnerId());
                if (clan != null && clan.getWarehouse().getAdena() >= ClanHallFunction.this._fee) {
                    clan.getWarehouse().destroyItemByItemId("CH_function_fee", 57, ClanHallFunction.this._fee, null, null);
                    ClanHallFunction.this.refreshEndTime();
                    ClanHallFunction.this.dbSave();
                    ClanHallFunction.this._feeTask = ThreadPool.schedule(ClanHallFunction.this.new FunctionTask(), ClanHallFunction.this.getRate());
                } else {
                    ClanHallFunction.this.removeFunction();
                }

            }
        }
    }
}