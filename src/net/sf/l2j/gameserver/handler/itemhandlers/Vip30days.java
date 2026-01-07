package net.sf.l2j.gameserver.handler.itemhandlers;

import enginemods.main.data.PlayerData;
import enginemods.main.engine.mods.SystemVip;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

public class Vip30days implements IItemHandler {
    protected static final Logger LOGGER = Logger.getLogger(Vip30days.class.getName());

    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player activeChar))
            return;
        if (activeChar.isInOlympiadMode()) {
            activeChar.sendMessage("SYS: You cannot do this.");
            return;
        }
        if (activeChar.isVip()) {
            activeChar.sendMessage("SYS: You are already in Vip status.");
            return;
        }
        activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
        Calendar time = new GregorianCalendar();
        time.add(Calendar.DAY_OF_YEAR, Integer.parseInt("30"));
        SystemVip.getInstance().setValueDB(activeChar, "vip", "" + time.getTimeInMillis());
        PlayerData.get(activeChar).setVip(true);
        PlayerData.get(activeChar).setVipExpireDate(time.getTimeInMillis());
        SystemVip.getInstance().addVip(activeChar, time.getTimeInMillis());
        activeChar.sendPacket(new ExShowScreenMessage("player: " + activeChar.getName() + " is Vip now", 10000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
        SystemVip.informeExpireVip(activeChar);
        L2Skill skill = SkillTable.getInstance().getInfo(2025, 1);
        if (skill != null) {
            MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2025, 1, 1, 0);
            activeChar.sendPacket(MSU);
            activeChar.broadcastPacket(MSU);
            activeChar.useMagic(skill, false, false);
        }
    }
}
