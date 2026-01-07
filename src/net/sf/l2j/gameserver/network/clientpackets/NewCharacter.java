package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.xml.PlayerClassData;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.network.serverpackets.CharTemplates;

public final class NewCharacter extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        CharTemplates ct = new CharTemplates();
        ct.addChar(PlayerClassData.getInstance().getTemplate(0));
        ct.addChar(PlayerClassData.getInstance().getTemplate(ClassId.HUMAN_FIGHTER));
        ct.addChar(PlayerClassData.getInstance().getTemplate(ClassId.HUMAN_MYSTIC));
        ct.addChar(PlayerClassData.getInstance().getTemplate(ClassId.ELVEN_FIGHTER));
        ct.addChar(PlayerClassData.getInstance().getTemplate(ClassId.ELVEN_MYSTIC));
        ct.addChar(PlayerClassData.getInstance().getTemplate(ClassId.DARK_FIGHTER));
        ct.addChar(PlayerClassData.getInstance().getTemplate(ClassId.DARK_MYSTIC));
        ct.addChar(PlayerClassData.getInstance().getTemplate(ClassId.ORC_FIGHTER));
        ct.addChar(PlayerClassData.getInstance().getTemplate(ClassId.ORC_MYSTIC));
        ct.addChar(PlayerClassData.getInstance().getTemplate(ClassId.DWARVEN_FIGHTER));
        sendPacket(ct);
    }
}
