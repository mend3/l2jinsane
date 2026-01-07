package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class RequestPetGetItem extends L2GameClientPacket {
    private int _objectId;

    protected void readImpl() {
        this._objectId = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null || !activeChar.hasPet())
            return;
        WorldObject item = World.getInstance().getObject(this._objectId);
        if (item == null)
            return;
        Pet pet = (Pet) activeChar.getSummon();
        if (pet.isDead() || pet.isOutOfControl()) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        pet.getAI().setIntention(IntentionType.PICK_UP, item);
    }
}
