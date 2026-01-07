package net.sf.l2j.gameserver.communitybbs.Manager;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;
import net.sf.l2j.gameserver.communitybbs.BB.Post;
import net.sf.l2j.gameserver.communitybbs.BB.Topic;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.TopicType;
import net.sf.l2j.gameserver.model.actor.Player;

import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TopicBBSManager extends BaseBBSManager {
    private final List<Topic> _topics = new ArrayList<>();

    private final Map<Forum, Integer> _maxId = new ConcurrentHashMap<>();

    private static void showNewTopic(Forum forum, Player player, int forumId) {
        if (forum == null) {
            separateAndSend("<html><body><br><br><center>The forum named '" + forumId + "' doesn't exist.</center></body></html>", player);
            return;
        }
        if (forum.getType() == 3) {
            showMemoNewTopics(forum, player);
        } else {
            separateAndSend("<html><body><br><br><center>The forum named '" + forum.getName() + "' doesn't exist.</center></body></html>", player);
        }
    }

    private static void showMemoNewTopics(Forum forum, Player player) {
        String html = "<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=0><tr><td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br1><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=20></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&$413;</td><td FIXWIDTH=540><edit var = \"Title\" width=540 height=13></td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29 valign=top>&$427;</td><td align=center FIXWIDTH=540><MultiEdit var =\"Content\" width=535 height=313></td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr></table><table fixwidth=610 border=0 cellspacing=0 cellpadding=0><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=1></td><td align=center FIXWIDTH=60 height=29>&nbsp;</td><td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Topic crea " + forum.getId() + " Title Content Title\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td><td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td><td align=center FIXWIDTH=400>&nbsp;</td><td><img src=\"l2ui.mini_logo\" width=5 height=1></td></tr></table></center></body></html>";
        send1001(html, player);
        send1002(player);
    }

    public static TopicBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, Player player) {
        if (ar1.equals("crea")) {
            Forum forum = ForumsBBSManager.getInstance().getForumByID(Integer.parseInt(ar2));
            if (forum == null) {
                separateAndSend("<html><body><br><br><center>The forum named '" + ar2 + "' doesn't exist.</center></body></html>", player);
                return;
            }
            forum.vload();
            Topic topic = new Topic(TopicType.CREATE, getInstance().getMaxID(forum) + 1, Integer.parseInt(ar2), ar5, Calendar.getInstance().getTimeInMillis(), player.getName(), player.getObjectId(), 1, 0);
            forum.addTopic(topic);
            getInstance().setMaxID(topic.getID(), forum);
            Post post = new Post(player.getName(), player.getObjectId(), Calendar.getInstance().getTimeInMillis(), topic.getID(), forum.getId(), ar4);
            PostBBSManager.getInstance().addPostByTopic(post, topic);
            parseCmd("_bbsmemo", player);
        } else if (ar1.equals("del")) {
            Forum forum = ForumsBBSManager.getInstance().getForumByID(Integer.parseInt(ar2));
            if (forum == null) {
                separateAndSend("<html><body><br><br><center>The forum named '" + ar2 + "' doesn't exist.</center></body></html>", player);
                return;
            }
            Topic topic = forum.getTopic(Integer.parseInt(ar3));
            if (topic == null) {
                separateAndSend("<html><body><br><br><center>The topic named '" + ar3 + "' doesn't exist.</center></body></html>", player);
                return;
            }
            Post post = PostBBSManager.getInstance().getPostByTopic(topic);
            if (post != null)
                post.deleteMe(topic);
            topic.deleteMe(forum);
            parseCmd("_bbsmemo", player);
        } else {
            super.parseWrite(ar1, ar2, ar3, ar4, ar5, player);
        }
    }

    public void parseCmd(String command, Player player) {
        if (command.equals("_bbsmemo")) {
            Forum forum = player.getMemo();
            if (forum != null)
                showTopics(forum, player, 1, forum.getId());
        } else if (command.startsWith("_bbstopics;read")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            int forumId = Integer.parseInt(st.nextToken());
            String index = st.hasMoreTokens() ? st.nextToken() : null;
            int ind = (index == null) ? 1 : Integer.parseInt(index);
            showTopics(ForumsBBSManager.getInstance().getForumByID(forumId), player, ind, forumId);
        } else if (command.startsWith("_bbstopics;crea")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            int forumId = Integer.parseInt(st.nextToken());
            showNewTopic(ForumsBBSManager.getInstance().getForumByID(forumId), player, forumId);
        } else if (command.startsWith("_bbstopics;del")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            int forumId = Integer.parseInt(st.nextToken());
            int topicId = Integer.parseInt(st.nextToken());
            Forum forum = ForumsBBSManager.getInstance().getForumByID(forumId);
            if (forum == null) {
                separateAndSend("<html><body><br><br><center>The forum named '" + forumId + "' doesn't exist.</center></body></html>", player);
                return;
            }
            Topic topic = forum.getTopic(topicId);
            if (topic == null) {
                separateAndSend("<html><body><br><br><center>The topic named '" + topicId + "' doesn't exist.</center></body></html>", player);
                return;
            }
            Post post = PostBBSManager.getInstance().getPostByTopic(topic);
            if (post != null)
                post.deleteMe(topic);
            topic.deleteMe(forum);
            parseCmd("_bbsmemo", player);
        } else {
            super.parseCmd(command, player);
        }
    }

    public void addTopic(Topic topic) {
        this._topics.add(topic);
    }

    public void deleteTopic(Topic topic) {
        this._topics.remove(topic);
    }

    public void setMaxID(int id, Forum forum) {
        this._maxId.put(forum, id);
    }

    public int getMaxID(Forum forum) {
        return this._maxId.getOrDefault(forum, 0);
    }

    public Topic getTopicById(int forumId) {
        return this._topics.stream().filter(t -> (t.getID() == forumId)).findFirst().orElse(null);
    }

    private void showTopics(Forum forum, Player player, int index, int forumId) {
        if (forum == null) {
            separateAndSend("<html><body><br><br><center>The forum named '" + forumId + "' doesn't exist.</center></body></html>", player);
            return;
        }
        if (forum.getType() == 3) {
            showMemoTopics(forum, player, index);
        } else {
            separateAndSend("<html><body><br><br><center>The forum named '" + forum.getName() + "' doesn't exist.</center></body></html>", player);
        }
    }

    private void showMemoTopics(Forum forum, Player player, int index) {
        forum.vload();
        StringBuilder sb = new StringBuilder("<html><body><br><br><table border=0 width=610><tr><td width=10></td><td width=600 align=left><a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a></td></tr></table><img src=\"L2UI.squareblank\" width=\"1\" height=\"10\"><center><table border=0 cellspacing=0 cellpadding=2 bgcolor=888888 width=610><tr><td FIXWIDTH=5></td><td FIXWIDTH=415 align=center>&$413;</td><td FIXWIDTH=120 align=center></td><td FIXWIDTH=70 align=center>&$418;</td></tr></table>");
        DateFormat dateFormat = DateFormat.getInstance();
        for (int i = 0, j = getMaxID(forum) + 1; i < 12 * index; j--) {
            if (j < 0)
                break;
            Topic topic = forum.getTopic(j);
            if (topic != null)
                if (i++ >= 12 * (index - 1))
                    StringUtil.append(sb, "<table border=0 cellspacing=0 cellpadding=5 WIDTH=610><tr><td FIXWIDTH=5></td><td FIXWIDTH=415><a action=\"bypass _bbsposts;read;", forum.getId(), ";", topic.getID(), "\">", topic.getName(), "</a></td><td FIXWIDTH=120 align=center></td><td FIXWIDTH=70 align=center>", dateFormat.format(new Date(topic.getDate())), "</td></tr></table><img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">");
        }
        sb.append("<br><table width=610 cellspace=0 cellpadding=0><tr><td width=50><button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td><td width=510 align=center><table border=0><tr>");
        if (index == 1) {
            sb.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
        } else {
            StringUtil.append(sb, "<td><button action=\"bypass _bbstopics;read;", forum.getId(), ";", index - 1, "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
        }
        int pageNumber = forum.getTopicSize() / 8;
        if (pageNumber * 8 != ClanTable.getInstance().getClans().size())
            pageNumber++;
        for (int k = 1; k <= pageNumber; k++) {
            if (k == index) {
                StringUtil.append(sb, "<td> ", k, " </td>");
            } else {
                StringUtil.append(sb, "<td><a action=\"bypass _bbstopics;read;", forum.getId(), ";", k, "\"> ", k, " </a></td>");
            }
        }
        if (index == pageNumber) {
            sb.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
        } else {
            StringUtil.append(sb, "<td><button action=\"bypass _bbstopics;read;", forum.getId(), ";", index + 1, "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
        }
        StringUtil.append(sb, "</tr></table></td><td align=right><button value = \"&$421;\" action=\"bypass _bbstopics;crea;", forum.getId(), "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td></tr><tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr><tr><td></td><td align=center><table border=0><tr><td></td><td><edit var = \"Search\" width=130 height=11></td><td><button value=\"&$420;\" action=\"Write 5 -2 0 Search _ _\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table></td></tr></table><br><br><br></center></body></html>");
        separateAndSend(sb.toString(), player);
    }

    private static class SingletonHolder {
        protected static final TopicBBSManager INSTANCE = new TopicBBSManager();
    }
}
