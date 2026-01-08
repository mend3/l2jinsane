package net.sf.l2j.gameserver.model.actor.player;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.enums.ShortcutType;
import net.sf.l2j.gameserver.model.Macro;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SendMacroList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class MacroList {
    private static final CLogger LOGGER = new CLogger(MacroList.class.getName());

    private static final String INSERT_MACRO = "REPLACE INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)";

    private static final String DELETE_MACRO = "DELETE FROM character_macroses WHERE char_obj_id=? AND id=?";

    private static final String LOAD_MACROS = "SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?";

    private final Map<Integer, Macro> _macros = new LinkedHashMap<>();

    private final Player _owner;

    private int _revision;

    private int _macroId;

    public MacroList(Player owner) {
        this._owner = owner;
        this._revision = 1;
        this._macroId = 1000;
    }

    public int getRevision() {
        return this._revision;
    }

    public Macro[] getMacros() {
        return (Macro[]) this._macros.values().toArray((Object[]) new Macro[this._macros.size()]);
    }

    public Macro getMacro(int id) {
        return this._macros.get(id);
    }

    public void registerMacro(Macro macro) {
        if (macro.id == 0) {
            macro.id = this._macroId++;
            while (this._macros.get(macro.id) != null)
                macro.id = this._macroId++;
            this._macros.put(macro.id, macro);
        } else {
            Macro old = this._macros.put(macro.id, macro);
            if (old != null)
                deleteMacroFromDb(old);
        }
        registerMacroInDb(macro);
        sendUpdate();
    }

    public void deleteMacro(int id) {
        Macro toRemove = this._macros.get(id);
        if (toRemove != null)
            deleteMacroFromDb(toRemove);
        this._macros.remove(id);
        this._owner.getShortcutList().deleteShortcuts(id, ShortcutType.MACRO);
        sendUpdate();
    }

    public void sendUpdate() {
        this._revision++;
        Macro[] macros = getMacros();
        if (macros.length == 0) {
            this._owner.sendPacket(new SendMacroList(this._revision, macros.length, null));
        } else {
            for (Macro macro : macros)
                this._owner.sendPacket(new SendMacroList(this._revision, macros.length, macro));
        }
    }

    private void registerMacroInDb(Macro macro) {
        StringBuilder sb = new StringBuilder(300);
        for (Macro.MacroCmd cmd : macro.commands) {
            StringUtil.append(sb, cmd.type(), ",", cmd.d1(), ",", cmd.d2());
            if (cmd.cmd() != null && !cmd.cmd().isEmpty())
                StringUtil.append(sb, ",", cmd.cmd());
            sb.append(';');
        }
        if (sb.length() > 255)
            sb.setLength(255);
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("REPLACE INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)");
                try {
                    ps.setInt(1, this._owner.getObjectId());
                    ps.setInt(2, macro.id);
                    ps.setInt(3, macro.icon);
                    ps.setString(4, macro.name);
                    ps.setString(5, macro.descr);
                    ps.setString(6, macro.acronym);
                    ps.setString(7, sb.toString());
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
            LOGGER.error("Couldn't store macro.", e);
        }
    }

    private void deleteMacroFromDb(Macro macro) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=? AND id=?");
                try {
                    ps.setInt(1, this._owner.getObjectId());
                    ps.setInt(2, macro.id);
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
            LOGGER.error("Couldn't delete macro.", e);
        }
    }

    public void restore() {
        this._macros.clear();
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?");
                try {
                    ps.setInt(1, this._owner.getObjectId());
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            int icon = rs.getInt("icon");
                            String name = rs.getString("name");
                            String descr = rs.getString("descr");
                            String acronym = rs.getString("acronym");
                            List<Macro.MacroCmd> commands = new ArrayList<>();
                            StringTokenizer st1 = new StringTokenizer(rs.getString("commands"), ";");
                            while (st1.hasMoreTokens()) {
                                StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
                                if (st.countTokens() < 3)
                                    continue;
                                int type = Integer.parseInt(st.nextToken());
                                int d1 = Integer.parseInt(st.nextToken());
                                int d2 = Integer.parseInt(st.nextToken());
                                String cmd = "";
                                if (st.hasMoreTokens())
                                    cmd = st.nextToken();
                                Macro.MacroCmd mcmd = new Macro.MacroCmd(commands.size(), type, d1, d2, cmd);
                                commands.add(mcmd);
                            }
                            Macro macro = new Macro(id, icon, name, descr, acronym, commands.toArray(new Macro.MacroCmd[0]));
                            this._macros.put(macro.id, macro);
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
            LOGGER.error("Couldn't load macros.", e);
        }
    }
}
