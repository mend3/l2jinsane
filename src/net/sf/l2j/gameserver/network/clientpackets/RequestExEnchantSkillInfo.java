package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.SkillTreeData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.holder.skillnode.EnchantSkillNode;
import net.sf.l2j.gameserver.network.serverpackets.ExEnchantSkillInfo;

public final class RequestExEnchantSkillInfo extends L2GameClientPacket {
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
        if (!folk.getTemplate().canTeach(player.getClassId()))
            return;
        EnchantSkillNode esn = SkillTreeData.getInstance().getEnchantSkillFor(player, this._skillId, this._skillLevel);
        if (esn == null)
            return;
        ExEnchantSkillInfo esi = new ExEnchantSkillInfo(this._skillId, this._skillLevel, esn.getSp(), esn.getExp(), esn.getEnchantRate(player.getLevel()));
        if (Config.ES_SP_BOOK_NEEDED && esn.getItem() != null)
            esi.addRequirement(4, esn.getItem().getId(), esn.getItem().getValue(), 0);
        sendPacket(esi);
    }
}
