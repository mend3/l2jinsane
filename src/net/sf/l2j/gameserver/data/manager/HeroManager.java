package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.data.xml.PlayerClassData;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

public class HeroManager {
    public static final String COUNT = "count";
    public static final String PLAYED = "played";
    public static final String CLAN_NAME = "clan_name";
    public static final String CLAN_CREST = "clan_crest";
    public static final String ALLY_NAME = "ally_name";
    public static final String ALLY_CREST = "ally_crest";
    public static final String ACTIVE = "active";
    public static final int ACTION_RAID_KILLED = 1;
    public static final int ACTION_HERO_GAINED = 2;
    public static final int ACTION_CASTLE_TAKEN = 3;
    private static final CLogger LOGGER = new CLogger(HeroManager.class.getName());
    private static final String LOAD_HEROES = "SELECT heroes.char_id, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.obj_Id = heroes.char_id AND heroes.played = 1";
    private static final String LOAD_ALL_HEROES = "SELECT heroes.char_id, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.obj_Id = heroes.char_id";
    private static final String RESET_PLAYED = "UPDATE heroes SET played = 0";
    private static final String INSERT_HERO = "INSERT INTO heroes (char_id, class_id, count, played, active) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE count=VALUES(count),played=VALUES(played),active=VALUES(active)";
    private static final String LOAD_CLAN_DATA = "SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid WHERE characters.obj_Id = ?";
    private static final String LOAD_MESSAGE = "SELECT message FROM heroes WHERE char_id=?";
    private static final String LOAD_DIARY = "SELECT * FROM  heroes_diary WHERE char_id=? ORDER BY time ASC";
    private static final String LOAD_FIGHTS = "SELECT * FROM olympiad_fights WHERE (charOneId=? OR charTwoId=?) AND start<? ORDER BY start ASC";
    private static final String UPDATE_DIARY = "INSERT INTO heroes_diary (char_id, time, action, param) values(?,?,?,?)";
    private static final String UPDATE_MESSAGE = "UPDATE heroes SET message=? WHERE char_id=?";
    private static final String DELETE_ITEMS = "DELETE FROM items WHERE item_id IN (6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621) AND owner_id NOT IN (SELECT obj_Id FROM characters WHERE accesslevel > 0)";
    private final Map<Integer, StatSet> _heroes = new HashMap<>();

    private final Map<Integer, StatSet> _completeHeroes = new HashMap<>();

    private final Map<Integer, StatSet> _heroCounts = new HashMap<>();

    private final Map<Integer, List<StatSet>> _heroFights = new HashMap<>();

    private final List<StatSet> _fights = new ArrayList<>();

    private final Map<Integer, List<StatSet>> _heroDiaries = new HashMap<>();

    private final Map<Integer, String> _heroMessages = new HashMap<>();

    private final List<StatSet> _diary = new ArrayList<>();

    private static String calcFightTime(long fightTime) {
        String format = String.format("%%0%dd", 2);
        fightTime /= 1000L;
        String seconds = String.format(format, fightTime % 60L);
        String minutes = String.format(format, fightTime % 3600L / 60L);
        String time = minutes + ":" + minutes;
        return time;
    }

