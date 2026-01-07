package net.sf.l2j.gameserver.network.gameserverpackets;

import net.sf.l2j.commons.network.AttributeType;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.util.ArrayList;
import java.util.List;

public class ServerStatus extends GameServerBasePacket {
    private static final int ON = 1;
    private static final int OFF = 0;
    private final List<IntIntHolder> _attributes = new ArrayList<>();

    public void addAttribute(AttributeType type, int value) {
        this._attributes.add(new IntIntHolder(type.getId(), value));
    }

    public void addAttribute(AttributeType type, boolean onOrOff) {
        addAttribute(type, onOrOff ? 1 : 0);
    }

    public byte[] getContent() {
        writeC(6);
        writeD(this._attributes.size());
        for (IntIntHolder temp : this._attributes) {
            writeD(temp.getId());
            writeD(temp.getValue());
        }
        return getBytes();
    }
}
