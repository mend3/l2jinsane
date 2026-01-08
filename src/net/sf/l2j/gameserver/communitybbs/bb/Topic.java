package net.sf.l2j.gameserver.communitybbs.bb;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.communitybbs.manager.TopicBBSManager;
import net.sf.l2j.gameserver.enums.TopicType;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class Topic {
    public static final int MORMAL = 0;
    public static final int MEMO = 1;
    private static final CLogger LOGGER = new CLogger(Topic.class.getName());
    private static final String INSERT_TOPIC = "INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)";
    private static final String DELETE_TOPIC = "DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?";
    private final int _id;

    private final int _forumId;

    private final String _topicName;

    private final long _date;

    private final String _ownerName;

    private final int _ownerId;

    private final int _type;

    private final int _cReply;

    public Topic(TopicType constructorType, int id, int forumId, String name, long date, String ownerName, int ownerId, int type, int Creply) {
        this._id = id;
        this._forumId = forumId;
        this._topicName = name;
        this._date = date;
        this._ownerName = ownerName;
        this._ownerId = ownerId;
        this._type = type;
        this._cReply = Creply;
        TopicBBSManager.getInstance().addTopic(this);
        if (constructorType == TopicType.CREATE)
            insertIntoDb();
    }

    public int getID() {
        return this._id;
    }

    public int getForumID() {
        return this._forumId;
    }

    public String getName() {
        return this._topicName;
    }

    public String getOwnerName() {
        return this._ownerName;
    }

    public long getDate() {
        return this._date;
    }

    private void insertIntoDb() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)");
                try {
                    ps.setInt(1, this._id);
                    ps.setInt(2, this._forumId);
                    ps.setString(3, this._topicName);
                    ps.setLong(4, this._date);
                    ps.setString(5, this._ownerName);
                    ps.setInt(6, this._ownerId);
                    ps.setInt(7, this._type);
                    ps.setInt(8, this._cReply);
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
            LOGGER.error("Couldn't save new topic.", e);
        }
    }

    public void deleteMe(Forum forum) {
        TopicBBSManager.getInstance().deleteTopic(this);
        forum.removeTopic(this._id);
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?");
                try {
                    ps.setInt(1, this._id);
                    ps.setInt(2, forum.getId());
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
            LOGGER.error("Couldn't delete topic.", e);
        }
    }
}
