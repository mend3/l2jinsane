package net.sf.l2j.gameserver.communitybbs.BB;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.communitybbs.Manager.TopicBBSManager;
import net.sf.l2j.gameserver.enums.TopicType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Forum {
    public static final int ROOT = 0;
    public static final int NORMAL = 1;
    public static final int CLAN = 2;
    public static final int MEMO = 3;
    public static final int MAIL = 4;
    public static final int INVISIBLE = 0;
    public static final int ALL = 1;
    public static final int CLANMEMBERONLY = 2;
    public static final int OWNERONLY = 3;
    private static final CLogger LOGGER = new CLogger(Forum.class.getName());
    private static final String RESTORE_FORUMS = "SELECT * FROM forums WHERE forum_id=?";
    private static final String RESTORE_TOPICS = "SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC";
    private static final String RESTORE_CHILDREN = "SELECT forum_id FROM forums WHERE forum_parent=?";
    private static final String ADD_FORUM = "INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) values (?,?,?,?,?,?,?)";
    private final List<Forum> _children = new ArrayList<>();

    private final Map<Integer, Topic> _topics = new HashMap<>();

    private final int _forumId;
    private final Forum _parent;
    private String _forumName;
    private int _forumType;
    private int _forumPost;
    private int _forumPerm;
    private int _ownerId;

    private boolean _loaded = false;

    public Forum(int forumId, Forum parent) {
        this._forumId = forumId;
        this._parent = parent;
    }

    public Forum(String name, Forum parent, int type, int perm, int ownerId) {
        this._forumName = name;
        this._forumId = ForumsBBSManager.getInstance().getANewID();
        this._forumType = type;
        this._forumPost = 0;
        this._forumPerm = perm;
        this._parent = parent;
        this._ownerId = ownerId;
        parent._children.add(this);
        ForumsBBSManager.getInstance().addForum(this);
        this._loaded = true;
    }

    private void load() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM forums WHERE forum_id=?");
                try {
                    PreparedStatement ps2 = con.prepareStatement("SELECT * FROM topic WHERE topic_forum_id=? ORDER BY topic_id DESC");
                    try {
                        ps.setInt(1, this._forumId);
                        ResultSet rs = ps.executeQuery();
                        try {
                            if (rs.next()) {
                                this._forumName = rs.getString("forum_name");
                                this._forumPost = rs.getInt("forum_post");
                                this._forumType = rs.getInt("forum_type");
                                this._forumPerm = rs.getInt("forum_perm");
                                this._ownerId = rs.getInt("forum_owner_id");
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
                        ps2.setInt(1, this._forumId);
                        ResultSet rs2 = ps2.executeQuery();
                        try {
                            while (rs2.next()) {
                                Topic topic = new Topic(TopicType.RESTORE, rs2.getInt("topic_id"), rs2.getInt("topic_forum_id"), rs2.getString("topic_name"), rs2.getLong("topic_date"), rs2.getString("topic_ownername"), rs2.getInt("topic_ownerid"), rs2.getInt("topic_type"), rs2.getInt("topic_reply"));
                                this._topics.put(Integer.valueOf(topic.getID()), topic);
                                if (topic.getID() > TopicBBSManager.getInstance().getMaxID(this))
                                    TopicBBSManager.getInstance().setMaxID(topic.getID(), this);
                            }
                            if (rs2 != null)
                                rs2.close();
                        } catch (Throwable throwable) {
                            if (rs2 != null)
                                try {
                                    rs2.close();
                                } catch (Throwable throwable1) {
                                    throwable.addSuppressed(throwable1);
                                }
                            throw throwable;
                        }
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
            LOGGER.error("Couldn't load forums with id {}.", e, Integer.valueOf(this._forumId));
        }
    }

    private void getChildren() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT forum_id FROM forums WHERE forum_parent=?");
                try {
                    ps.setInt(1, this._forumId);
                    ResultSet result = ps.executeQuery();
                    try {
                        while (result.next()) {
                            Forum forum = new Forum(result.getInt("forum_id"), this);
                            this._children.add(forum);
                            ForumsBBSManager.getInstance().addForum(forum);
                        }
                        if (result != null)
                            result.close();
                    } catch (Throwable throwable) {
                        if (result != null)
                            try {
                                result.close();
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
            LOGGER.error("Couldn't load children forum for parentId {}.", e, Integer.valueOf(this._forumId));
        }
    }

    public int getTopicSize() {
        vload();
        return this._topics.size();
    }

    public Topic getTopic(int id) {
        vload();
        return this._topics.get(Integer.valueOf(id));
    }

    public void addTopic(Topic topic) {
        vload();
        this._topics.put(Integer.valueOf(topic.getID()), topic);
    }

    public int getId() {
        return this._forumId;
    }

    public String getName() {
        vload();
        return this._forumName;
    }

    public int getType() {
        vload();
        return this._forumType;
    }

    public Forum getChildByName(String name) {
        vload();
        return this._children.stream().filter(f -> f.getName().equals(name)).findFirst().orElse(null);
    }

    public void removeTopic(int id) {
        this._topics.remove(Integer.valueOf(id));
    }

    public void insertIntoDb() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO forums (forum_id,forum_name,forum_parent,forum_post,forum_type,forum_perm,forum_owner_id) values (?,?,?,?,?,?,?)");
                try {
                    ps.setInt(1, this._forumId);
                    ps.setString(2, this._forumName);
                    ps.setInt(3, this._parent.getId());
                    ps.setInt(4, this._forumPost);
                    ps.setInt(5, this._forumType);
                    ps.setInt(6, this._forumPerm);
                    ps.setInt(7, this._ownerId);
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
            LOGGER.error("Couldn't save new Forum.", e);
        }
    }

    public void vload() {
        if (!this._loaded) {
            load();
            getChildren();
            this._loaded = true;
        }
    }
}