    public static HeroManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps2 = con.prepareStatement("SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid WHERE characters.obj_Id = ?");
                try {
                    PreparedStatement ps = con.prepareStatement("SELECT heroes.char_id, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.obj_Id = heroes.char_id AND heroes.played = 1");
                    try {
                        ResultSet rs = ps.executeQuery();
                        try {
                            while (rs.next()) {
                                int objectId = rs.getInt("char_id");
                                StatSet hero = new StatSet();
                                hero.set("char_name", rs.getString("char_name"));
                                hero.set("class_id", rs.getInt("class_id"));
                                hero.set("count", rs.getInt("count"));
                                hero.set("played", rs.getInt("played"));
                                hero.set("active", rs.getInt("active"));
                                loadFights(objectId);
                                loadDiary(objectId);
                                loadMessage(objectId);
                                ps2.setInt(1, objectId);
                                ResultSet rs2 = ps2.executeQuery();
                                try {
                                    if (rs2.next()) {
                                        String clanName = "";
                                        String allyName = "";
                                        int clanCrest = 0;
                                        int allyCrest = 0;
                                        int clanId = rs2.getInt("clanid");
                                        if (clanId > 0) {
                                            Clan clan = ClanTable.getInstance().getClan(clanId);
                                            if (clan != null) {
                                                clanName = clan.getName();
                                                clanCrest = clan.getCrestId();
                                                int allyId = rs2.getInt("allyId");
                                                if (allyId > 0) {
                                                    allyName = clan.getAllyName();
                                                    allyCrest = clan.getAllyCrestId();
                                                }
                                            }
                                        }
                                        hero.set("clan_crest", clanCrest);
                                        hero.set("clan_name", clanName);
                                        hero.set("ally_crest", allyCrest);
                                        hero.set("ally_name", allyName);
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
                                ps2.clearParameters();
                                this._heroes.put(objectId, hero);
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
                    ps = con.prepareStatement("SELECT heroes.char_id, characters.char_name, heroes.class_id, heroes.count, heroes.played, heroes.active FROM heroes, characters WHERE characters.obj_Id = heroes.char_id");
                    try {
                        ResultSet rs = ps.executeQuery();
                        try {
                            while (rs.next()) {
                                int objectId = rs.getInt("char_id");
                                StatSet hero = new StatSet();
                                hero.set("char_name", rs.getString("char_name"));
                                hero.set("class_id", rs.getInt("class_id"));
                                hero.set("count", rs.getInt("count"));
                                hero.set("played", rs.getInt("played"));
                                hero.set("active", rs.getInt("active"));
                                ps2.setInt(1, objectId);
                                ResultSet rs2 = ps2.executeQuery();
                                try {
                                    if (rs2.next()) {
                                        String clanName = "";
                                        String allyName = "";
                                        int clanCrest = 0;
                                        int allyCrest = 0;
                                        int clanId = rs2.getInt("clanid");
                                        if (clanId > 0) {
                                            Clan clan = ClanTable.getInstance().getClan(clanId);
                                            if (clan != null) {
                                                clanName = clan.getName();
                                                clanCrest = clan.getCrestId();
                                                int allyId = rs2.getInt("allyId");
                                                if (allyId > 0) {
                                                    allyName = clan.getAllyName();
                                                    allyCrest = clan.getAllyCrestId();
                                                }
                                            }
                                        }
                                        hero.set("clan_crest", clanCrest);
                                        hero.set("clan_name", clanName);
                                        hero.set("ally_crest", allyCrest);
                                        hero.set("ally_name", allyName);
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
                                ps2.clearParameters();
                                this._completeHeroes.put(objectId, hero);
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
            LOGGER.error("Couldn't load heroes.", e);
        }
        LOGGER.info("Loaded {} heroes and {} all time heroes.", this._heroes.size(), this._completeHeroes.size());
    }

    private void loadMessage(int objectId) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT message FROM heroes WHERE char_id=?");
                try {
                    ps.setInt(1, objectId);
                    ResultSet rs = ps.executeQuery();
                    try {
                        if (rs.next())
                            this._heroMessages.put(objectId, rs.getString("message"));
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
            LOGGER.error("Couldn't load hero message for: {}.", e, objectId);
        }
    }

    private void loadDiary(int objectId) {
        int entries = 0;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM  heroes_diary WHERE char_id=? ORDER BY time ASC");
                try {
                    ps.setInt(1, objectId);
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            long time = rs.getLong("time");
                            int action = rs.getInt("action");
                            int param = rs.getInt("param");
                            StatSet entry = new StatSet();
                            entry.set("date", (new SimpleDateFormat("yyyy-MM-dd HH")).format(time));
                            if (action == 1) {
                                NpcTemplate template = NpcData.getInstance().getTemplate(param);
                                if (template != null)
                                    entry.set("action", template.getName() + " was defeated");
                            } else if (action == 2) {
                                entry.set("action", "Gained Hero status");
                            } else if (action == 3) {
                                Castle castle = CastleManager.getInstance().getCastleById(param);
                                if (castle != null)
                                    entry.set("action", castle.getName() + " Castle was successfuly taken");
                            }
                            this._diary.add(entry);
                            entries++;
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
                    this._heroDiaries.put(objectId, this._diary);
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
            LOGGER.error("Couldn't load hero diary for: {}.", e, objectId);
        }
        LOGGER.info("Loaded {} diary entries for hero: {}.", entries, PlayerInfoTable.getInstance().getPlayerName(objectId));
    }

    private void loadFights(int charId) {
        StatSet heroCountData = new StatSet();
        Calendar data = Calendar.getInstance();
        data.set(Calendar.DATE, 1);
        data.set(Calendar.HOUR_OF_DAY, 0);
        data.set(Calendar.MINUTE, 0);
        data.set(Calendar.MILLISECOND, 0);
        long from = data.getTimeInMillis();
        int numberOfFights = 0;
        int victories = 0;
        int losses = 0;
        int draws = 0;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM olympiad_fights WHERE (charOneId=? OR charTwoId=?) AND start<? ORDER BY start ASC");
                try {
                    ps.setInt(1, charId);
                    ps.setInt(2, charId);
                    ps.setLong(3, from);
                    ResultSet rset = ps.executeQuery();
                    try {
                        while (rset.next()) {
                            int charOneId = rset.getInt("charOneId");
                            int charOneClass = rset.getInt("charOneClass");
                            int charTwoId = rset.getInt("charTwoId");
                            int charTwoClass = rset.getInt("charTwoClass");
                            int winner = rset.getInt("winner");
                            long start = rset.getLong("start");
                            int time = rset.getInt("time");
                            int classed = rset.getInt("classed");
                            if (charId == charOneId) {
                                String name = PlayerInfoTable.getInstance().getPlayerName(charTwoId);
                                String cls = PlayerClassData.getInstance().getClassNameById(charTwoClass);
                                if (name != null && cls != null) {
                                    StatSet fight = new StatSet();
                                    fight.set("oponent", name);
                                    fight.set("oponentclass", cls);
                                    fight.set("time", calcFightTime(time));
                                    fight.set("start", (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(start));
                                    fight.set("classed", classed);
                                    if (winner == 1) {
                                        fight.set("result", "<font color=\"00ff00\">victory</font>");
                                        victories++;
                                    } else if (winner == 2) {
                                        fight.set("result", "<font color=\"ff0000\">loss</font>");
                                        losses++;
                                    } else if (winner == 0) {
                                        fight.set("result", "<font color=\"ffff00\">draw</font>");
                                        draws++;
                                    }
                                    this._fights.add(fight);
                                    numberOfFights++;
                                }
                                continue;
                            }
                            if (charId == charTwoId) {
                                String name = PlayerInfoTable.getInstance().getPlayerName(charOneId);
                                String cls = PlayerClassData.getInstance().getClassNameById(charOneClass);
                                if (name != null && cls != null) {
                                    StatSet fight = new StatSet();
                                    fight.set("oponent", name);
                                    fight.set("oponentclass", cls);
                                    fight.set("time", calcFightTime(time));
                                    fight.set("start", (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(start));
                                    fight.set("classed", classed);
                                    if (winner == 1) {
                                        fight.set("result", "<font color=\"ff0000\">loss</font>");
                                        losses++;
                                    } else if (winner == 2) {
                                        fight.set("result", "<font color=\"00ff00\">victory</font>");
                                        victories++;
                                    } else if (winner == 0) {
                                        fight.set("result", "<font color=\"ffff00\">draw</font>");
                                        draws++;
                                    }
                                    this._fights.add(fight);
                                    numberOfFights++;
                                }
                            }
                        }
                        if (rset != null)
                            rset.close();
                    } catch (Throwable throwable) {
                        if (rset != null)
                            try {
                                rset.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    heroCountData.set("victory", victories);
                    heroCountData.set("draw", draws);
                    heroCountData.set("loss", losses);
                    this._heroCounts.put(charId, heroCountData);
                    this._heroFights.put(charId, this._fights);
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
            LOGGER.error("Couldn't load hero fights history for: {}.", e, charId);
        }
        LOGGER.info("Loaded {} fights for: {}.", numberOfFights, PlayerInfoTable.getInstance().getPlayerName(charId));
    }

    public Map<Integer, StatSet> getHeroes() {
        return this._heroes;
    }

    public Map<Integer, StatSet> getAllHeroes() {
        return this._completeHeroes;
    }

    public int getHeroesCount(Player player) {
        if (this._heroes.isEmpty() || !this._heroes.containsKey(player.getObjectId()))
            return 0;
        int val = Integer.parseInt(this._heroes.get(player.getObjectId()).getString("count"));
        return (val > 0) ? val : 0;
    }

    public int getHeroByClass(int classId) {
        if (this._heroes.isEmpty())
            return 0;
        for (Map.Entry<Integer, StatSet> hero : this._heroes.entrySet()) {
            if (hero.getValue().getInteger("class_id") == classId)
                return hero.getKey();
        }
        return 0;
    }

    public void resetData() {
        this._heroDiaries.clear();
        this._heroFights.clear();
        this._heroCounts.clear();
        this._heroMessages.clear();
    }

    public void showHeroDiary(Player player, int heroclass, int objectId, int page) {
        List<StatSet> mainList = this._heroDiaries.get(objectId);
        if (mainList == null)
            return;
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/olympiad/herodiary.htm");
        html.replace("%heroname%", PlayerInfoTable.getInstance().getPlayerName(objectId));
        html.replace("%message%", this._heroMessages.get(objectId));
        html.disableValidation();
        if (!mainList.isEmpty()) {
            List<StatSet> list = new ArrayList<>();
            list.addAll(mainList);
            Collections.reverse(list);
            boolean color = true;
            int counter = 0;
            int breakat = 0;
            int perpage = 10;
            StringBuilder sb = new StringBuilder(500);
            for (int i = (page - 1) * 10; i < list.size(); i++) {
                breakat = i;
                StatSet _diaryentry = list.get(i);
                StringUtil.append(sb, "<tr><td>", color ? "<table width=270 bgcolor=\"131210\">" : "<table width=270>", "<tr><td width=270><font color=\"LEVEL\">", _diaryentry.getString("date"), ":xx</font></td></tr><tr><td width=270>", _diaryentry.getString("action"), "</td></tr><tr><td>&nbsp;</td></tr></table></td></tr>");
                color = !color;
                counter++;
                if (counter >= 10)
                    break;
            }
            if (breakat < list.size() - 1) {
                html.replace("%buttprev%", "<button value=\"Prev\" action=\"bypass _diary?class=" + heroclass + "&page=" + page + 1 + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
            } else {
                html.replace("%buttprev%", "");
            }
            if (page > 1) {
                html.replace("%buttnext%", "<button value=\"Next\" action=\"bypass _diary?class=" + heroclass + "&page=" + (page - 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
            } else {
                html.replace("%buttnext%", "");
            }
            html.replace("%list%", sb.toString());
        } else {
            html.replace("%list%", "");
            html.replace("%buttprev%", "");
            html.replace("%buttnext%", "");
        }
        player.sendPacket(html);
    }

    public void showHeroFights(Player player, int heroclass, int objectId, int page) {
        List<StatSet> list = this._heroFights.get(objectId);
        if (list == null)
            return;
        int win = 0;
        int loss = 0;
        int draw = 0;
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/olympiad/herohistory.htm");
        html.replace("%heroname%", PlayerInfoTable.getInstance().getPlayerName(objectId));
        html.disableValidation();
        if (!list.isEmpty()) {
            if (this._heroCounts.containsKey(objectId)) {
                StatSet _herocount = this._heroCounts.get(objectId);
                win = _herocount.getInteger("victory");
                loss = _herocount.getInteger("loss");
                draw = _herocount.getInteger("draw");
            }
            boolean color = true;
            int counter = 0;
            int breakat = 0;
            int perpage = 20;
            StringBuilder sb = new StringBuilder(500);
            for (int i = (page - 1) * 20; i < list.size(); i++) {
                breakat = i;
                StatSet fight = list.get(i);
                StringUtil.append(sb, "<tr><td>", color ? "<table width=270 bgcolor=\"131210\">" : "<table width=270><tr><td width=220><font color=\"LEVEL\">", fight.getString("start"), "</font>&nbsp;&nbsp;", fight.getString("result"), "</td><td width=50 align=right>", (fight.getInteger("classed") > 0) ? "<font color=\"FFFF99\">cls</font>" : "<font color=\"999999\">non-cls<font>", "</td></tr><tr><td width=220>vs ", fight.getString("oponent"), " (",
                        fight.getString("oponentclass"), ")</td><td width=50 align=right>(", fight.getString("time"), ")</td></tr><tr><td colspan=2>&nbsp;</td></tr></table></td></tr>");
                color = !color;
                counter++;
                if (counter >= 20)
                    break;
            }
            if (breakat < list.size() - 1) {
                html.replace("%buttprev%", "<button value=\"Prev\" action=\"bypass _match?class=" + heroclass + "&page=" + page + 1 + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
            } else {
                html.replace("%buttprev%", "");
            }
            if (page > 1) {
                html.replace("%buttnext%", "<button value=\"Next\" action=\"bypass _match?class=" + heroclass + "&page=" + (page - 1) + "\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
            } else {
                html.replace("%buttnext%", "");
            }
            html.replace("%list%", sb.toString());
        } else {
            html.replace("%list%", "");
            html.replace("%buttprev%", "");
            html.replace("%buttnext%", "");
        }
        html.replace("%win%", win);
        html.replace("%draw%", draw);
        html.replace("%loos%", loss);
        player.sendPacket(html);
    }

    public synchronized void computeNewHeroes(List<StatSet> newHeroes) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE heroes SET played = 0");
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
            LOGGER.error("Couldn't reset heroes.", e);
        }
        if (!this._heroes.isEmpty())
            for (StatSet hero : this._heroes.values()) {
                String name = hero.getString("char_name");
                Player player = World.getInstance().getPlayer(name);
                if (player == null)
                    continue;
                player.setHero(false);
                for (int i = 0; i < 17; i++) {
                    ItemInstance equippedItem = player.getInventory().getPaperdollItem(i);
                    if (equippedItem != null && equippedItem.isHeroItem())
                        player.getInventory().unEquipItemInSlot(i);
                }
                for (ItemInstance item : player.getInventory().getAvailableItems(false, true)) {
                    if (item.isHeroItem())
                        player.destroyItem("Hero", item, null, true);
                }
                player.broadcastUserInfo();
            }
        if (newHeroes.isEmpty()) {
            this._heroes.clear();
            return;
        }
        Map<Integer, StatSet> heroes = new HashMap<>();
        for (StatSet hero : newHeroes) {
            int objectId = hero.getInteger("char_id");
            StatSet set = this._completeHeroes.get(objectId);
            if (set != null) {
                set.set("count", set.getInteger("count") + 1);
                set.set("played", 1);
                set.set("active", 0);
            } else {
                set = new StatSet();
                set.set("char_name", hero.getString("char_name"));
                set.set("class_id", hero.getInteger("class_id"));
                set.set("count", 1);
                set.set("played", 1);
                set.set("active", 0);
            }
            heroes.put(objectId, set);
        }
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM items WHERE item_id IN (6842, 6611, 6612, 6613, 6614, 6615, 6616, 6617, 6618, 6619, 6620, 6621) AND owner_id NOT IN (SELECT obj_Id FROM characters WHERE accesslevel > 0)");
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
            LOGGER.error("Couldn't delete hero items.", e);
        }
        this._heroes.clear();
        this._heroes.putAll(heroes);
        heroes.clear();
        updateHeroes();
    }

    private void updateHeroes() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO heroes (char_id, class_id, count, played, active) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE count=VALUES(count),played=VALUES(played),active=VALUES(active)");
                try {
                    for (Map.Entry<Integer, StatSet> heroEntry : this._heroes.entrySet()) {
                        int heroId = heroEntry.getKey();
                        StatSet hero = heroEntry.getValue();
                        ps.setInt(1, heroId);
                        ps.setInt(2, hero.getInteger("class_id"));
                        ps.setInt(3, hero.getInteger("count"));
                        ps.setInt(4, hero.getInteger("played"));
                        ps.setInt(5, hero.getInteger("active"));
                        ps.addBatch();
                        if (!this._completeHeroes.containsKey(heroId)) {
                            PreparedStatement ps2 = con.prepareStatement("SELECT characters.clanid AS clanid, coalesce(clan_data.ally_Id, 0) AS allyId FROM characters LEFT JOIN clan_data ON clan_data.clan_id = characters.clanid WHERE characters.obj_Id = ?");
                            try {
                                ps2.setInt(1, heroId);
                                ResultSet rs2 = ps2.executeQuery();
                                try {
                                    if (rs2.next()) {
                                        String clanName = "";
                                        String allyName = "";
                                        int clanCrest = 0;
                                        int allyCrest = 0;
                                        int clanId = rs2.getInt("clanid");
                                        if (clanId > 0) {
                                            Clan clan = ClanTable.getInstance().getClan(clanId);
                                            if (clan != null) {
                                                clanName = clan.getName();
                                                clanCrest = clan.getCrestId();
                                                int allyId = rs2.getInt("allyId");
                                                if (allyId > 0) {
                                                    allyName = clan.getAllyName();
                                                    allyCrest = clan.getAllyCrestId();
                                                }
                                            }
                                        }
                                        hero.set("clan_crest", clanCrest);
                                        hero.set("clan_name", clanName);
                                        hero.set("ally_crest", allyCrest);
                                        hero.set("ally_name", allyName);
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
                            this._heroes.put(heroId, hero);
                            this._completeHeroes.put(heroId, hero);
                        }
                    }
                    ps.executeBatch();
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
            LOGGER.error("Couldn't update heroes.", e);
        }
    }

    public void setHeroGained(int objectId) {
        setDiaryData(objectId, 2, 0);
    }

    public void setRBkilled(int objectId, int npcId) {
        setDiaryData(objectId, 1, npcId);
        NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
        if (template == null)
            return;
        List<StatSet> list = this._heroDiaries.get(objectId);
        if (list == null)
            return;
        this._heroDiaries.remove(objectId);
        StatSet entry = new StatSet();
        entry.set("date", (new SimpleDateFormat("yyyy-MM-dd HH")).format(System.currentTimeMillis()));
        entry.set("action", template.getName() + " was defeated");
        list.add(entry);
        this._heroDiaries.put(objectId, list);
    }

    public void setCastleTaken(int objectId, int castleId) {
        setDiaryData(objectId, 3, castleId);
        Castle castle = CastleManager.getInstance().getCastleById(castleId);
        if (castle == null)
            return;
        List<StatSet> list = this._heroDiaries.get(objectId);
        if (list == null)
            return;
        this._heroDiaries.remove(objectId);
        StatSet entry = new StatSet();
        entry.set("date", (new SimpleDateFormat("yyyy-MM-dd HH")).format(System.currentTimeMillis()));
        entry.set("action", castle.getName() + " Castle was successfuly taken");
        list.add(entry);
        this._heroDiaries.put(objectId, list);
    }

    public void setDiaryData(int objectId, int action, int param) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO heroes_diary (char_id, time, action, param) values(?,?,?,?)");
                try {
                    ps.setInt(1, objectId);
                    ps.setLong(2, System.currentTimeMillis());
                    ps.setInt(3, action);
                    ps.setInt(4, param);
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
            LOGGER.error("Couldn't save diary data for {}.", e, objectId);
        }
    }

    public void setHeroMessage(Player player, String message) {
        this._heroMessages.put(player.getObjectId(), message);
    }

    public void saveHeroMessage(int objectId) {
        if (!this._heroMessages.containsKey(objectId))
            return;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE heroes SET message=? WHERE char_id=?");
                try {
                    ps.setString(1, this._heroMessages.get(objectId));
                    ps.setInt(2, objectId);
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
            LOGGER.error("Couldn't save hero message for {}.", e, objectId);
        }
    }

    public void shutdown() {
        for (Iterator<Integer> iterator = this._heroMessages.keySet().iterator(); iterator.hasNext(); ) {
            int charId = iterator.next();
            saveHeroMessage(charId);
        }
    }

    public boolean isActiveHero(int id) {
        StatSet entry = this._heroes.get(id);
        return (entry != null && entry.getInteger("active") == 1);
    }

    public boolean isInactiveHero(int id) {
        StatSet entry = this._heroes.get(id);
        return (entry != null && entry.getInteger("active") == 0);
    }

    public void activateHero(Player player) {
        StatSet hero = this._heroes.get(player.getObjectId());
        if (hero == null)
            return;
        hero.set("active", 1);
        player.setHero(true);
        player.broadcastPacket(new SocialAction(player, 16));
        player.broadcastUserInfo();
        Clan clan = player.getClan();
        if (clan != null && clan.getLevel() >= 5) {
            clan.addReputationScore(1000);
            clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan), SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_BECAME_HERO_AND_GAINED_S2_REPUTATION_POINTS).addString(hero.getString("char_name")).addNumber(1000));
        }
        setHeroGained(player.getObjectId());
        loadFights(player.getObjectId());
        loadDiary(player.getObjectId());
        this._heroMessages.put(player.getObjectId(), "");
        updateHeroes();
    }

    private static class SingletonHolder {
        protected static final HeroManager INSTANCE = new HeroManager();
    }
}
