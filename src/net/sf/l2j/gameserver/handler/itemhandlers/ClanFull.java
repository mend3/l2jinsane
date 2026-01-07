package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class ClanFull implements IItemHandler {
    private final int reputation = 150000;

    private final byte level = 8;

    private final int[] clanSkills = new int[]{
            370, 371, 372, 373, 374, 375, 376, 377, 378, 379,
            380, 381, 382, 383, 384, 385, 386, 387, 388, 389,
            390, 391};

    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player activeChar))
            return;
        if (activeChar.isClanLeader()) {
            if (activeChar.getClan().getLevel() == 8) {
                activeChar.sendMessage("Your clan is already maximum level!");
                return;
            }
            activeChar.getClan().changeLevel(8);
            activeChar.getClan().addReputationScore(150000);
            for (int s : this.clanSkills) {
                L2Skill clanSkill = SkillTable.getInstance().getInfo(s, SkillTable.getInstance().getMaxLevel(s));
                activeChar.getClan().addNewSkill(clanSkill);
            }
            activeChar.sendSkillList();
            activeChar.getClan().updateClanInDB();
            activeChar.sendMessage("Your clan Level/Skills/Reputation has been updated!");
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
            activeChar.broadcastUserInfo();
            activeChar.sendPacket(new ExShowScreenMessage("player: " + activeChar.getName() + " Your Clan is level 8 and full skills.", 10000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
            activeChar.sendPacket(new MagicSkillUse(activeChar, activeChar, 1034, 1, 1, 1));
        } else {
            activeChar.sendMessage("You are not the clan leader.");
        }
    }
}
