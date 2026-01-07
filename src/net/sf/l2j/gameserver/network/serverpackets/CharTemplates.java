package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate;

import java.util.ArrayList;
import java.util.List;

public class CharTemplates extends L2GameServerPacket {
    private final List<PlayerTemplate> _chars = new ArrayList<>();

    public void addChar(PlayerTemplate template) {
        this._chars.add(template);
    }

    protected final void writeImpl() {
        writeC(23);
        writeD(this._chars.size());
        for (PlayerTemplate temp : this._chars) {
            if (temp == null)
                continue;
            writeD(temp.getRace().ordinal());
            writeD(temp.getClassId().getId());
            writeD(70);
            writeD(temp.getBaseSTR());
            writeD(10);
            writeD(70);
            writeD(temp.getBaseDEX());
            writeD(10);
            writeD(70);
            writeD(temp.getBaseCON());
            writeD(10);
            writeD(70);
            writeD(temp.getBaseINT());
            writeD(10);
            writeD(70);
            writeD(temp.getBaseWIT());
            writeD(10);
            writeD(70);
            writeD(temp.getBaseMEN());
            writeD(10);
        }
    }
}
