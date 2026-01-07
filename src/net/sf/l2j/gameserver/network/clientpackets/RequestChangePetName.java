package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class RequestChangePetName extends L2GameClientPacket {
    private static final String SEARCH_NAME = "SELECT name FROM pets WHERE name=?";

    private String _name;

    private static boolean doesPetNameExist(String name) {
        boolean result = true;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT name FROM pets WHERE name=?");
                try {
                    ps.setString(1, name);
                    ResultSet rs = ps.executeQuery();
                    try {
                        result = rs.next();
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
        } catch (SQLException e) {
            LOGGER.error("Couldn't check existing petname.", e);
        }
        return result;
    }

    protected void readImpl() {
        this._name = readS();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (!player.hasPet())
            return;
        if (this._name.length() < 2 || this._name.length() > 8) {
            player.sendPacket(SystemMessageId.NAMING_PETNAME_UP_TO_8CHARS);
            return;
        }
        Pet pet = (Pet) player.getSummon();
        if (pet.getName() != null) {
            player.sendPacket(SystemMessageId.NAMING_YOU_CANNOT_SET_NAME_OF_THE_PET);
            return;
        }
        if (!StringUtil.isValidString(this._name, "^[A-Za-z0-9]{2,8}$")) {
            player.sendPacket(SystemMessageId.NAMING_PETNAME_CONTAINS_INVALID_CHARS);
            return;
        }
        if (NpcData.getInstance().getTemplateByName(this._name) != null)
            return;
        if (doesPetNameExist(this._name)) {
            player.sendPacket(SystemMessageId.NAMING_ALREADY_IN_USE_BY_ANOTHER_PET);
            return;
        }
        pet.setName(this._name);
        pet.sendPetInfosToOwner();
    }
}
