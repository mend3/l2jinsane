package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.communitybbs.manager.BaseBBSManager;
import net.sf.l2j.gameserver.data.DailyRewardData;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.DailyReward;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.util.variables.PlayerVariables;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DailyRewardManager {
    private static final CLogger LOGGER = new CLogger(DailyRewardManager.class.getName());
    private Map<Integer, List<LocalDate>> _playersReceivdList = new ConcurrentHashMap<>();
    private Map<String, List<LocalDate>> _hwidReceivedList = new ConcurrentHashMap<>();

    public DailyRewardManager() {
        this.loadData();
        DailyRewardData.getInstance();
    }

    public static void agregarNuevoRegistroAlMapHID(Map<String, List<LocalDate>> map, String key, LocalDate value) {
        (map.computeIfAbsent(key, (k) -> new ArrayList<>())).add(value);
    }

    public static void agregarNuevoRegistroAlMap(Map<Integer, List<LocalDate>> map, Integer key, LocalDate value) {
        (map.computeIfAbsent(key, (k) -> new ArrayList<>())).add(value);
    }

    private static void saveMapHWIDToDatabase(Map<String, List<LocalDate>> testmap) {
        String truncateSQL = "TRUNCATE TABLE daily_rewarded_players_hwid";
        String insertSQL = "INSERT INTO daily_rewarded_players_hwid (hwid, fecha) VALUES (?, ?)";

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Statement stmt = conn.createStatement();

                try {
                    stmt.executeUpdate(truncateSQL);
                } catch (Throwable var21) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (Throwable var19) {
                            var21.addSuppressed(var19);
                        }
                    }

                    throw var21;
                }

                if (stmt != null) {
                    stmt.close();
                }

                PreparedStatement pstmt = conn.prepareStatement(insertSQL);

                try {
                    (testmap.entrySet().parallelStream().flatMap((entry) -> (entry.getValue()).stream()
                            .map((fecha) -> new AbstractMap.SimpleEntry<>(entry.getKey(), fecha))).sequential()).forEach((pair) -> {
                        try {
                            pstmt.setString(1, (String) pair.getKey());
                            pstmt.setDate(2, Date.valueOf((LocalDate) pair.getValue()));
                            pstmt.addBatch();
                        } catch (SQLException e) {
                            throw new RuntimeException("Error procesando datos", e);
                        }
                    });
                    pstmt.executeBatch();
                } catch (Throwable var20) {
                    if (pstmt != null) {
                        try {
                            pstmt.close();
                        } catch (Throwable var18) {
                            var20.addSuppressed(var18);
                        }
                    }

                    throw var20;
                }

                if (pstmt != null) {
                    pstmt.close();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Error en la operación de base de datos", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.error("Error de conexión: " + e.getMessage());
        }

    }

    private static Map<String, List<LocalDate>> loadMapHWIDFromDatabaseWithStream() {
        String selectSQL = "SELECT hwid, fecha FROM daily_rewarded_players_hwid ORDER BY hwid, fecha";

        try {
            Map<String, List<LocalDate>> var4;
            try (
                    Connection conn = ConnectionPool.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(selectSQL);
                    ResultSet rs = pstmt.executeQuery()
            ) {
                var4 = resultSetToStreamHWID(rs).collect(Collectors.groupingBy(TestMapRowHWID::hwid, Collectors.mapping(TestMapRowHWID::fecha, Collectors.toList())));
            }

            return var4;
        } catch (SQLException e) {
            LOGGER.error(e);
            return new HashMap<>();
        }
    }

    private static Stream<TestMapRowHWID> resultSetToStreamHWID(ResultSet rs) {
        return Stream.generate(() -> {
            try {
                if (rs.next()) {
                    String hwid = rs.getString("hwid");
                    LocalDate fecha = rs.getDate("fecha").toLocalDate();
                    return new TestMapRowHWID(hwid, fecha);
                } else {
                    return null;
                }
            } catch (SQLException e) {
                LOGGER.error("Error al procesar ResultSet: " + e.getMessage());
                throw new RuntimeException("Error al leer ResultSet", e);
            }
        }).takeWhile(Objects::nonNull);
    }

    private static void saveMapToDatabase(Map<Integer, List<LocalDate>> testmap) {
        String truncateSQL = "TRUNCATE TABLE daily_rewarded_players";
        String insertSQL = "INSERT INTO daily_rewarded_players (obj_Id, fecha) VALUES (?, ?)";

        try (Connection conn = ConnectionPool.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Statement stmt = conn.createStatement();

                try {
                    stmt.executeUpdate(truncateSQL);
                } catch (Throwable var21) {
                    if (stmt != null) {
                        try {
                            stmt.close();
                        } catch (Throwable var19) {
                            var21.addSuppressed(var19);
                        }
                    }

                    throw var21;
                }

                if (stmt != null) {
                    stmt.close();
                }

                PreparedStatement pstmt = conn.prepareStatement(insertSQL);

                try {
                    testmap.entrySet().stream()
                            .flatMap((entry) -> (entry.getValue()).stream().map((fecha) -> new AbstractMap.SimpleEntry<>(entry.getKey(), fecha)))
                            .forEach((pair) -> {
                                try {
                                    pstmt.setInt(1, (Integer) pair.getKey());
                                    pstmt.setDate(2, Date.valueOf((LocalDate) pair.getValue()));
                                    pstmt.addBatch();
                                } catch (SQLException e) {
                                    throw new RuntimeException("Error procesando datos", e);
                                }
                            });
                    pstmt.executeBatch();
                } catch (Throwable var20) {
                    if (pstmt != null) {
                        try {
                            pstmt.close();
                        } catch (Throwable var18) {
                            var20.addSuppressed(var18);
                        }
                    }

                    throw var20;
                }

                if (pstmt != null) {
                    pstmt.close();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Error en la operación de base de datos", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.error("Error de conexión: " + e.getMessage());
        }

    }

    private static Map<Integer, List<LocalDate>> loadMapFromDatabaseWithStream() {
        String selectSQL = "SELECT obj_Id, fecha FROM daily_rewarded_players ORDER BY obj_Id, fecha";

        try {
            Map<Integer, List<LocalDate>> var4;
            try (
                    Connection conn = ConnectionPool.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(selectSQL);
                    ResultSet rs = pstmt.executeQuery()
            ) {
                var4 = resultSetToStream(rs).collect(Collectors.groupingBy(TestMapRow::obj_Id, Collectors.mapping(TestMapRow::fecha, Collectors.toList())));
            }

            return var4;
        } catch (SQLException e) {
            LOGGER.error(e);
            return new HashMap<>();
        }
    }

    private static Stream<TestMapRow> resultSetToStream(ResultSet rs) {
        return Stream.generate(() -> {
            try {
                if (rs.next()) {
                    Integer hwid = rs.getInt("obj_Id");
                    LocalDate fecha = rs.getDate("fecha").toLocalDate();
                    return new TestMapRow(hwid, fecha);
                } else {
                    return null;
                }
            } catch (SQLException e) {
                LOGGER.error("Error al procesar ResultSet: " + e.getMessage());
                throw new RuntimeException("Error al leer ResultSet", e);
            }
        }).takeWhile(Objects::nonNull);
    }

    private static void limpiarRegistrosAnterioresHWID() {
        String sql = "DELETE FROM daily_rewarded_players_hwid WHERE fecha < DATE_FORMAT(CURDATE(), '%Y-%m-01')";

        try (
                Connection connection = ConnectionPool.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)
        ) {
            int filasEliminadas = stmt.executeUpdate();
            LOGGER.info("Registros con HWID de meses anteriores eliminados: " + filasEliminadas);
        } catch (SQLException e) {
            LOGGER.error("Error al limpiar registros anteriores: " + e.getMessage());
        }

    }

    private static void limpiarRegistrosAnteriores() {
        String sql = "DELETE FROM daily_rewarded_players WHERE fecha < DATE_FORMAT(CURDATE(), '%Y-%m-01')";

        try (
                Connection connection = ConnectionPool.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)
        ) {
            int filasEliminadas = stmt.executeUpdate();
            LOGGER.info("Registros con Obj_ID de meses anteriores eliminados: " + filasEliminadas);
        } catch (SQLException e) {
            LOGGER.error("Error al limpiar registros anteriores: " + e.getMessage());
        }

    }

    public static DailyRewardManager getInstance() {
        return DailyRewardManager.SingleTonHolder._instance;
    }

    public void loadData() {
        this.setPlayersReceivdList(loadMapFromDatabaseWithStream());
        this.setHwidReceivedList(loadMapHWIDFromDatabaseWithStream());
    }

    public Map<Integer, List<LocalDate>> getPlayersReceivdList() {
        return this._playersReceivdList;
    }

    public void setPlayersReceivdList(Map<Integer, List<LocalDate>> playersReceivdList) {
        this._playersReceivdList = playersReceivdList;
    }

    public Map<String, List<LocalDate>> getHwidReceivedList() {
        return this._hwidReceivedList;
    }

    public void setHwidReceivedList(Map<String, List<LocalDate>> hwidReceivedList) {
        this._hwidReceivedList = hwidReceivedList;
    }

    public void showBoard(Player player, String file) {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/dailyreward/" + file + ".htm");
        content = content.replace("%rewards%", this.generateDailyRewardsHtml(player));
        content = content.replace("%rewardrestart%", "<br>You can get reward only once a day, when day change you can get a new reward.");
        BaseBBSManager.separateAndSend(content, player);
    }

    public String generateDailyRewardsHtml(Player player) {
        LocalDate fecha = LocalDate.now();
        int cantidadDeDiasDelMes = fecha.lengthOfMonth();
        StringBuilder sb = new StringBuilder();
        sb.append("<table bgcolor=000000 border=1>");
        int line = 0;
        sb.append("<tr>");
        int totalReward = DailyRewardData.getInstance().getAllDailyRewads().size();
        int rewardCount = 0;

        for (DailyReward dr : DailyRewardData.getInstance().getAllDailyRewads()) {
            if (dr.getDay() <= cantidadDeDiasDelMes) {
                if (line < 7) {
                    sb.append("<td align=center width=80>");
                    sb.append("<table>");
                    sb.append("<tr>");
                    sb.append("<td align=center width=72>");
                    int var10001 = dr.getDay();
                    sb.append("Day ").append(var10001).append(" - <font color=LEVEL>(").append(dr.getAmountTxt()).append(")</font>");
                    sb.append(this.getReceivedStatus(player, dr));
                    sb.append("</td>");
                    sb.append("</tr>");
                    sb.append("</table>");
                    sb.append("</td>");
                    ++line;
                    ++rewardCount;
                }

                if (line >= 7) {
                    line = 0;
                    sb.append("</tr>");
                    if (rewardCount < totalReward) {
                        sb.append("<tr>");
                    }
                }
            }
        }

        sb.append("</table>");
        return sb.toString();
    }

    public String getReceivedStatus(Player player, DailyReward dr) {
        LocalDate fechaActual = LocalDate.now();
        int diaDelMes = fechaActual.getDayOfMonth();
        if (!this.getPlayersReceivdList().containsKey(player.getObjectId())) {
            this.getPlayersReceivdList().put(player.getObjectId(), new ArrayList<>());
        }

        if (!this.getHwidReceivedList().containsKey(player.getHWID())) {
            this.getHwidReceivedList().put(player.getHWID(), new ArrayList<>());
        }

        ArrayList<Integer> diasDelMes = this.getPlayersReceivdList().values().stream().flatMap(Collection::stream).map(LocalDate::getDayOfMonth).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Integer> diasDelMesHWID = this.getHwidReceivedList().values().stream().flatMap(Collection::stream).map(LocalDate::getDayOfMonth).collect(Collectors.toCollection(ArrayList::new));
        if (diasDelMes.contains(dr.getDay()) && diasDelMesHWID.contains(dr.getDay()) && dr.getDay() <= diaDelMes) {
            return "<button  action=\"\" width=32 height=32 back=\"icon.skill0000\" fore=\"" + dr.getIcon() + "\"><font color=00ffff>(Received)</font><br>";
        } else if (diaDelMes < dr.getDay()) {
            return "<button  action=\"\" width=32 height=32 back=\"icon.skill0000\" fore=\"" + dr.getIcon() + "\"><font color=ffff00>(Soon)</font><br>";
        } else if (diaDelMes > dr.getDay()) {
            return "<button  action=\"\" width=32 height=32 back=\"icon.skill0000\" fore=\"" + dr.getIcon() + "\"><font color=ff0000>Expired</font><br>";
        } else {
            int var10000 = dr.getDay();
            return "<button  action=\"bypass bp_getDailyReward " + var10000 + "\" width=32 height=32 back=\"icon.skill0000\" fore=\"" + dr.getIcon() + "\"><font color=LEVEL>(REWARD)</font><br>";
        }
    }

    public boolean canAddDaysForPlayer(Player player) {
        if (player.getVariables().get("CanAddDaysForPlayer") == null) {
            PlayerVariables.setVar(player, "CanAddDaysForPlayer", "true", -1L);
        }

        return (player.getVariables().get("CanAddDaysForPlayer")).getValueBoolean();
    }

    public void addReward(Player player, DailyReward dr) {
        ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), dr.getItemId());
        item.setEnchantLevel(dr.getEnchantLevel());
        if (item.isStackable()) {
            player.addItem("DailyReward", dr.getItemId(), dr.getAmount(), player, true);
        } else {
            player.addItem("DailyReward", item, player, true);
        }

    }

    public void tryToGetDailyReward(Player player, DailyReward dr) {
        LocalDate fechaActual = LocalDate.now();
        int diaDelMes = fechaActual.getDayOfMonth();
        if (!(this.getPlayersReceivdList().get(player.getObjectId())).contains(fechaActual)) {
            if ((this.getHwidReceivedList().get(player.getHWID())).contains(fechaActual)) {
                player.sendMessage("You Already received this reward in another character.");
                return;
            }

            if (dr.getDay() != diaDelMes) {
                player.sendMessage("Reward not available yet!");
                return;
            }

            this.addReward(player, dr);
            player.sendMessage("Congratulations, you received Day " + dr.getDay() + " reward!");
            agregarNuevoRegistroAlMap(this.getPlayersReceivdList(), player.getObjectId(), fechaActual);
            agregarNuevoRegistroAlMapHID(this.getHwidReceivedList(), player.getHWID(), fechaActual);
        } else {
            player.sendMessage("You already received this reward! ");
        }

    }

    public void saveRewardedPlayersHWID() {
        saveMapHWIDToDatabase(this._hwidReceivedList);
    }

    public void saveRewardedPlayersObjId() {
        saveMapToDatabase(this._playersReceivdList);
    }

    public void cleanOldDatesDataBase() {
        limpiarRegistrosAnterioresHWID();
        limpiarRegistrosAnteriores();
    }

    private record TestMapRowHWID(String hwid, LocalDate fecha) {
    }

    private record TestMapRow(Integer obj_Id, LocalDate fecha) {
    }

    private static class SingleTonHolder {
        protected static final DailyRewardManager _instance = new DailyRewardManager();
    }
}
