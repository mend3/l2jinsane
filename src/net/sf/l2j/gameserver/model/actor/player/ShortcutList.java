package net.sf.l2j.gameserver.model.actor.player;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.enums.ShortcutType;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.Macro;
import net.sf.l2j.gameserver.model.Shortcut;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutDelete;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutRegister;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class ShortcutList {
    private static final CLogger LOGGER = new CLogger(ShortcutList.class.getName());

    private static final String INSERT_SHORTCUT = "REPLACE INTO character_shortcuts (char_obj_id,slot,page,type,id,level,class_index) values(?,?,?,?,?,?,?)";

    private static final String DELETE_SHORTCUT = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND slot=? AND page=? AND class_index=?";

    private static final String LOAD_SHORTCUTS = "SELECT char_obj_id, slot, page, type, id, level FROM character_shortcuts WHERE char_obj_id=? AND class_index=?";

    private static final int MAX_SHORTCUTS_PER_BAR = 12;

    private final Map<Integer, Shortcut> _shortcuts = new ConcurrentSkipListMap<>();

    private final Player _owner;

    public ShortcutList(Player owner) {
        this._owner = owner;
    }

    public Shortcut[] getShortcuts() {
        return (Shortcut[]) this._shortcuts.values().toArray((Object[]) new Shortcut[this._shortcuts.size()]);
    }

    public void addShortcut(Shortcut shortcut) {
        addShortcut(shortcut, true);
    }

    public void addShortcut(Shortcut shortcut, boolean checkIntegrity) {
        if (checkIntegrity) {
            ItemInstance item;
            L2Skill skill;
            Macro macro;
            switch (shortcut.getType()) {
                case ITEM:
                    item = this._owner.getInventory().getItemByObjectId(shortcut.getId());
                    if (item == null)
                        return;
                    if (item.isEtcItem())
                        shortcut.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
                    break;
                case SKILL:
                    skill = this._owner.getSkill(shortcut.getId());
                    if (skill == null)
                        return;
                    if (skill.getLevel() != shortcut.getLevel())
                        shortcut.setLevel(skill.getLevel());
                    break;
                case MACRO:
                    macro = this._owner.getMacroList().getMacro(shortcut.getId());
                    if (macro == null)
                        return;
                    break;
                case RECIPE:
                    if (!this._owner.hasRecipeList(shortcut.getId()))
                        return;
                    break;
            }
        }
        Shortcut oldShortcut = this._shortcuts.put(shortcut.getSlot() + shortcut.getPage() * 12, shortcut);
        if (oldShortcut != null)
            deleteShortCutFromDb(oldShortcut);
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("REPLACE INTO character_shortcuts (char_obj_id,slot,page,type,id,level,class_index) values(?,?,?,?,?,?,?)");
                try {
                    ps.setInt(1, this._owner.getObjectId());
                    ps.setInt(2, shortcut.getSlot());
                    ps.setInt(3, shortcut.getPage());
                    ps.setString(4, shortcut.getType().toString());
                    ps.setInt(5, shortcut.getId());
                    ps.setInt(6, shortcut.getLevel());
                    ps.setInt(7, this._owner.getClassIndex());
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
            LOGGER.error("Couldn't store shortcut.", e);
        }
    }

    public void deleteShortcut(int slot, int page) {
        slot += page * 12;
        Shortcut oldShortcut = this._shortcuts.remove(slot);
        if (oldShortcut == null || this._owner == null)
            return;
        deleteShortCutFromDb(oldShortcut);
        if (oldShortcut.getType() == ShortcutType.ITEM) {
            ItemInstance item = this._owner.getInventory().getItemByObjectId(oldShortcut.getId());
            if (item != null && item.getItemType() == EtcItemType.SHOT)
                if (this._owner.removeAutoSoulShot(item.getItemId()))
                    this._owner.sendPacket(new ExAutoSoulShot(item.getItemId(), 0));
        }
        this._owner.sendPacket(new ShortCutDelete(slot));
        for (int shotId : this._owner.getAutoSoulShot()) {
            this._owner.sendPacket(new ExAutoSoulShot(shotId, 1));
        }
    }

    private void deleteShortCutFromDb(Shortcut shortcut) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=? AND slot=? AND page=? AND class_index=?");
                try {
                    ps.setInt(1, this._owner.getObjectId());
                    ps.setInt(2, shortcut.getSlot());
                    ps.setInt(3, shortcut.getPage());
                    ps.setInt(4, this._owner.getClassIndex());
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
            LOGGER.error("Couldn't delete shortcut.", e);
        }
    }

    public void restore() {
        this._shortcuts.clear();
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT char_obj_id, slot, page, type, id, level FROM character_shortcuts WHERE char_obj_id=? AND class_index=?");
                try {
                    ps.setInt(1, this._owner.getObjectId());
                    ps.setInt(2, this._owner.getClassIndex());
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            int slot = rs.getInt("slot");
                            int page = rs.getInt("page");
                            Shortcut shortcut = new Shortcut(slot, page, Enum.valueOf(ShortcutType.class, rs.getString("type")), rs.getInt("id"), rs.getInt("level"), 1);
                            if (shortcut.getType() == ShortcutType.ITEM) {
                                ItemInstance item = this._owner.getInventory().getItemByObjectId(shortcut.getId());
                                if (item == null)
                                    continue;
                                if (item.isEtcItem())
                                    shortcut.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
                            }
                            this._shortcuts.put(slot + page * 12, shortcut);
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
            LOGGER.error("Couldn't restore shortcuts.", e);
        }
    }

    public void refreshShortcuts(int id, int level, ShortcutType type) {
        for (Shortcut shortcut : this._shortcuts.values()) {
            if (shortcut.getId() == id && shortcut.getType() == type) {
                shortcut.setLevel(level);
                this._owner.sendPacket(new ShortCutRegister(shortcut));
            }
        }
    }

    public void deleteShortcuts(int id, ShortcutType type) {
        for (Shortcut shortcut : this._shortcuts.values()) {
            if (shortcut.getId() == id && shortcut.getType() == type)
                deleteShortcut(shortcut.getSlot(), shortcut.getPage());
        }
    }
}
