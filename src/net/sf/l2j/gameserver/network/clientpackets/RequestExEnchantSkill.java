package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.SkillTreeData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.holder.skillnode.EnchantSkillNode;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public final class RequestExEnchantSkill extends L2GameClientPacket {
    private int _skillId;

    private int _skillLevel;

    protected void readImpl() {
        this._skillId = readD();
        this._skillLevel = readD();
    }

    protected void runImpl() {
        if (this._skillId <= 0 || this._skillLevel <= 0)
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.getClassId().level() < 3 || player.getLevel() < 76)
            return;
        Folk folk = player.getCurrentFolk();
        if (folk == null || !folk.canInteract(player))
            return;
        if (player.getSkillLevel(this._skillId) >= this._skillLevel)
            return;
        L2Skill skill = SkillTable.getInstance().getInfo(this._skillId, this._skillLevel);
        if (skill == null)
            return;
        EnchantSkillNode esn = SkillTreeData.getInstance().getEnchantSkillFor(player, this._skillId, this._skillLevel);
        if (esn == null)
            return;
        if (player.getSp() < esn.getSp()) {
            player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
            return;
        }
        if (player.getExp() - esn.getExp() < player.getStat().getExpForLevel(76)) {
            player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ENOUGH_EXP_TO_ENCHANT_THAT_SKILL);
            return;
        }
        if (Config.ES_SP_BOOK_NEEDED && esn.getItem() != null && !player.destroyItemByItemId("SkillEnchant", esn.getItem().getId(), esn.getItem().getValue(), folk, true)) {
            player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
            return;
        }
        player.removeExpAndSp(esn.getExp(), esn.getSp());
        int skillLevel = this._skillLevel;
        if (Rnd.get(100) <= esn.getEnchantRate(player.getLevel())) {
            player.addSkill(skill, true, true);
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1).addSkillName(this._skillId, this._skillLevel));
        } else {
            skillLevel = SkillTable.getInstance().getMaxLevel(this._skillId);
            player.addSkill(SkillTable.getInstance().getInfo(this._skillId, skillLevel), true, true);
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ENCHANT_THE_SKILL_S1).addSkillName(this._skillId, this._skillLevel));
        }
        player.sendSkillList();
        player.sendPacket(new UserInfo(player));
        folk.showEnchantSkillList(player);
    }
}
