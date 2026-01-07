/**/
package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

public class LotteryManager {
    public static final long SECOND = 1000L;
    public static final long MINUTE = 60000L;
    protected static final CLogger LOGGER = new CLogger(LotteryManager.class.getName());
    private static final String INSERT_LOTTERY = "INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_PRICE = "UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?";
    private static final String UPDATE_LOTTERY = "UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?";
    private static final String SELECT_LAST_LOTTERY = "SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1";
    private static final String SELECT_LOTTERY_ITEM = "SELECT enchant_level, custom_type2 FROM items WHERE item_id = 4442 AND custom_type1 = ?";
    private static final String SELECT_LOTTERY_TICKET = "SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 and idnr = ?";
    protected int _number = 1;
    protected int _prize;
    protected boolean _isSellingTickets;
    protected boolean _isStarted;
    protected long _endDate;

    public static int[] decodeNumbers(int enchant, int type2) {
        int[] res = new int[5];
        int id = 0;

        int nr;
        int val;
        for (nr = 1; enchant > 0; ++nr) {
            val = enchant / 2;
            if ((double) val != (double) enchant / 2.0D) {
                res[id++] = nr;
            }

            enchant /= 2;
        }

        for (nr = 17; type2 > 0; ++nr) {
            val = type2 / 2;
            if ((double) val != (double) type2 / 2.0D) {
                res[id++] = nr;
            }

            type2 /= 2;
        }

        return res;
    }

    public static int[] checkTicket(ItemInstance item) {
        return checkTicket(item.getCustomType1(), item.getEnchantLevel(), item.getCustomType2());
    }

    public static int[] checkTicket(int id, int enchant, int type2) {
        int[] res = new int[]{0, 0};

        try {
            Connection con = ConnectionPool.getConnection();

            int[] var20;
            label150:
            {
                try {
                    label138:
                    {
                        PreparedStatement ps;
                        label139:
                        {
                            ps = con.prepareStatement("SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 and idnr = ?");

                            try {
                                label140:
                                {
                                    ps.setInt(1, id);
                                    ResultSet rs = ps.executeQuery();

                                    label122:
                                    {
                                        try {
                                            if (rs.next()) {
                                                int curenchant = rs.getInt("number1") & enchant;
                                                int curtype2 = rs.getInt("number2") & type2;
                                                if (curenchant == 0 && curtype2 == 0) {
                                                    var20 = res;
                                                    break label122;
                                                }

                                                int count = 0;

                                                for (int i = 1; i <= 16; ++i) {
                                                    int val = curenchant / 2;
                                                    if ((double) val != (double) curenchant / 2.0D) {
                                                        ++count;
                                                    }

                                                    int val2 = curtype2 / 2;
                                                    if ((double) val2 != (double) curtype2 / 2.0D) {
                                                        ++count;
                                                    }

                                                    curenchant = val;
                                                    curtype2 = val2;
                                                }

                                                switch (count) {
                                                    case 0:
                                                        break;
                                                    case 1:
                                                    case 2:
                                                    default:
                                                        res[0] = 4;
                                                        res[1] = Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
                                                        break;
                                                    case 3:
                                                        res[0] = 3;
                                                        res[1] = rs.getInt("prize3");
                                                        break;
                                                    case 4:
                                                        res[0] = 2;
                                                        res[1] = rs.getInt("prize2");
                                                        break;
                                                    case 5:
                                                        res[0] = 1;
                                                        res[1] = rs.getInt("prize1");
                                                }
                                            }
                                        } catch (Throwable var16) {
                                            if (rs != null) {
                                                try {
                                                    rs.close();
                                                } catch (Throwable var15) {
                                                    var16.addSuppressed(var15);
                                                }
                                            }

                                            throw var16;
                                        }

                                        if (rs != null) {
                                            rs.close();
                                        }
                                        break label140;
                                    }

                                    if (rs != null) {
                                        rs.close();
                                    }
                                    break label139;
                                }
                            } catch (Throwable var17) {
                                if (ps != null) {
                                    try {
                                        ps.close();
                                    } catch (Throwable var14) {
                                        var17.addSuppressed(var14);
                                    }
                                }

                                throw var17;
                            }

                            if (ps != null) {
                                ps.close();
                            }
                            break label138;
                        }

                        if (ps != null) {
                            ps.close();
                        }
                        break label150;
                    }
                } catch (Throwable var18) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var13) {
                            var18.addSuppressed(var13);
                        }
                    }

                    throw var18;
                }

                if (con != null) {
                    con.close();
                }

