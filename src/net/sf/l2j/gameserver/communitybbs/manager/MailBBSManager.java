/**/
package net.sf.l2j.gameserver.communitybbs.manager;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.player.BlockList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExMailArrived;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MailBBSManager extends BaseBBSManager {
    private static final String SELECT_CHAR_MAILS = "SELECT * FROM character_mail WHERE charId = ? ORDER BY letterId ASC";
    private static final String INSERT_NEW_MAIL = "INSERT INTO character_mail (charId, letterId, senderId, location, recipientNames, subject, message, sentDate, unread) VALUES (?,?,?,?,?,?,?,?,?)";
    private static final String DELETE_MAIL = "DELETE FROM character_mail WHERE letterId = ?";
    private static final String MARK_MAIL_READ = "UPDATE character_mail SET unread = 0 WHERE letterId = ?";
    private static final String SET_MAIL_LOC = "UPDATE character_mail SET location = ? WHERE letterId = ?";
    private static final String SELECT_LAST_ID = "SELECT letterId FROM character_mail ORDER BY letterId DESC LIMIT 1";
    private static final String GET_GM_STATUS = "SELECT accesslevel FROM characters WHERE obj_Id = ?";
    private final Map<Integer, Set<MailBBSManager.Mail>> _mails = new ConcurrentHashMap<>();
    private int _lastid = 0;

    protected MailBBSManager() {
        this.initId();
    }

    private static String abbreviate(String s, int maxWidth) {
        return s.length() > maxWidth ? s.substring(0, maxWidth) : s;
    }

    private static void showWriteView(Player player) {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/mail/mail-write.htm");
        separateAndSend(content, player);
    }

    private static void showWriteView(Player player, MailBBSManager.Mail mail) {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/mail/mail-reply.htm");
        String var10000 = mail.location.getBypass();
        String link = var10000 + "&nbsp;&gt;&nbsp;<a action=\"bypass _bbsmail;view;" + mail.mailId + "\">" + mail.subject + "</a>&nbsp;&gt;&nbsp;";
        content = content.replace("%maillink%", link);
        content = content.replace("%recipients%", mail.senderId == player.getObjectId() ? mail.recipientNames : getPlayerName(mail.senderId));
        content = content.replace("%letterId%", String.valueOf(mail.mailId));
        send1001(content, player);
        send1002(player, " ", "Re: " + mail.subject, "0");
    }

    private static boolean isBlocked(Player player, int objectId) {
        Iterator<Player> var2 = World.getInstance().getPlayers().iterator();

        Player playerToTest;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            playerToTest = var2.next();
        } while (playerToTest.getObjectId() != objectId);

        return BlockList.isInBlockList(playerToTest, player);
    }

    private static String getPlayerName(int objectId) {
        String name = PlayerInfoTable.getInstance().getPlayerName(objectId);
        return name == null ? "Unknown" : name;
    }

    private static boolean isGM(int objectId) {
        boolean isGM = false;

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT accesslevel FROM characters WHERE obj_Id = ?");

                try {
                    ps.setInt(1, objectId);
                    ResultSet rs = ps.executeQuery();

                    try {
                        if (rs.next()) {
                            isGM = rs.getInt(1) > 0;
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
            LOGGER.error("Couldn't verify GM access for {}.", var13, objectId);
        }

        return isGM;
    }

    private static int getPagesCount(int mailCount) {
        if (mailCount < 1) {
            return 1;
        } else {
            return mailCount % 10 == 0 ? mailCount / 10 : mailCount / 10 + 1;
        }
    }

    public static MailBBSManager getInstance() {
        return MailBBSManager.SingletonHolder.INSTANCE;
    }

    public void parseCmd(String command, Player player) {
        if (!command.equals("_bbsmail") && !command.equals("_maillist_0_1_0_")) {
            if (command.startsWith("_bbsmail")) {
                StringTokenizer st = new StringTokenizer(command, ";");
                st.nextToken();
                String action = st.nextToken();
                if (!action.equals("inbox") && !action.equals("sentbox") && !action.equals("archive") && !action.equals("temparchive")) {
                    if (action.equals("crea")) {
                        showWriteView(player);
                    } else {
                        MailBBSManager.Mail mail = this.getMail(player, st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : -1);
                        if (mail == null) {
                            this.showLastForum(player);
                            return;
                        }

                        switch (action) {
                            case "view" -> {
                                this.showMailView(player, mail);
                                if (mail.unread) {
                                    this.setMailToRead(player, mail.mailId);
                                }
                            }
                            case "reply" -> showWriteView(player, mail);
                            case "del" -> {
                                this.deleteMail(player, mail.mailId);
                                this.showLastForum(player);
                            }
                            case "store" -> {
                                this.setMailLocation(player, mail.mailId, MailType.ARCHIVE);
                                this.showMailList(player, 1, MailType.ARCHIVE);
                            }
                        }
                    }
                } else {
                    int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
                    String sType = st.hasMoreTokens() ? st.nextToken() : "";
                    String search = st.hasMoreTokens() ? st.nextToken() : "";
                    this.showMailList(player, page, Enum.valueOf(MailType.class, action.toUpperCase()), sType, search);
                }
            } else {
                super.parseCmd(command, player);
            }
        } else {
            this.showMailList(player, 1, MailBBSManager.MailType.INBOX);
        }

    }

    public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, Player player) {
        if (ar1.equals("Send")) {
            this.sendMail(ar3, ar4, ar5, player);
            this.showMailList(player, 1, MailBBSManager.MailType.SENTBOX);
        } else if (ar1.startsWith("Search")) {
            StringTokenizer st = new StringTokenizer(ar1, ";");
            st.nextToken();
            this.showMailList(player, 1, Enum.valueOf(MailType.class, st.nextToken().toUpperCase()), ar4, ar5);
        } else {
            super.parseWrite(ar1, ar2, ar3, ar4, ar5, player);
        }

    }

    private void initId() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT letterId FROM character_mail ORDER BY letterId DESC LIMIT 1");

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            if (rs.getInt(1) > this._lastid) {
                                this._lastid = rs.getInt(1);
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
            LOGGER.error("Couldn't find the last mail id.", var12);
        }

    }

    private synchronized int getNewMailId() {
        return ++this._lastid;
    }

    private Set<MailBBSManager.Mail> getPlayerMails(int objectId) {
        Set<MailBBSManager.Mail> mails = this._mails.get(objectId);
        if (mails == null) {
            mails = ConcurrentHashMap.newKeySet();

            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM character_mail WHERE charId = ? ORDER BY letterId ASC");

                    try {
                        ps.setInt(1, objectId);
                        ResultSet rs = ps.executeQuery();

                        try {
                            while (rs.next()) {
                                MailBBSManager.Mail mail = new Mail(this);
                                mail.charId = rs.getInt("charId");
                                mail.mailId = rs.getInt("letterId");
                                mail.senderId = rs.getInt("senderId");
                                mail.location = Enum.valueOf(MailType.class, rs.getString("location").toUpperCase());
                                mail.recipientNames = rs.getString("recipientNames");
                                mail.subject = rs.getString("subject");
                                mail.message = rs.getString("message");
                                mail.sentDate = rs.getTimestamp("sentDate");
                                mail.sentDateString = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(mail.sentDate);
                                mail.unread = rs.getInt("unread") != 0;
                                mails.add(mail);
                            }
                        } catch (Throwable var11) {
                            if (rs != null) {
                                try {
                                    rs.close();
                                } catch (Throwable var10) {
                                    var11.addSuppressed(var10);
                                }
                            }

                            throw var11;
                        }

                        if (rs != null) {
                            rs.close();
                        }
                    } catch (Throwable var12) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var9) {
                                var12.addSuppressed(var9);
                            }
                        }

                        throw var12;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } catch (Throwable var13) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var8) {
                            var13.addSuppressed(var8);
                        }
                    }

                    throw var13;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var14) {
                LOGGER.error("Couldn't load mail for player id : {}.", var14, objectId);
            }

            this._mails.put(objectId, mails);
        }

        return mails;
    }

    private MailBBSManager.Mail getMail(Player player, int mailId) {
        return this.getPlayerMails(player.getObjectId()).stream().filter((l) -> l.mailId == mailId).findFirst().orElse(null);
    }

    public int checkUnreadMail(Player player) {
        return (int) this.getPlayerMails(player.getObjectId()).stream().filter((l) -> l.unread).count();
    }

    private void showMailList(Player player, int page, MailBBSManager.MailType type) {
        this.showMailList(player, page, type, "", "");
    }

    private void showMailList(Player player, int page, MailBBSManager.MailType type, String sType, String search) {
        Set<Mail> mails;
        if (!sType.isEmpty() && !search.isEmpty()) {
            mails = ConcurrentHashMap.newKeySet();
            boolean byTitle = sType.equalsIgnoreCase("title");
            Iterator<Mail> var8 = this.getPlayerMails(player.getObjectId()).iterator();

            label169:
            while (true) {
                while (true) {
                    if (!var8.hasNext()) {
                        break label169;
                    }

                    MailBBSManager.Mail mail = var8.next();
                    if (byTitle && mail.subject.toLowerCase().contains(search.toLowerCase())) {
                        mails.add(mail);
                    } else if (!byTitle) {
                        String writer = getPlayerName(mail.senderId);
                        if (writer.toLowerCase().contains(search.toLowerCase())) {
                            mails.add(mail);
                        }
                    }
                }
            }
        } else {
            mails = this.getPlayerMails(player.getObjectId());
        }

        int countMails = this.getMailCount(player.getObjectId(), type, sType, search);
        int maxpage = getPagesCount(countMails);
        if (page > maxpage) {
            page = maxpage;
        }

        if (page < 1) {
            page = 1;
        }

        player.setMailPosition(page);
        int index = 0;
        int maxIndex = page == 1 ? page * 9 : page * 10 - 1;
        int minIndex = maxIndex - 9;
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/mail/mail.htm");
        content = content.replace("%inbox%", Integer.toString(this.getMailCount(player.getObjectId(), MailBBSManager.MailType.INBOX, "", "")));
        content = content.replace("%sentbox%", Integer.toString(this.getMailCount(player.getObjectId(), MailBBSManager.MailType.SENTBOX, "", "")));
        content = content.replace("%archive%", Integer.toString(this.getMailCount(player.getObjectId(), MailBBSManager.MailType.ARCHIVE, "", "")));
        content = content.replace("%temparchive%", Integer.toString(this.getMailCount(player.getObjectId(), MailBBSManager.MailType.TEMPARCHIVE, "", "")));
        content = content.replace("%type%", type.getDescription());
        content = content.replace("%htype%", type.toString().toLowerCase());
        StringBuilder sb = new StringBuilder();

        for (Object o : mails) {
            Mail mail = (Mail) o;
            if (mail.location.equals(type)) {
                if (index < minIndex) {
                    ++index;
                } else {
                    if (index > maxIndex) {
                        break;
                    }

                    StringUtil.append(sb, "<table width=610><tr><td width=5></td><td width=150>", getPlayerName(mail.senderId), "</td><td width=300><a action=\"bypass _bbsmail;view;", mail.mailId, "\">");
                    if (mail.unread) {
                        sb.append("<font color=\"LEVEL\">");
                    }

                    sb.append(abbreviate(mail.subject, 51));
                    if (mail.unread) {
                        sb.append("</font>");
                    }

                    StringUtil.append(sb, "</a></td><td width=150>", mail.sentDateString, "</td><td width=5></td></tr></table><img src=\"L2UI.Squaregray\" width=610 height=1>");
                    ++index;
                }
            }
        }

        content = content.replace("%maillist%", sb.toString());
        sb.setLength(0);
        String fullSearch = !sType.isEmpty() && !search.isEmpty() ? ";" + sType + ";" + search : "";
        StringUtil.append(sb, "<td><table><tr><td></td></tr><tr><td><button action=\"bypass _bbsmail;", type, ";", page == 1 ? page : page - 1, fullSearch, "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td></tr></table></td>");
        int i;
        if (maxpage > 21) {
            if (page <= 11) {
                for (i = 1; i <= 10 + page; ++i) {
                    if (i == page) {
                        StringUtil.append(sb, "<td> ", i, " </td>");
                    } else {
                        StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
                    }
                }
            } else if (page > 11 && maxpage - page > 10) {
                for (i = page - 10; i <= page - 1; ++i) {
                    if (i != page) {
                        StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
                    }
                }

                for (i = page; i <= page + 10; ++i) {
                    if (i == page) {
                        StringUtil.append(sb, "<td> ", i, " </td>");
                    } else {
                        StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
                    }
                }
            } else if (maxpage - page <= 10) {
                for (i = page - 10; i <= maxpage; ++i) {
                    if (i == page) {
                        StringUtil.append(sb, "<td> ", i, " </td>");
                    } else {
                        StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
                    }
                }
            }
        } else {
            for (i = 1; i <= maxpage; ++i) {
                if (i == page) {
                    StringUtil.append(sb, "<td> ", i, " </td>");
                } else {
                    StringUtil.append(sb, "<td><a action=\"bypass _bbsmail;", type, ";", i, fullSearch, "\"> ", i, " </a></td>");
                }
            }
        }

        StringUtil.append(sb, "<td><table><tr><td></td></tr><tr><td><button action=\"bypass _bbsmail;", type, ";", page == maxpage ? page : page + 1, fullSearch, "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td></tr></table></td>");
        content = content.replace("%maillistlength%", sb.toString());
        separateAndSend(content, player);
    }

    private void showMailView(Player player, MailBBSManager.Mail mail) {
        if (mail == null) {
            this.showMailList(player, 1, MailBBSManager.MailType.INBOX);
        } else {
            String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/mail/mail-show.htm");
            String var10000 = mail.location.getBypass();
            String link = var10000 + "&nbsp;&gt;&nbsp;" + mail.subject;
            content = content.replace("%maillink%", link);
            content = content.replace("%writer%", getPlayerName(mail.senderId));
            content = content.replace("%sentDate%", mail.sentDateString);
            content = content.replace("%receiver%", mail.recipientNames);
            content = content.replace("%delDate%", "Unknown");
            content = content.replace("%title%", mail.subject.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;"));
            content = content.replace("%mes%", mail.message.replaceAll("\r\n", "<br>").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;"));
            content = content.replace("%letterId%", String.valueOf(mail.mailId));
            separateAndSend(content, player);
        }
    }

    public void sendMail(String recipients, String subject, String message, Player player) {
        long currentDate = Calendar.getInstance().getTimeInMillis();
        Timestamp ts = new Timestamp(currentDate - 86400000L);
        if (this.getPlayerMails(player.getObjectId()).stream().filter((l) -> l.sentDate.after(ts) && l.location == MailType.SENTBOX).count() >= 10L) {
            player.sendPacket(SystemMessageId.NO_MORE_MESSAGES_TODAY);
        } else {
            String[] recipientNames = recipients.trim().split(";");
            if (recipientNames.length > 5 && !player.isGM()) {
                player.sendPacket(SystemMessageId.ONLY_FIVE_RECIPIENTS);
            } else {
                if (subject == null || subject.isEmpty()) {
                    subject = "(no subject)";
                }

                message = message.replaceAll("\n", "<br1>");
                Timestamp time = new Timestamp(currentDate);

                try {
                    Connection con = ConnectionPool.getConnection();

                    try {
                        PreparedStatement ps = con.prepareStatement("INSERT INTO character_mail (charId, letterId, senderId, location, recipientNames, subject, message, sentDate, unread) VALUES (?,?,?,?,?,?,?,?,?)");

                        try {
                            ps.setInt(3, player.getObjectId());
                            ps.setString(4, "inbox");
                            ps.setString(5, recipients);
                            ps.setString(6, abbreviate(subject, 128));
                            ps.setString(7, message);
                            ps.setTimestamp(8, time);
                            ps.setInt(9, 1);
                            String[] var12 = recipientNames;
                            int id = recipientNames.length;
                            int var14 = 0;

                            while (true) {
                                if (var14 >= id) {
                                    int[] result = ps.executeBatch();
                                    if (result.length > 0) {
                                        id = this.getNewMailId();
                                        ps.setInt(1, player.getObjectId());
                                        ps.setInt(2, id);
                                        ps.setString(4, "sentbox");
                                        ps.setInt(9, 0);
                                        ps.execute();
                                        MailBBSManager.Mail mail = new Mail(this);
                                        mail.charId = player.getObjectId();
                                        mail.mailId = id;
                                        mail.senderId = player.getObjectId();
                                        mail.location = MailBBSManager.MailType.SENTBOX;
                                        mail.recipientNames = recipients;
                                        mail.subject = abbreviate(subject, 128);
                                        mail.message = message;
                                        mail.sentDate = time;
                                        mail.sentDateString = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(mail.sentDate);
                                        mail.unread = false;
                                        this.getPlayerMails(player.getObjectId()).add(mail);
                                        player.sendPacket(SystemMessageId.SENT_MAIL);
                                    }
                                    break;
                                }

                                String recipientName = var12[var14];
                                int recipientId = PlayerInfoTable.getInstance().getPlayerObjectId(recipientName);
                                if (recipientId > 0 && recipientId != player.getObjectId()) {
                                    label81:
                                    {
                                        Player recipientPlayer = World.getInstance().getPlayer(recipientId);
                                        if (!player.isGM()) {
                                            if (isGM(recipientId)) {
                                                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_MAIL_GM_S1).addString(recipientName));
                                                break label81;
                                            }

                                            if (isBlocked(player, recipientId)) {
                                                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_BLOCKED_YOU_CANNOT_MAIL).addString(recipientName));
                                                break label81;
                                            }

                                            if (this.isInboxFull(recipientId)) {
                                                player.sendPacket(SystemMessageId.MESSAGE_NOT_SENT);
                                                if (recipientPlayer != null) {
                                                    recipientPlayer.sendPacket(SystemMessageId.MAILBOX_FULL);
                                                }
                                                break label81;
                                            }
                                        }

                                        id = this.getNewMailId();
                                        ps.setInt(1, recipientId);
                                        ps.setInt(2, id);
                                        ps.addBatch();
                                        MailBBSManager.Mail mail = new Mail(this);
                                        mail.charId = recipientId;
                                        mail.mailId = id;
                                        mail.senderId = player.getObjectId();
                                        mail.location = MailBBSManager.MailType.INBOX;
                                        mail.recipientNames = recipients;
                                        mail.subject = abbreviate(subject, 128);
                                        mail.message = message;
                                        mail.sentDate = time;
                                        mail.sentDateString = (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(mail.sentDate);
                                        mail.unread = true;
                                        this.getPlayerMails(recipientId).add(mail);
                                        if (recipientPlayer != null) {
                                            recipientPlayer.sendPacket(SystemMessageId.NEW_MAIL);
                                            recipientPlayer.sendPacket(new PlaySound("systemmsg_e.1233"));
                                            recipientPlayer.sendPacket(ExMailArrived.STATIC_PACKET);
                                        }
                                    }
                                } else {
                                    player.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                }

                                ++var14;
                            }
                        } catch (Throwable var22) {
                            if (ps != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var21) {
                                    var22.addSuppressed(var21);
                                }
                            }

                            throw var22;
                        }

                        if (ps != null) {
                            ps.close();
                        }
                    } catch (Throwable var23) {
                        if (con != null) {
                            try {
                                con.close();
                            } catch (Throwable var20) {
                                var23.addSuppressed(var20);
                            }
                        }

                        throw var23;
                    }

                    if (con != null) {
                        con.close();
                    }
                } catch (Exception var24) {
                    LOGGER.error("Couldn't send mail for {}.", var24, player.getName());
                }

            }
        }
    }

    private int getMailCount(int objectId, MailBBSManager.MailType location, String type, String search) {
        int count = 0;
        if (!type.isEmpty() && !search.isEmpty()) {
            boolean byTitle = type.equalsIgnoreCase("title");
            Iterator<Mail> var11 = this.getPlayerMails(objectId).iterator();

            while (true) {
                while (true) {
                    MailBBSManager.Mail mail;
                    do {
                        if (!var11.hasNext()) {
                            return count;
                        }

                        mail = var11.next();
                    } while (!mail.location.equals(location));

                    if (byTitle && mail.subject.toLowerCase().contains(search.toLowerCase())) {
                        ++count;
                    } else if (!byTitle) {
                        String writer = getPlayerName(mail.senderId);
                        if (writer.toLowerCase().contains(search.toLowerCase())) {
                            ++count;
                        }
                    }
                }
            }
        } else {

            for (Mail mail : this.getPlayerMails(objectId)) {
                if (mail.location.equals(location)) {
                    ++count;
                }
            }

            return count;
        }
    }

    private void deleteMail(Player player, int mailId) {

        for (Mail mail : this.getPlayerMails(player.getObjectId())) {
            if (mail.mailId == mailId) {
                this.getPlayerMails(player.getObjectId()).remove(mail);
                break;
            }
        }

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM character_mail WHERE letterId = ?");

                try {
                    ps.setInt(1, mailId);
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
            LOGGER.error("Couldn't delete mail #{}.", var11, mailId);
        }

    }

    private void setMailToRead(Player player, int mailId) {
        this.getMail(player, mailId).unread = false;

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE character_mail SET unread = 0 WHERE letterId = ?");

                try {
                    ps.setInt(1, mailId);
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
            LOGGER.error("Couldn't set read status for mail #{}.", var11, mailId);
        }

    }

    private void setMailLocation(Player player, int mailId, MailBBSManager.MailType location) {
        this.getMail(player, mailId).location = location;

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE character_mail SET location = ? WHERE letterId = ?");

                try {
                    ps.setString(1, location.toString().toLowerCase());
                    ps.setInt(2, mailId);
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
            LOGGER.error("Couldn't set mail #{} location.", var12, mailId);
        }

    }

    private boolean isInboxFull(int objectId) {
        return this.getMailCount(objectId, MailBBSManager.MailType.INBOX, "", "") >= 100;
    }

    private void showLastForum(Player player) {
        int page = player.getMailPosition() % 1000;
        int type = player.getMailPosition() / 1000;
        this.showMailList(player, page, MailBBSManager.MailType.VALUES[type]);
    }

    private enum MailType {
        INBOX("Inbox", "<a action=\"bypass _bbsmail\">Inbox</a>"),
        SENTBOX("Sent Box", "<a action=\"bypass _bbsmail;sentbox\">Sent Box</a>"),
        ARCHIVE("Mail Archive", "<a action=\"bypass _bbsmail;archive\">Mail Archive</a>"),
        TEMPARCHIVE("Temporary Mail Archive", "<a action=\"bypass _bbsmail;temp_archive\">Temporary Mail Archive</a>");

        public static final MailBBSManager.MailType[] VALUES = values();
        private final String _description;
        private final String _bypass;

        MailType(String bypass, String description) {
            this._description = description;
            this._bypass = bypass;
        }

        public String getDescription() {
            return this._description;
        }

        public String getBypass() {
            return this._bypass;
        }
    }

    private static class SingletonHolder {
        protected static final MailBBSManager INSTANCE = new MailBBSManager();
    }

    public static class Mail {
        int charId;
        int mailId;
        int senderId;
        MailBBSManager.MailType location;
        String recipientNames;
        String subject;
        String message;
        Timestamp sentDate;
        String sentDateString;
        boolean unread;

        public Mail(final MailBBSManager param1) {
        }
    }
}