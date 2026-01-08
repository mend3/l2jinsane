/**/
package net.sf.l2j.gameserver.communitybbs.BB;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.communitybbs.Manager.PostBBSManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Post {
    private static final CLogger LOGGER = new CLogger(Post.class.getName());
    private static final String RESTORE_POSTS = "SELECT * FROM posts WHERE post_forum_id=? AND post_topic_id=? ORDER BY post_id ASC";
    private static final String ADD_POST = "INSERT INTO posts (post_id,post_owner_name,post_ownerid,post_date,post_topic_id,post_forum_id,post_txt) values (?,?,?,?,?,?,?)";
    private static final String DELETE_POST = "DELETE FROM posts WHERE post_forum_id=? AND post_topic_id=?";
    private static final String UPDATE_TEXT = "UPDATE posts SET post_txt=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?";
    private final List<Post.CPost> _posts = new ArrayList<>();

    public Post(Topic topic) {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM posts WHERE post_forum_id=? AND post_topic_id=? ORDER BY post_id ASC");

                try {
                    ps.setInt(1, topic.getForumID());
                    ps.setInt(2, topic.getID());
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            this._posts.add(new CPost(this, rs.getInt("post_id"), rs.getString("post_owner_name"), rs.getInt("post_ownerid"), rs.getLong("post_date"), rs.getInt("post_topic_id"), rs.getInt("post_forum_id"), rs.getString("post_txt")));
                        }
                    } catch (Throwable var10) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var9) {
                                var10.addSuppressed(var9);
                            }
                        }

                        throw var10;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var11) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var8) {
                            var11.addSuppressed(var8);
                        }
                    }

                    throw var11;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var12) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var7) {
                        var12.addSuppressed(var7);
                    }
                }

                throw var12;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var13) {
            LOGGER.error("Couldn't load posts for {} / {}.", var13, topic.getForumID(), topic.getID());
        }

    }

    public Post(String owner, int ownerId, long date, int topicId, int forumId, String text) {
        Post.CPost post = new CPost(this, 0, owner, ownerId, date, topicId, forumId, text);
        this._posts.add(post);

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO posts (post_id,post_owner_name,post_ownerid,post_date,post_topic_id,post_forum_id,post_txt) values (?,?,?,?,?,?,?)");

                try {
                    ps.setInt(1, 0);
                    ps.setString(2, owner);
                    ps.setInt(3, ownerId);
                    ps.setLong(4, date);
                    ps.setInt(5, topicId);
                    ps.setInt(6, forumId);
                    ps.setString(7, text);
                    ps.execute();
                } catch (Throwable var15) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var14) {
                            var15.addSuppressed(var14);
                        }
                    }

                    throw var15;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var16) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var13) {
                        var16.addSuppressed(var13);
                    }
                }

                throw var16;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var17) {
            LOGGER.error("Couldn't save new Post.", var17);
        }

    }

    public Post.CPost getCPost(int id) {
        int i = 0;
        Iterator<CPost> var3 = this._posts.iterator();

        Post.CPost cp;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            cp = var3.next();
        } while (i++ != id);

        return cp;
    }

    public void deleteMe(Topic topic) {
        PostBBSManager.getInstance().deletePostByTopic(topic);

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM posts WHERE post_forum_id=? AND post_topic_id=?");

                try {
                    ps.setInt(1, topic.getForumID());
                    ps.setInt(2, topic.getID());
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
            LOGGER.error("Couldn't delete Post.", var10);
        }

    }

    public void updateText(int index) {
        Post.CPost post = this.getCPost(index);
        if (post != null) {
            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("UPDATE posts SET post_txt=? WHERE post_id=? AND post_topic_id=? AND post_forum_id=?");

                    try {
                        ps.setString(1, post.getText());
                        ps.setInt(2, post.getId());
                        ps.setInt(3, post.getTopicId());
                        ps.setInt(4, post.getForumId());
                        ps.execute();
                    } catch (Throwable var9) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } catch (Throwable var10) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var11) {
                LOGGER.error("Couldn't update Post text.", var11);
            }

        }
    }

    public static class CPost {
        private final int _id;
        private final String _owner;
        private final int _ownerId;
        private final long _date;
        private final int _topicId;
        private final int _forumId;
        private String _text;

        public CPost(final Post param1, int param2, String param3, int param4, long param5, int param7, int param8, String param9) {
            this._id = param2;
            this._owner = param3;
            this._ownerId = param4;
            this._date = param5;
            this._topicId = param7;
            this._forumId = param8;
            this._text = param9;
        }

        public int getId() {
            return this._id;
        }

        public String getOwner() {
            return this._owner;
        }

        public int getOwnerId() {
            return this._ownerId;
        }

        public long getDate() {
            return this._date;
        }

        public int getTopicId() {
            return this._topicId;
        }

        public int getForumId() {
            return this._forumId;
        }

        public String getText() {
            return this._text;
        }

        public void setText(String text) {
            this._text = text;
        }
    }
}