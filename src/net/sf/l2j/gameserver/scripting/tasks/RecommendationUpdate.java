package net.sf.l2j.gameserver.scripting.tasks;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.scripting.ScheduledQuest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class RecommendationUpdate extends ScheduledQuest {
    private static final String DELETE_CHAR_RECOMS = "TRUNCATE TABLE character_recommends";

    private static final String SELECT_ALL_RECOMS = "SELECT obj_Id, level, rec_have FROM characters";

    private static final String UPDATE_ALL_RECOMS = "UPDATE characters SET rec_left=?, rec_have=? WHERE obj_Id=?";

    public RecommendationUpdate() {
        super(-1, "tasks");
    }

    public void onStart() {
        for (Player player : World.getInstance().getPlayers()) {
            player.getRecomChars().clear();
            int level = player.getLevel();
            if (level < 20) {
                player.setRecomLeft(3);
                player.editRecomHave(-1);
            } else if (level < 40) {
                player.setRecomLeft(6);
                player.editRecomHave(-2);
            } else {
                player.setRecomLeft(9);
                player.editRecomHave(-3);
            }
            player.sendPacket(new UserInfo(player));
        }
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("TRUNCATE TABLE character_recommends");
                try {
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
                PreparedStatement ps2 = con.prepareStatement("UPDATE characters SET rec_left=?, rec_have=? WHERE obj_Id=?");
                try {
                    PreparedStatement preparedStatement = con.prepareStatement("SELECT obj_Id, level, rec_have FROM characters");
                    try {
                        ResultSet rs = preparedStatement.executeQuery();
                        try {
                            while (rs.next()) {
                                int level = rs.getInt("level");
                                if (level < 20) {
                                    ps2.setInt(1, 3);
                                    ps2.setInt(2, Math.max(0, rs.getInt("rec_have") - 1));
                                } else if (level < 40) {
                                    ps2.setInt(1, 6);
                                    ps2.setInt(2, Math.max(0, rs.getInt("rec_have") - 2));
                                } else {
                                    ps2.setInt(1, 9);
                                    ps2.setInt(2, Math.max(0, rs.getInt("rec_have") - 3));
                                }
                                ps2.setInt(3, rs.getInt("obj_Id"));
                                ps2.addBatch();
                            }
                            if (rs != null)
                                rs.close();
                        } catch (Throwable throwable) {
                            if (rs != null)
                                try {
                                    rs.close();
                                } catch (Throwable throwable1) {
                                    throwable.addSuppressed(throwable1);
                                }
                            throw throwable;
                        }
                        if (preparedStatement != null)
                            preparedStatement.close();
                    } catch (Throwable throwable) {
                        if (preparedStatement != null)
                            try {
                                preparedStatement.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    ps2.executeBatch();
                    if (ps2 != null)
                        ps2.close();
                } catch (Throwable throwable) {
                    if (ps2 != null)
                        try {
                            ps2.close();
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
            LOGGER.error("Couldn't clear players recommendations.", e);
        }
    }

    public void onEnd() {
    }
}
