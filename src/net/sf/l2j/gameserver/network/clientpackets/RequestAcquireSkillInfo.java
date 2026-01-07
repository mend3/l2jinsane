package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.SkillTreeData;
import net.sf.l2j.gameserver.data.xml.SpellbookData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.holder.skillnode.ClanSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.FishingSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.network.serverpackets.AcquireSkillInfo;

public class RequestAcquireSkillInfo extends L2GameClientPacket {
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
                if (!folk.getTemplate().canTeach(player.getClassId()))
                    return;
                gsn = player.getTemplate().findSkill(this._skillId, this._skillLevel);
                if (gsn != null) {
                    AcquireSkillInfo asi = new AcquireSkillInfo(this._skillId, this._skillLevel, gsn.getCorrectedCost(), 0);
                    int bookId = SpellbookData.getInstance().getBookForSkill(this._skillId, this._skillLevel);
                    if (bookId != 0)
                        asi.addRequirement(99, bookId, 1, 50);
                    sendPacket(asi);
                }
                break;
            case 1:
                skillLvl = player.getSkillLevel(this._skillId);
                if (skillLvl >= this._skillLevel)
                    return;
                if (skillLvl != this._skillLevel - 1)
                    return;
                fsn = SkillTreeData.getInstance().getFishingSkillFor(player, this._skillId, this._skillLevel);
                if (fsn != null) {
                    AcquireSkillInfo asi = new AcquireSkillInfo(this._skillId, this._skillLevel, 0, 1);
                    asi.addRequirement(4, fsn.getItemId(), fsn.getItemCount(), 0);
                    sendPacket(asi);
                }
                break;
            case 2:
                if (!player.isClanLeader())
                    return;
                csn = SkillTreeData.getInstance().getClanSkillFor(player, this._skillId, this._skillLevel);
                if (csn != null) {
                    AcquireSkillInfo asi = new AcquireSkillInfo(skill.getId(), skill.getLevel(), csn.getCost(), 2);
                    if (Config.LIFE_CRYSTAL_NEEDED && csn.getItemId() != 0)
                        asi.addRequirement(1, csn.getItemId(), 1, 0);
                    sendPacket(asi);
                }
                break;
        }
    }
}
