package net.sf.l2j.gameserver.communitybbs.Manager;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ForumsBBSManager extends BaseBBSManager {
    private static final String LOAD_FORUMS = "SELECT forum_id FROM forums WHERE forum_type=0";

    private final Set<Forum> _forums = ConcurrentHashMap.newKeySet();

    private int _lastId = 1;

    public static ForumsBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_type=0");
                try {
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next())
                            addForum(new Forum(rs.getInt("forum_id"), null));
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
            LOGGER.error("Couldn't load forums root.", e);
        }
    }

    public void initRoot() {
        for (Forum forum : this._forums)
            forum.vload();
        LOGGER.info("Loaded {} forums.", Integer.valueOf(this._forums.size()));
    }

    public void addForum(Forum forum) {
        if (forum == null)
            return;
        this._forums.add(forum);
        if (forum.getId() > this._lastId)
            this._lastId = forum.getId();
    }

    public Forum createNewForum(String name, Forum parent, int type, int perm, int oid) {
        Forum forum = new Forum(name, parent, type, perm, oid);
        forum.insertIntoDb();
        return forum;
    }

    public int getANewID() {
        return ++this._lastId;
    }

    public Forum getForumByName(String name) {
        return this._forums.stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
    }

    public Forum getForumByID(int id) {
        return this._forums.stream().filter(f -> (f.getId() == id)).findFirst().orElse(null);
    }

    private static class SingletonHolder {
        protected static final ForumsBBSManager INSTANCE = new ForumsBBSManager();
    }
}
