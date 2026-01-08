package net.sf.l2j.gameserver.network.serverpackets;

import mods.dressme.DressMeData;
import mods.dressme.SkinPackage;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.CharSelectSlot;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.GameClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CharSelectInfo extends L2GameServerPacket {
    private static final String SELECT_INFOS = "SELECT obj_Id, char_name, level, maxHp, curHp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, sp, karma, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, accesslevel, online, lastAccess, base_class FROM characters WHERE account_name=?";

    private static final String SELECT_CURRENT_SUBCLASS = "SELECT exp, sp, level FROM character_subclasses WHERE char_obj_id=? && class_id=? ORDER BY char_obj_id";

    private static final String SELECT_AUGMENTS = "SELECT attributes FROM augmentations WHERE item_id=?";

    private final CharSelectSlot[] _slots;

    private final String _loginName;

    private final int _sessionId;

    private int _activeId;

    public CharSelectInfo(String loginName, int sessionId) {
        this._slots = loadCharSelectSlots(loginName);
        this._sessionId = sessionId;
        this._loginName = loginName;
        this._activeId = -1;
    }

    public CharSelectInfo(String loginName, int sessionId, int activeId) {
        this._slots = loadCharSelectSlots(loginName);
        this._sessionId = sessionId;
        this._loginName = loginName;
        this._activeId = activeId;
    }

    private static CharSelectSlot[] loadCharSelectSlots(String loginName) {
        List<CharSelectSlot> list = new ArrayList<>();
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT obj_Id, char_name, level, maxHp, curHp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, sp, karma, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, accesslevel, online, lastAccess, base_class FROM characters WHERE account_name=?");
                try {
                    ps.setString(1, loginName);
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            int objectId = rs.getInt("obj_id");
                            String name = rs.getString("char_name");
                            long deleteTime = rs.getLong("deletetime");
                            if (deleteTime > 0L)
                                if (System.currentTimeMillis() > deleteTime) {
                                    Clan clan = ClanTable.getInstance().getClan(rs.getInt("clanid"));
                                    if (clan != null)
                                        clan.removeClanMember(objectId, 0L);
                                    GameClient.deleteCharByObjId(objectId);
                                    continue;
                                }
                            CharSelectSlot slot = new CharSelectSlot(objectId, name);
                            slot.setAccessLevel(rs.getInt("accesslevel"));
                            slot.setLevel(rs.getInt("level"));
                            slot.setMaxHp(rs.getInt("maxhp"));
                            slot.setCurrentHp(rs.getDouble("curhp"));
                            slot.setMaxMp(rs.getInt("maxmp"));
                            slot.setCurrentMp(rs.getDouble("curmp"));
                            slot.setKarma(rs.getInt("karma"));
                            slot.setPkKills(rs.getInt("pkkills"));
                            slot.setPvPKills(rs.getInt("pvpkills"));
                            slot.setFace(rs.getInt("face"));
                            slot.setHairStyle(rs.getInt("hairstyle"));
                            slot.setHairColor(rs.getInt("haircolor"));
                            slot.setSex(rs.getInt("sex"));
                            slot.setExp(rs.getLong("exp"));
                            slot.setSp(rs.getInt("sp"));
                            slot.setClanId(rs.getInt("clanid"));
                            slot.setRace(rs.getInt("race"));
                            slot.setX(rs.getInt("x"));
                            slot.setY(rs.getInt("y"));
                            slot.setZ(rs.getInt("z"));
                            int baseClassId = rs.getInt("base_class");
                            int activeClassId = rs.getInt("classid");
                            if (baseClassId != activeClassId) {
                                PreparedStatement ps2 = con.prepareStatement("SELECT exp, sp, level FROM character_subclasses WHERE char_obj_id=? && class_id=? ORDER BY char_obj_id");
                                try {
                                    ps2.setInt(1, objectId);
                                    ps2.setInt(2, activeClassId);
                                    ResultSet rs2 = ps2.executeQuery();
                                    try {
                                        if (rs2.next()) {
                                            slot.setExp(rs2.getLong("exp"));
                                            slot.setSp(rs2.getInt("sp"));
                                            slot.setLevel(rs2.getInt("level"));
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
                            }
                            slot.setClassId(activeClassId);
                            int weaponObjId = slot.getPaperdollObjectId(7);
                            if (weaponObjId > 0) {
                                PreparedStatement ps3 = con.prepareStatement("SELECT attributes FROM augmentations WHERE item_id=?");
                                try {
                                    ps3.setInt(1, weaponObjId);
                                    ResultSet rs3 = ps3.executeQuery();
                                    try {
                                        if (rs3.next()) {
                                            int augment = rs3.getInt("attributes");
                                            slot.setAugmentationId((augment == -1) ? 0 : augment);
                                        }
                                        if (rs3 != null)
                                            rs3.close();
                                    } catch (Throwable throwable) {
                                        if (rs3 != null)
                                            try {
                                                rs3.close();
                                            } catch (Throwable throwable1) {
                                                throwable.addSuppressed(throwable1);
                                            }
                                        throw throwable;
                                    }
                                    if (ps3 != null)
                                        ps3.close();
                                } catch (Throwable throwable) {
                                    if (ps3 != null)
                                        try {
                                            ps3.close();
                                        } catch (Throwable throwable1) {
                                            throwable.addSuppressed(throwable1);
                                        }
                                    throw throwable;
                                }
                            }
                            slot.setBaseClassId((baseClassId == 0 && activeClassId > 0) ? activeClassId : baseClassId);
                            slot.setDeleteTimer(deleteTime);
                            slot.setLastAccess(rs.getLong("lastAccess"));
                            list.add(slot);
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
                    CharSelectSlot[] arrayOfCharSelectSlot = list.toArray(new CharSelectSlot[0]);
                    if (ps != null)
                        ps.close();
                    if (con != null)
                        con.close();
                    return arrayOfCharSelectSlot;
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
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
            LOGGER.error("Couldn't restore player slots for account {}.", e, loginName);
            return new CharSelectSlot[0];
        }
    }

    protected final void writeImpl() {
        int size = this._slots.length;
        writeC(19);
        writeD(size);
        long lastAccess = 0L;
        if (this._activeId == -1)
            for (int j = 0; j < size; j++) {
                if (lastAccess < this._slots[j].getLastAccess()) {
                    lastAccess = this._slots[j].getLastAccess();
                    this._activeId = j;
                }
            }
        for (int i = 0; i < size; i++) {
            CharSelectSlot slot = this._slots[i];
            Player _player = Player.restoreCharInfo(slot.getObjectId());
            writeS(slot.getName());
            writeD(slot.getCharId());
            writeS(this._loginName);
            writeD(this._sessionId);
            writeD(slot.getClanId());
            writeD(0);
            writeD(slot.getSex());
            writeD(slot.getRace());
            writeD(slot.getBaseClassId());
            writeD(1);
            writeD(slot.getX());
            writeD(slot.getY());
            writeD(slot.getZ());
            writeF(slot.getCurrentHp());
            writeF(slot.getCurrentMp());
            writeD(slot.getSp());
            writeQ(slot.getExp());
            writeD(slot.getLevel());
            writeD(slot.getKarma());
            writeD(slot.getPkKills());
            writeD(slot.getPvPKills());
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(slot.getPaperdollObjectId(16));
            writeD(slot.getPaperdollObjectId(2));
            writeD(slot.getPaperdollObjectId(1));
            writeD(slot.getPaperdollObjectId(3));
            writeD(slot.getPaperdollObjectId(5));
            writeD(slot.getPaperdollObjectId(4));
            writeD(slot.getPaperdollObjectId(6));

            if (_player.getWeaponSkinOption() > 0 && getWeaponOption(_player.getWeaponSkinOption()) != null) {
                writeD((getWeaponOption(_player.getWeaponSkinOption()).getWeaponId() != 0) ? getWeaponOption(_player.getWeaponSkinOption()).getWeaponId() : slot.getPaperdollObjectId(7));
            } else {
                writeD(slot.getPaperdollObjectId(7));
            }
            if (_player.getShieldSkinOption() > 0 && getShieldOption(_player.getShieldSkinOption()) != null) {
                writeD((getShieldOption(_player.getShieldSkinOption()).getShieldId() != 0) ? getShieldOption(_player.getShieldSkinOption()).getShieldId() : slot.getPaperdollObjectId(8));
            } else {
                writeD(slot.getPaperdollObjectId(8));
            }
            if (_player.getArmorSkinOption() > 0 && getArmorOption(_player.getArmorSkinOption()) != null) {
                writeD((getArmorOption(_player.getArmorSkinOption()).getGlovesId() != 0) ? getArmorOption(_player.getArmorSkinOption()).getGlovesId() : slot.getPaperdollObjectId(9));
                writeD((getArmorOption(_player.getArmorSkinOption()).getChestId() != 0) ? getArmorOption(_player.getArmorSkinOption()).getChestId() : slot.getPaperdollObjectId(10));
                writeD((getArmorOption(_player.getArmorSkinOption()).getLegsId() != 0) ? getArmorOption(_player.getArmorSkinOption()).getLegsId() : slot.getPaperdollObjectId(11));
                writeD((getArmorOption(_player.getArmorSkinOption()).getFeetId() != 0) ? getArmorOption(_player.getArmorSkinOption()).getFeetId() : slot.getPaperdollObjectId(12));
            } else {
                writeD(slot.getPaperdollObjectId(9));
                writeD(slot.getPaperdollObjectId(10));
                writeD(slot.getPaperdollObjectId(11));
                writeD(slot.getPaperdollObjectId(12));
            }
            writeD(slot.getPaperdollObjectId(13));

            if (_player.getWeaponSkinOption() > 0 && getWeaponOption(_player.getWeaponSkinOption()) != null) {
                writeD((getWeaponOption(_player.getWeaponSkinOption()).getWeaponId() != 0) ? getWeaponOption(_player.getWeaponSkinOption()).getWeaponId() : slot.getPaperdollObjectId(7));
            } else {
                writeD(slot.getPaperdollObjectId(7));
            }
            if (_player.getHairSkinOption() > 0 && getHairOption(_player.getHairSkinOption()) != null) {
                writeD((getHairOption(_player.getHairSkinOption()).getHairId() != 0) ? getHairOption(_player.getHairSkinOption()).getHairId() : slot.getPaperdollObjectId(15));
            } else {
                writeD(slot.getPaperdollObjectId(15));
            }
            if (_player.getFaceSkinOption() > 0 && getFaceOption(_player.getFaceSkinOption()) != null) {
                writeD((getFaceOption(_player.getFaceSkinOption()).getFaceId() != 0) ? getFaceOption(_player.getFaceSkinOption()).getFaceId() : slot.getPaperdollObjectId(14));
            } else {
                writeD(slot.getPaperdollObjectId(14));
            }

            writeD(slot.getPaperdollItemId(16));
            writeD(slot.getPaperdollItemId(2));
            writeD(slot.getPaperdollItemId(1));
            writeD(slot.getPaperdollItemId(3));
            writeD(slot.getPaperdollItemId(5));
            writeD(slot.getPaperdollItemId(4));
            writeD(slot.getPaperdollItemId(6));
            if (_player.getWeaponSkinOption() > 0 && getWeaponOption(_player.getWeaponSkinOption()) != null) {
                writeD((getWeaponOption(_player.getWeaponSkinOption()).getWeaponId() != 0) ? getWeaponOption(_player.getWeaponSkinOption()).getWeaponId() : _player.getInventory().getPaperdollItemId(7));
            } else {
                writeD(_player.getInventory().getPaperdollItemId(7));
            }
            writeD(slot.getPaperdollItemId(8));
            if (_player.getArmorSkinOption() > 0 && getArmorOption(_player.getArmorSkinOption()) != null) {
                writeD((getArmorOption(_player.getArmorSkinOption()).getGlovesId() != 0) ? getArmorOption(_player.getArmorSkinOption()).getGlovesId() : slot.getPaperdollItemId(9));
                writeD((getArmorOption(_player.getArmorSkinOption()).getChestId() != 0) ? getArmorOption(_player.getArmorSkinOption()).getChestId() : slot.getPaperdollItemId(10));
                writeD((getArmorOption(_player.getArmorSkinOption()).getLegsId() != 0) ? getArmorOption(_player.getArmorSkinOption()).getLegsId() : slot.getPaperdollItemId(11));
                writeD((getArmorOption(_player.getArmorSkinOption()).getFeetId() != 0) ? getArmorOption(_player.getArmorSkinOption()).getFeetId() : slot.getPaperdollItemId(12));
            } else {
                writeD(slot.getPaperdollItemId(9));
                writeD(slot.getPaperdollItemId(10));
                writeD(slot.getPaperdollItemId(11));
                writeD(slot.getPaperdollItemId(12));
            }
            writeD(slot.getPaperdollItemId(13));
            if (_player.getWeaponSkinOption() > 0 && getWeaponOption(_player.getWeaponSkinOption()) != null) {
                writeD((getWeaponOption(_player.getWeaponSkinOption()).getWeaponId() != 0) ? getWeaponOption(_player.getWeaponSkinOption()).getWeaponId() : _player.getInventory().getPaperdollItemId(7));
            } else {
                writeD(_player.getInventory().getPaperdollItemId(7));
            }
            if (_player.getHairSkinOption() > 0 && getHairOption(_player.getHairSkinOption()) != null) {
                writeD((getHairOption(_player.getHairSkinOption()).getHairId() != 0) ? getHairOption(_player.getHairSkinOption()).getHairId() : slot.getPaperdollItemId(15));
            } else {
                writeD(slot.getPaperdollItemId(15));
            }
            if (_player.getFaceSkinOption() > 0 && getFaceOption(_player.getFaceSkinOption()) != null) {
                writeD((getFaceOption(_player.getFaceSkinOption()).getFaceId() != 0) ? getFaceOption(_player.getFaceSkinOption()).getFaceId() : slot.getPaperdollItemId(14));
            } else {
                writeD(slot.getPaperdollItemId(14));
            }
            writeD(slot.getHairStyle());
            writeD(slot.getHairColor());
            writeD(slot.getFace());
            writeF(slot.getMaxHp());
            writeF(slot.getMaxMp());
            writeD((slot.getAccessLevel() > -1) ? ((slot.getDeleteTimer() > 0L) ? (int) ((slot.getDeleteTimer() - System.currentTimeMillis()) / 1000L) : 0) : -1);
            writeD(slot.getClassId());
            writeD((i == this._activeId) ? 1 : 0);
            writeC(Math.min(127, slot.getEnchantEffect()));
            writeD(slot.getAugmentationId());
        }
        getClient().setCharSelectSlot(this._slots);
    }

    public CharSelectSlot[] getCharacterSlots() {
        return this._slots;
    }

    public SkinPackage getArmorOption(int option) {
        return DressMeData.getInstance().getArmorSkinsPackage(option);
    }

    public SkinPackage getWeaponOption(int option) {
        return DressMeData.getInstance().getWeaponSkinsPackage(option);
    }

    public SkinPackage getHairOption(int option) {
        return DressMeData.getInstance().getHairSkinsPackage(option);
    }

    public SkinPackage getFaceOption(int option) {
        return DressMeData.getInstance().getFaceSkinsPackage(option);
    }

    public SkinPackage getShieldOption(int option) {
        return DressMeData.getInstance().getShieldSkinsPackage(option);
    }
}
