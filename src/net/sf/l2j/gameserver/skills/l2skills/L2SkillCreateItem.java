package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class L2SkillCreateItem extends L2Skill {
    private final int[] _createItemId;
    private final int _createItemCount;
    private final int _randomCount;

    public L2SkillCreateItem(StatSet set) {
        super(set);
        this._createItemId = set.getIntegerArray("create_item_id");
        this._createItemCount = set.getInteger("create_item_count", 0);
        this._randomCount = set.getInteger("random_count", 1);
    }

    public void useSkill(Creature activeChar, WorldObject[] targets) {
        Player player = activeChar.getActingPlayer();
        if (!activeChar.isAlikeDead()) {
            if (activeChar instanceof Playable) {
                if (this._createItemId == null || this._createItemCount == 0) {
                    SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
                    sm.addSkillName(this);
                    activeChar.sendPacket(sm);
                    return;
                }

                int count = this._createItemCount + Rnd.get(this._randomCount);
                int rndid = Rnd.get(this._createItemId.length);
                if (activeChar instanceof Player) {
                    player.addItem("Skill", this._createItemId[rndid], count, activeChar, true);
                } else if (activeChar instanceof Pet) {
                    activeChar.getInventory().addItem("Skill", this._createItemId[rndid], count, player, activeChar);
                    player.sendPacket(new PetItemList((Pet) activeChar));
                }
            }

        }
    }
}
