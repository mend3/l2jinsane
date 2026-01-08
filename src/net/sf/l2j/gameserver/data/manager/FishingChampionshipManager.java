/**/
package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.sql.ServerMemoTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class FishingChampionshipManager {
    private static final CLogger LOGGER = new CLogger(FishingChampionshipManager.class.getName());
    private static final String INSERT = "INSERT INTO fishing_championship(player_name,fish_length,rewarded) VALUES (?,?,?)";
    private static final String DELETE = "DELETE FROM fishing_championship";
    private static final String SELECT = "SELECT `player_name`, `fish_length`, `rewarded` FROM fishing_championship";
    private final List<String> _playersName = new ArrayList<>();
    private final List<String> _fishLength = new ArrayList<>();
    private final List<String> _winPlayersName = new ArrayList<>();
    private final List<String> _winFishLength = new ArrayList<>();
    private final List<FishingChampionshipManager.Fisher> _tmpPlayers = new ArrayList<>();
    private final List<FishingChampionshipManager.Fisher> _winPlayers = new ArrayList<>();
    private long _endDate = 0L;
    private double _minFishLength = 0.0D;
    private boolean _needRefresh = true;

    public static FishingChampionshipManager getInstance() {
        return FishingChampionshipManager.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.restoreData();
        this.refreshWinResult();
        this.recalculateMinLength();
        if (this._endDate <= System.currentTimeMillis()) {
            this._endDate = System.currentTimeMillis();
            this.finishChamp();
        } else {
            ThreadPool.schedule(this::finishChamp, this._endDate - System.currentTimeMillis());
        }

    }

    private void setEndOfChamp() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(this._endDate);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.DATE, 6);
        cal.set(Calendar.DAY_OF_WEEK, 3);
        cal.set(Calendar.HOUR_OF_DAY, 19);
        this._endDate = cal.getTimeInMillis();
    }

    private void restoreData() {
        this._endDate = ServerMemoTable.getInstance().getLong("fishChampionshipEnd", 0L);

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT `player_name`, `fish_length`, `rewarded` FROM fishing_championship");

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            int rewarded = rs.getInt("rewarded");
                            if (rewarded == 0) {
                                this._tmpPlayers.add(new Fisher(this, rs.getString("player_name"), rs.getDouble("fish_length"), 0));
                            } else if (rewarded > 0) {
                                this._winPlayers.add(new Fisher(this, rs.getString("player_name"), rs.getDouble("fish_length"), rewarded));
                            }
                        }
                    } catch (Throwable var9) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var10) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var11) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var6) {
                        var11.addSuppressed(var6);
                    }
                }

                throw var11;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var12) {
            LOGGER.error("Couldn't restore fishing championship data.", var12);
        }

    }

    private synchronized void refreshResult() {
        this._needRefresh = false;
        this._playersName.clear();
        this._fishLength.clear();

        int x;
        for (x = 0; x <= this._tmpPlayers.size() - 1; ++x) {
            for (int y = 0; y <= this._tmpPlayers.size() - 2; ++y) {
                FishingChampionshipManager.Fisher fisher1 = this._tmpPlayers.get(y);
                FishingChampionshipManager.Fisher fisher2 = this._tmpPlayers.get(y + 1);
                if (fisher1.getLength() < fisher2.getLength()) {
                    this._tmpPlayers.set(y, fisher2);
                    this._tmpPlayers.set(y + 1, fisher1);
                }
            }
        }

        for (x = 0; x <= this._tmpPlayers.size() - 1; ++x) {
            this._playersName.add(this._tmpPlayers.get(x).getName());
            this._fishLength.add(String.valueOf(this._tmpPlayers.get(x).getLength()));
        }

    }

    private void refreshWinResult() {
        this._winPlayersName.clear();
        this._winFishLength.clear();

        int x;
        for (x = 0; x <= this._winPlayers.size() - 1; ++x) {
            for (int y = 0; y <= this._winPlayers.size() - 2; ++y) {
                FishingChampionshipManager.Fisher fisher1 = this._winPlayers.get(y);
                FishingChampionshipManager.Fisher fisher2 = this._winPlayers.get(y + 1);
                if (fisher1.getLength() < fisher2.getLength()) {
                    this._winPlayers.set(y, fisher2);
                    this._winPlayers.set(y + 1, fisher1);
                }
            }
        }

        for (x = 0; x <= this._winPlayers.size() - 1; ++x) {
            this._winPlayersName.add(this._winPlayers.get(x).getName());
            this._winFishLength.add(String.valueOf(this._winPlayers.get(x).getLength()));
        }

    }

    private void finishChamp() {
        this._winPlayers.clear();

        for (Fisher fisher : this._tmpPlayers) {
            fisher.setRewardType(1);
            this._winPlayers.add(fisher);
        }

        this._tmpPlayers.clear();
        this.refreshWinResult();
        this.setEndOfChamp();
        this.shutdown();
        LOGGER.info("A new Fishing Championship event period has started.");
        ThreadPool.schedule(this::finishChamp, this._endDate - System.currentTimeMillis());
    }

    private void recalculateMinLength() {
        double minLen = 99999.0D;

        for (Fisher fisher : this._tmpPlayers) {
            if (fisher.getLength() < minLen) {
                minLen = fisher.getLength();
            }
        }

        this._minFishLength = minLen;
    }

    public synchronized void newFish(Player player, int lureId) {
        if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED) {
            double len = (double) Rnd.get(60, 89) + (double) Rnd.get(0, 1000) / 1000.0D;
            if (lureId >= 8484 && lureId <= 8486) {
                len += (double) Rnd.get(0, 3000) / 1000.0D;
            }

            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CAUGHT_FISH_S1_LENGTH).addString(String.valueOf(len)));
            Iterator var5;
            FishingChampionshipManager.Fisher fisher;
            if (this._tmpPlayers.size() < 5) {
                var5 = this._tmpPlayers.iterator();

                while (var5.hasNext()) {
                    fisher = (FishingChampionshipManager.Fisher) var5.next();
                    if (fisher.getName().equalsIgnoreCase(player.getName())) {
                        if (fisher.getLength() < len) {
                            fisher.setLength(len);
                            player.sendPacket(SystemMessageId.REGISTERED_IN_FISH_SIZE_RANKING);
                            this.recalculateMinLength();
                        }

                        return;
                    }
                }

                this._tmpPlayers.add(new Fisher(this, player.getName(), len, 0));
                player.sendPacket(SystemMessageId.REGISTERED_IN_FISH_SIZE_RANKING);
                this.recalculateMinLength();
            } else if (this._minFishLength < len) {
                var5 = this._tmpPlayers.iterator();

                while (var5.hasNext()) {
                    fisher = (FishingChampionshipManager.Fisher) var5.next();
                    if (fisher.getName().equalsIgnoreCase(player.getName())) {
                        if (fisher.getLength() < len) {
                            fisher.setLength(len);
                            player.sendPacket(SystemMessageId.REGISTERED_IN_FISH_SIZE_RANKING);
                            this.recalculateMinLength();
                        }

                        return;
                    }
                }

                FishingChampionshipManager.Fisher minFisher = null;
                double minLen = 99999.0D;

                for (Fisher tmpPlayer : this._tmpPlayers) {
                    fisher = tmpPlayer;
                    if (fisher.getLength() < minLen) {
                        minFisher = fisher;
                        minLen = fisher.getLength();
                    }
                }

                this._tmpPlayers.remove(minFisher);
                this._tmpPlayers.add(new Fisher(this, player.getName(), len, 0));
                player.sendPacket(SystemMessageId.REGISTERED_IN_FISH_SIZE_RANKING);
                this.recalculateMinLength();
            }

        }
    }

    public long getTimeRemaining() {
        return (this._endDate - System.currentTimeMillis()) / 60000L;
    }

    public String getWinnerName(int par) {
        return this._winPlayersName.size() >= par ? this._winPlayersName.get(par - 1) : "None";
    }

    public String getCurrentName(int par) {
        return this._playersName.size() >= par ? this._playersName.get(par - 1) : "None";
    }

    public String getFishLength(int par) {
        return this._winFishLength.size() >= par ? this._winFishLength.get(par - 1) : "0";
    }

    public String getCurrentFishLength(int par) {
        return this._fishLength.size() >= par ? this._fishLength.get(par - 1) : "0";
    }

    public boolean isWinner(String playerName) {
        Iterator<String> var2 = this._winPlayersName.iterator();

        String name;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            name = var2.next();
        } while (!name.equals(playerName));

        return true;
    }

    public void getReward(Player player) {
        Iterator<Fisher> var2 = this._winPlayers.iterator();

        while (true) {
            FishingChampionshipManager.Fisher fisher;
            do {
                do {
                    if (!var2.hasNext()) {
                        return;
                    }

                    fisher = var2.next();
                } while (!fisher.getName().equalsIgnoreCase(player.getName()));
            } while (fisher.getRewardType() == 2);

            int rewardCnt = 0;

            for (int x = 0; x < this._winPlayersName.size(); ++x) {
                if (this._winPlayersName.get(x).equalsIgnoreCase(player.getName())) {
                    rewardCnt = switch (x) {
                        case 0 -> Config.ALT_FISH_CHAMPIONSHIP_REWARD_1;
                        case 1 -> Config.ALT_FISH_CHAMPIONSHIP_REWARD_2;
                        case 2 -> Config.ALT_FISH_CHAMPIONSHIP_REWARD_3;
                        case 3 -> Config.ALT_FISH_CHAMPIONSHIP_REWARD_4;
                        case 4 -> Config.ALT_FISH_CHAMPIONSHIP_REWARD_5;
                        default -> rewardCnt;
                    };
                }
            }

            fisher.setRewardType(2);
            if (rewardCnt > 0) {
                player.addItem("fishing_reward", Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM, rewardCnt, null, true);
                NpcHtmlMessage html = new NpcHtmlMessage(0);
                html.setFile("data/html/fisherman/championship/fish_event_reward001.htm");
                player.sendPacket(html);
            }
        }
    }

    public void showMidResult(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        if (this._needRefresh) {
            html.setFile("data/html/fisherman/championship/fish_event003.htm");
            player.sendPacket(html);
            this.refreshResult();
            ThreadPool.schedule(() -> this._needRefresh = true, 60000L);
        } else {
            html.setFile("data/html/fisherman/championship/fish_event002.htm");
            StringBuilder sb = new StringBuilder(100);

            for (int x = 1; x <= 5; ++x) {
                StringUtil.append(sb, "<tr><td width=70 align=center>", x, "</td>");
                StringUtil.append(sb, "<td width=110 align=center>", this.getCurrentName(x), "</td>");
                StringUtil.append(sb, "<td width=80 align=center>", this.getCurrentFishLength(x), "</td></tr>");
            }

            html.replace("%TABLE%", sb.toString());
            html.replace("%prizeItem%", ItemTable.getInstance().getTemplate(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM).getName());
            html.replace("%prizeFirst%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_1);
            html.replace("%prizeTwo%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_2);
            html.replace("%prizeThree%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_3);
            html.replace("%prizeFour%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_4);
            html.replace("%prizeFive%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_5);
            player.sendPacket(html);
        }
    }

    public void showChampScreen(Player player, int objectId) {
        NpcHtmlMessage html = new NpcHtmlMessage(objectId);
        html.setFile("data/html/fisherman/championship/fish_event001.htm");
        StringBuilder sb = new StringBuilder(100);

        for (int x = 1; x <= 5; ++x) {
            StringUtil.append(sb, "<tr><td width=70 align=center>", x, "</td>");
            StringUtil.append(sb, "<td width=110 align=center>", this.getWinnerName(x), "</td>");
            StringUtil.append(sb, "<td width=80 align=center>", this.getFishLength(x), "</td></tr>");
        }

        html.replace("%TABLE%", sb.toString());
        html.replace("%prizeItem%", ItemTable.getInstance().getTemplate(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM).getName());
        html.replace("%prizeFirst%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_1);
        html.replace("%prizeTwo%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_2);
        html.replace("%prizeThree%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_3);
        html.replace("%prizeFour%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_4);
        html.replace("%prizeFive%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_5);
        html.replace("%refresh%", this.getTimeRemaining());
        html.replace("%objectId%", objectId);
        player.sendPacket(html);
    }

    public void shutdown() {
        ServerMemoTable.getInstance().set("fishChampionshipEnd", this._endDate);

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM fishing_championship");

                try {
                    PreparedStatement ps2 = con.prepareStatement("INSERT INTO fishing_championship(player_name,fish_length,rewarded) VALUES (?,?,?)");

                    try {
                        ps.execute();
                        ps.close();
                        Iterator<Fisher> var4 = this._winPlayers.iterator();

                        FishingChampionshipManager.Fisher fisher;
                        while (var4.hasNext()) {
                            fisher = var4.next();
                            ps2.setString(1, fisher.getName());
                            ps2.setDouble(2, fisher.getLength());
                            ps2.setInt(3, fisher.getRewardType());
                            ps2.addBatch();
                        }

                        var4 = this._tmpPlayers.iterator();

                        while (true) {
                            if (!var4.hasNext()) {
                                ps2.executeBatch();
                                break;
                            }

                            fisher = var4.next();
                            ps2.setString(1, fisher.getName());
                            ps2.setDouble(2, fisher.getLength());
                            ps2.setInt(3, 0);
                            ps2.addBatch();
                        }
                    } catch (Throwable var9) {
                        if (ps2 != null) {
                            try {
                                ps2.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (ps2 != null) {
                        ps2.close();
                    }
                } catch (Throwable var10) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var11) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var6) {
                        var11.addSuppressed(var6);
                    }
                }

                throw var11;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var12) {
            LOGGER.error("Couldn't update fishing championship data.", var12);
        }

    }

    private static class SingletonHolder {
        protected static final FishingChampionshipManager INSTANCE = new FishingChampionshipManager();
    }

    private static class Fisher {
        private final String _name;
        private double _length;
        private int _reward;

        public Fisher(final FishingChampionshipManager param1, String name, double length, int rewardType) {
            this._name = name;
            this._length = length;
            this._reward = rewardType;
        }

        public String getName() {
            return this._name;
        }

        public int getRewardType() {
            return this._reward;
        }

        public void setRewardType(int value) {
            this._reward = value;
        }

        public double getLength() {
            return this._length;
        }

        public void setLength(double value) {
            this._length = value;
        }
    }
}