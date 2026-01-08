/**/
package net.sf.l2j.gameserver.data.sql;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.Bookmark;
import net.sf.l2j.gameserver.model.actor.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class BookmarkTable {
    private static final CLogger LOGGER = new CLogger(BookmarkTable.class.getName());
    private final List<Bookmark> _bks = new ArrayList<>();

    protected BookmarkTable() {
    }

    public static BookmarkTable getInstance() {
        return BookmarkTable.SingletonHolder.INSTANCE;
    }

    public void load() {

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM bookmarks");

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            this._bks.add(new Bookmark(rs.getString("name"), rs.getInt("obj_Id"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
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
            LOGGER.error("Couldn't restore bookmarks.", var12);
        }

        LOGGER.info("Loaded {} bookmarks.", this._bks.size());
    }

    public boolean isExisting(String name, int objId) {
        return this.getBookmark(name, objId) != null;
    }

    public Bookmark getBookmark(String name, int objId) {
        Iterator<Bookmark> var3 = this._bks.iterator();

        Bookmark bk;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            bk = var3.next();
        } while (!bk.getName().equalsIgnoreCase(name) || bk.getId() != objId);

        return bk;
    }

    public List<Bookmark> getBookmarks(int objId) {
        return this._bks.stream().filter((bk) -> bk.getId() == objId).collect(Collectors.toList());
    }

    public void saveBookmark(String name, Player player) {
        int objId = player.getObjectId();
        int x = player.getX();
        int y = player.getY();
        int z = player.getZ();
        this._bks.add(new Bookmark(name, objId, x, y, z));

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO bookmarks (name, obj_Id, x, y, z) values (?,?,?,?,?)");

                try {
                    ps.setString(1, name);
                    ps.setInt(2, objId);
                    ps.setInt(3, x);
                    ps.setInt(4, y);
                    ps.setInt(5, z);
                    ps.execute();
                } catch (Throwable var13) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var12) {
                            var13.addSuppressed(var12);
                        }
                    }

                    throw var13;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var14) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var11) {
                        var14.addSuppressed(var11);
                    }
                }

                throw var14;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var15) {
            LOGGER.error("Couldn't save bookmark.", var15);
        }

    }

    public void deleteBookmark(String name, int objId) {
        Bookmark bookmark = this.getBookmark(name, objId);
        if (bookmark != null) {
            this._bks.remove(bookmark);

            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM bookmarks WHERE name=? AND obj_Id=?");

                    try {
                        ps.setString(1, name);
                        ps.setInt(2, objId);
                        ps.execute();
                    } catch (Throwable var10) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var9) {
                                var10.addSuppressed(var9);
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
                        } catch (Throwable var8) {
                            var11.addSuppressed(var8);
                        }
                    }

                    throw var11;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var12) {
                LOGGER.error("Couldn't delete bookmark.", var12);
            }
        }

    }

    private static class SingletonHolder {
        protected static final BookmarkTable INSTANCE = new BookmarkTable();
    }
}