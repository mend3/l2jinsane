package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.SkillTreeData;
import net.sf.l2j.gameserver.data.xml.SpellbookData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Fisherman;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.actor.instance.VillageMaster;
import net.sf.l2j.gameserver.model.holder.skillnode.ClanSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.FishingSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class RequestAcquireSkill extends L2GameClientPacket {
    private int _skillId;

    private int _skillLevel;

    private int _skillType;

    protected void readImpl() {
        this._skillId = readD();
        this._skillLevel = readD();
        this._skillType = readD();
    }

    protected void runImpl() {
        int skillLvl;
        GeneralSkillNode gsn;
        int bookId;
        FishingSkillNode fsn;
        ClanSkillNode csn;
        if (this._skillId <= 0 || this._skillLevel <= 0)
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Folk folk = player.getCurrentFolk();
        if (folk == null || !folk.canInteract(player))
            return;
        L2Skill skill = SkillTable.getInstance().getInfo(this._skillId, this._skillLevel);
        if (skill == null)
            return;
        switch (this._skillType) {
            case 0:
                skillLvl = player.getSkillLevel(this._skillId);
                if (skillLvl >= this._skillLevel)
                    return;
                if (skillLvl != this._skillLevel - 1)
                    return;
                gsn = player.getTemplate().findSkill(this._skillId, this._skillLevel);
                if (gsn == null)
                    return;
                if (player.getSp() < gsn.getCorrectedCost()) {
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
                    folk.showSkillList(player);
                    return;
                }
                bookId = SpellbookData.getInstance().getBookForSkill(this._skillId, this._skillLevel);
                if (bookId > 0 && !player.destroyItemByItemId("SkillLearn", bookId, 1, folk, true)) {
                    player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
                    folk.showSkillList(player);
                    return;
                }
                player.removeExpAndSp(0L, gsn.getCorrectedCost());
                player.addSkill(skill, true, true);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1).addSkillName(skill));
                player.sendSkillList();
                folk.showSkillList(player);
                break;
            case 1:
                skillLvl = player.getSkillLevel(this._skillId);
                if (skillLvl >= this._skillLevel)
                    return;
                if (skillLvl != this._skillLevel - 1)
                    return;
                fsn = SkillTreeData.getInstance().getFishingSkillFor(player, this._skillId, this._skillLevel);
                if (fsn == null)
                    return;
                if (!player.destroyItemByItemId("Consume", fsn.getItemId(), fsn.getItemCount(), folk, true)) {
                    player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
                    Fisherman.showFishSkillList(player);
                    return;
                }
                player.addSkill(skill, true, true);
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEARNED_SKILL_S1).addSkillName(skill));
                if (this._skillId >= 1368 && this._skillId <= 1372)
                    player.sendPacket(new ExStorageMaxCount(player));
                player.sendSkillList();
                Fisherman.showFishSkillList(player);
                break;
            case 2:
                if (!player.isClanLeader())
                    return;
                csn = SkillTreeData.getInstance().getClanSkillFor(player, this._skillId, this._skillLevel);
                if (csn == null)
                    return;
                if (player.getClan().getReputationScore() < csn.getCost()) {
                    player.sendPacket(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
                    VillageMaster.showPledgeSkillList(player);
                    return;
                }
                if (Config.LIFE_CRYSTAL_NEEDED && !player.destroyItemByItemId("Consume", csn.getItemId(), 1, folk, true)) {
                    player.sendPacket(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL);
                    VillageMaster.showPledgeSkillList(player);
                    return;
                }
                player.getClan().takeReputationScore(csn.getCost());
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(csn.getCost()));
                player.getClan().addNewSkill(skill);
                VillageMaster.showPledgeSkillList(player);
        }
    }
}
