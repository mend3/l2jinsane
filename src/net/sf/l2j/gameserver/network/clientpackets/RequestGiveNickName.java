package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class RequestGiveNickName extends L2GameClientPacket {
    private String _target;

    private String _title;

    protected void readImpl() {
        this._target = readS();
        this._title = readS();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (!StringUtil.isValidString(this._title, "^[a-zA-Z0-9 !@#$&()\\-`.+,/\"]*{0,16}$")) {
            activeChar.sendPacket(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
            return;
        }
        if (activeChar.isNoble() && this._target.matches(activeChar.getName())) {
            activeChar.setTitle(this._title);
            activeChar.sendPacket(SystemMessageId.TITLE_CHANGED);
            activeChar.broadcastTitleInfo();
        } else {
            if ((activeChar.getClanPrivileges() & 0x4) != 4) {
                activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (activeChar.getClan().getLevel() < 3) {
                activeChar.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE);
                return;
            }
            ClanMember member = activeChar.getClan().getClanMember(this._target);
            if (member != null) {
                Player playerMember = member.getPlayerInstance();
                if (playerMember != null) {
                    playerMember.setTitle(this._title);
                    playerMember.sendPacket(SystemMessageId.TITLE_CHANGED);
                    if (activeChar != playerMember)
                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_TITLE_CHANGED_TO_S2).addCharName(playerMember).addString(this._title));
                    playerMember.broadcastTitleInfo();
                } else {
                    activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
                }
            } else {
                activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
            }
        }
    }
}