                return res;
            }

            if (con != null) {
                con.close();
            }

            return var20;
        } catch (Exception var19) {
            LOGGER.error("Couldn't check lottery ticket #{}.", var19, id);
            return res;
        }
    }

    public static LotteryManager getInstance() {
        return LotteryManager.SingletonHolder.INSTANCE;
    }

    public void load() {
        this._prize = Config.ALT_LOTTERY_PRIZE;
        this._isSellingTickets = false;
        this._isStarted = false;
        this._endDate = System.currentTimeMillis();
        if (Config.ALLOW_LOTTERY) {
            (new LotteryManager.StartLottery()).run();
        }

    }

    public int getId() {
        return this._number;
    }

    public int getPrize() {
        return this._prize;
    }

    public long getEndDate() {
        return this._endDate;
    }

    public boolean isSellableTickets() {
        return this._isSellingTickets;
    }

    public boolean isStarted() {
        return this._isStarted;
    }

    public void increasePrize(int count) {
        this._prize += count;

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?");

                try {
                    ps.setInt(1, this.getPrize());
                    ps.setInt(2, this.getPrize());
                    ps.setInt(3, this.getId());
                    ps.execute();
                } catch (Throwable var8) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var8.addSuppressed(var7);
                        }
                    }

                    throw var8;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var9) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                    }
                }

                throw var9;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var10) {
            LOGGER.error("Couldn't increase current lottery prize.", var10);
        }

    }

    private static class SingletonHolder {
        protected static final LotteryManager INSTANCE = new LotteryManager();
    }

    private class StartLottery implements Runnable {
        protected StartLottery() {
        }

        public void run() {
            try {
                label190:
                {
                    Connection con = ConnectionPool.getConnection();

                    label191:
                    {
                        label183:
                        {
                            try {
                                PreparedStatement ps;
                                label193:
                                {
                                    label194:
                                    {
                                        ps = con.prepareStatement("SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1");

                                        try {
                                            label195:
                                            {
                                                ResultSet rs = ps.executeQuery();

                                                label174:
                                                {
                                                    label173:
                                                    {
                                                        try {
                                                            if (rs.next()) {
                                                                LotteryManager.this._number = rs.getInt("idnr");
                                                                if (rs.getInt("finished") == 1) {
                                                                    ++LotteryManager.this._number;
                                                                    LotteryManager.this._prize = rs.getInt("newprize");
                                                                } else {
                                                                    LotteryManager.this._prize = rs.getInt("prize");
                                                                    LotteryManager.this._endDate = rs.getLong("enddate");
                                                                    if (LotteryManager.this._endDate <= System.currentTimeMillis() + 120000L) {
                                                                        (LotteryManager.this.new FinishLottery()).run();
                                                                        break label173;
                                                                    }

                                                                    if (LotteryManager.this._endDate > System.currentTimeMillis()) {
                                                                        LotteryManager.this._isStarted = true;
                                                                        ThreadPool.schedule(LotteryManager.this.new FinishLottery(), LotteryManager.this._endDate - System.currentTimeMillis());
                                                                        if (LotteryManager.this._endDate > System.currentTimeMillis() + 720000L) {
                                                                            LotteryManager.this._isSellingTickets = true;
                                                                            ThreadPool.schedule(LotteryManager.this.new StopSellingTickets(), LotteryManager.this._endDate - System.currentTimeMillis() - 600000L);
                                                                        }
                                                                        break label174;
                                                                    }
                                                                }
                                                            }
                                                        } catch (Throwable var14) {
                                                            if (rs != null) {
                                                                try {
                                                                    rs.close();
                                                                } catch (Throwable var10) {
                                                                    var14.addSuppressed(var10);
                                                                }
                                                            }

                                                            throw var14;
                                                        }

                                                        if (rs != null) {
                                                            rs.close();
                                                        }
                                                        break label193;
                                                    }

                                                    if (rs != null) {
                                                        rs.close();
                                                    }
                                                    break label195;
                                                }

                                                if (rs != null) {
                                                    rs.close();
                                                }
                                                break label194;
                                            }
                                        } catch (Throwable var15) {
                                            if (ps != null) {
                                                try {
                                                    ps.close();
                                                } catch (Throwable var9) {
                                                    var15.addSuppressed(var9);
                                                }
                                            }

                                            throw var15;
                                        }

                                        if (ps != null) {
                                            ps.close();
                                        }
                                        break label183;
                                    }

                                    if (ps != null) {
                                        ps.close();
                                    }
                                    break label191;
                                }

                                if (ps != null) {
                                    ps.close();
                                }
                            } catch (Throwable var16) {
                                if (con != null) {
                                    try {
                                        con.close();
                                    } catch (Throwable var8) {
                                        var16.addSuppressed(var8);
                                    }
                                }

                                throw var16;
                            }

                            if (con != null) {
                                con.close();
                            }
                            break label190;
                        }

                        if (con != null) {
                            con.close();
                        }

                        return;
                    }

                    if (con != null) {
                        con.close();
                    }

                    return;
                }
            } catch (Exception var17) {
                LotteryManager.LOGGER.error("Couldn't restore lottery data.", var17);
            }

            LotteryManager.this._isSellingTickets = true;
            LotteryManager.this._isStarted = true;
            World.announceToOnlinePlayers("Lottery tickets are now available for Lucky Lottery #" + LotteryManager.this.getId() + ".");
            Calendar finishTime = Calendar.getInstance();
            finishTime.setTimeInMillis(LotteryManager.this._endDate);
            finishTime.set(Calendar.MINUTE, 0);
            finishTime.set(Calendar.SECOND, 0);
            if (finishTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                finishTime.set(Calendar.HOUR_OF_DAY, 19);
                LotteryManager.this._endDate = finishTime.getTimeInMillis();
                LotteryManager var10000 = LotteryManager.this;
                var10000._endDate += 604800000L;
            } else {
                finishTime.set(Calendar.DAY_OF_WEEK, 1);
                finishTime.set(Calendar.HOUR_OF_DAY, 19);
                LotteryManager.this._endDate = finishTime.getTimeInMillis();
            }

            ThreadPool.schedule(LotteryManager.this.new StopSellingTickets(), LotteryManager.this._endDate - System.currentTimeMillis() - 600000L);
            ThreadPool.schedule(LotteryManager.this.new FinishLottery(), LotteryManager.this._endDate - System.currentTimeMillis());

            try {
                Connection conx = ConnectionPool.getConnection();

                try {
                    PreparedStatement psx = conx.prepareStatement("INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)");

                    try {
                        psx.setInt(1, 1);
                        psx.setInt(2, LotteryManager.this.getId());
                        psx.setLong(3, LotteryManager.this.getEndDate());
                        psx.setInt(4, LotteryManager.this.getPrize());
                        psx.setInt(5, LotteryManager.this.getPrize());
                        psx.execute();
                    } catch (Throwable var11) {
                        if (psx != null) {
                            try {
                                psx.close();
                            } catch (Throwable var7) {
                                var11.addSuppressed(var7);
                            }
                        }

                        throw var11;
                    }

                    if (psx != null) {
                        psx.close();
                    }
                } catch (Throwable var12) {
                    if (conx != null) {
                        try {
                            conx.close();
                        } catch (Throwable var6) {
                            var12.addSuppressed(var6);
                        }
                    }

                    throw var12;
                }

                if (conx != null) {
                    conx.close();
                }
            } catch (Exception var13) {
                LotteryManager.LOGGER.error("Couldn't store new lottery data.", var13);
            }

        }
    }

    private class FinishLottery implements Runnable {
        protected FinishLottery() {
        }

        public void run() {
            int[] luckynums = new int[5];
            int luckynum = 0;

            int enchant;
            int count1;
            for (enchant = 0; enchant < 5; ++enchant) {
                boolean found = true;

                while (found) {
                    luckynum = Rnd.get(20) + 1;
                    found = false;

                    for (count1 = 0; count1 < enchant; ++count1) {
                        if (luckynums[count1] == luckynum) {
                            found = true;
                        }
                    }
                }

                luckynums[enchant] = luckynum;
            }

            enchant = 0;
            int type2 = 0;

            for (count1 = 0; count1 < 5; ++count1) {
                if (luckynums[count1] < 17) {
                    enchant = (int) ((double) enchant + Math.pow(2.0D, luckynums[count1] - 1));
                } else {
                    type2 = (int) ((double) type2 + Math.pow(2.0D, luckynums[count1] - 17));
                }
            }

            count1 = 0;
            int count2 = 0;
            int count3 = 0;
            int count4 = 0;

            int curenchant;
            int curtype2;
            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("SELECT enchant_level, custom_type2 FROM items WHERE item_id = 4442 AND custom_type1 = ?");

                    try {
                        ps.setInt(1, LotteryManager.this.getId());
                        ResultSet rs = ps.executeQuery();

                        try {
                            while (rs.next()) {
                                curenchant = rs.getInt("enchant_level") & enchant;
                                curtype2 = rs.getInt("custom_type2") & type2;
                                if (curenchant != 0 || curtype2 != 0) {
                                    int count = 0;

                                    for (int i = 1; i <= 16; ++i) {
                                        int val = curenchant / 2;
                                        if ((double) val != (double) curenchant / 2.0D) {
                                            ++count;
                                        }

                                        int val2 = curtype2 / 2;
                                        if ((double) val2 != (double) curtype2 / 2.0D) {
                                            ++count;
                                        }

                                        curenchant = val;
                                        curtype2 = val2;
                                    }

                                    if (count == 5) {
                                        ++count1;
                                    } else if (count == 4) {
                                        ++count2;
                                    } else if (count == 3) {
                                        ++count3;
                                    } else if (count > 0) {
                                        ++count4;
                                    }
                                }
                            }
                        } catch (Throwable var26) {
                            if (rs != null) {
                                try {
                                    rs.close();
                                } catch (Throwable var22) {
                                    var26.addSuppressed(var22);
                                }
                            }

                            throw var26;
                        }

                        if (rs != null) {
                            rs.close();
                        }
                    } catch (Throwable var27) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var21) {
                                var27.addSuppressed(var21);
                            }
                        }

                        throw var27;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } catch (Throwable var28) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var20) {
                            var28.addSuppressed(var20);
                        }
                    }

                    throw var28;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var29) {
                LotteryManager.LOGGER.error("Couldn't restore lottery data.", var29);
            }

            int prize4 = count4 * Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
            int prize1 = 0;
            int prize2 = 0;
            curenchant = 0;
            if (count1 > 0) {
                prize1 = (int) ((double) (LotteryManager.this.getPrize() - prize4) * Config.ALT_LOTTERY_5_NUMBER_RATE / (double) count1);
            }

            if (count2 > 0) {
                prize2 = (int) ((double) (LotteryManager.this.getPrize() - prize4) * Config.ALT_LOTTERY_4_NUMBER_RATE / (double) count2);
            }

            if (count3 > 0) {
                curenchant = (int) ((double) (LotteryManager.this.getPrize() - prize4) * Config.ALT_LOTTERY_3_NUMBER_RATE / (double) count3);
            }

            curtype2 = Config.ALT_LOTTERY_PRIZE + LotteryManager.this.getPrize() - (prize1 + prize2 + curenchant + prize4);
            if (count1 > 0) {
                World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_WINNER_S1_IS_S2_ADENA_WE_HAVE_S3_PRIZE_WINNER).addNumber(LotteryManager.this.getId()).addNumber(LotteryManager.this.getPrize()).addNumber(count1));
            } else {
                World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_LOTTERY_S1_IS_S2_ADENA_NO_WINNER).addNumber(LotteryManager.this.getId()).addNumber(LotteryManager.this.getPrize()));
            }

            try {
                Connection conx = ConnectionPool.getConnection();

                try {
                    PreparedStatement psx = conx.prepareStatement("UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?");

                    try {
                        psx.setInt(1, LotteryManager.this.getPrize());
                        psx.setInt(2, curtype2);
                        psx.setInt(3, enchant);
                        psx.setInt(4, type2);
                        psx.setInt(5, prize1);
                        psx.setInt(6, prize2);
                        psx.setInt(7, curenchant);
                        psx.setInt(8, LotteryManager.this.getId());
                        psx.execute();
                    } catch (Throwable var23) {
                        if (psx != null) {
                            try {
                                psx.close();
                            } catch (Throwable var19) {
                                var23.addSuppressed(var19);
                            }
                        }

                        throw var23;
                    }

                    if (psx != null) {
                        psx.close();
                    }
                } catch (Throwable var24) {
                    if (conx != null) {
                        try {
                            conx.close();
                        } catch (Throwable var18) {
                            var24.addSuppressed(var18);
                        }
                    }

                    throw var24;
                }

                if (conx != null) {
                    conx.close();
                }
            } catch (Exception var25) {
                LotteryManager.LOGGER.error("Couldn't store finished lottery data.", var25);
            }

            ThreadPool.schedule(LotteryManager.this.new StartLottery(), 60000L);
            ++LotteryManager.this._number;
            LotteryManager.this._isStarted = false;
        }
    }

    private class StopSellingTickets implements Runnable {
        protected StopSellingTickets() {
        }

        public void run() {
            LotteryManager.this._isSellingTickets = false;
            World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.LOTTERY_TICKET_SALES_TEMP_SUSPENDED));
        }
    }
}