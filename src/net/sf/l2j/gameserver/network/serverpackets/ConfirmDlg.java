package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.ArrayList;
import java.util.List;

public class ConfirmDlg extends L2GameServerPacket {
    private static final int TYPE_ZONE_NAME = 7;

    private static final int TYPE_SKILL_NAME = 4;

    private static final int TYPE_ITEM_NAME = 3;

    private static final int TYPE_NPC_NAME = 2;

    private static final int TYPE_NUMBER = 1;

    private static final int TYPE_TEXT = 0;

    private final int _messageId;

    private final List<CnfDlgData> _info = new ArrayList<>();

    private int _time = 0;

    private int _requesterId = 0;

    public ConfirmDlg(int messageId) {
        this._messageId = messageId;
    }

    public ConfirmDlg(SystemMessageId messageId) {
        this._messageId = messageId.getId();
    }

    public ConfirmDlg addString(String text) {
        this._info.add(new CnfDlgData(0, text));
        return this;
    }

    public ConfirmDlg addNumber(int number) {
        this._info.add(new CnfDlgData(1, Integer.valueOf(number)));
        return this;
    }

    public ConfirmDlg addCharName(Creature cha) {
        return addString(cha.getName());
    }

    public ConfirmDlg addItemName(ItemInstance item) {
        return addItemName(item.getItem().getItemId());
    }

    public ConfirmDlg addItemName(Item item) {
        return addItemName(item.getItemId());
    }

    public ConfirmDlg addItemName(int id) {
        this._info.add(new CnfDlgData(3, Integer.valueOf(id)));
        return this;
    }

    public ConfirmDlg addZoneName(Location loc) {
        this._info.add(new CnfDlgData(7, loc));
        return this;
    }

    public ConfirmDlg addSkillName(L2Effect effect) {
        return addSkillName(effect.getSkill());
    }

    public ConfirmDlg addSkillName(L2Skill skill) {
        return addSkillName(skill.getId(), skill.getLevel());
    }

    public ConfirmDlg addSkillName(int id) {
        return addSkillName(id, 1);
    }

    public ConfirmDlg addSkillName(int id, int lvl) {
        this._info.add(new CnfDlgData(4, new IntIntHolder(id, lvl)));
        return this;
    }

    public ConfirmDlg addTime(int time) {
        this._time = time;
        return this;
    }

    public ConfirmDlg addRequesterId(int id) {
        this._requesterId = id;
        return this;
    }

    protected final void writeImpl() {
        writeC(237);
        writeD(this._messageId);
        if (this._info.isEmpty()) {
            writeD(0);
            writeD(this._time);
            writeD(this._requesterId);
        } else {
            writeD(this._info.size());
            for (CnfDlgData data : this._info) {
                IntIntHolder info;
                writeD(data.getType());
                switch (data.getType()) {
                    case 0:
                        writeS((String) data.getObject());
                    case 1:
                    case 2:
                    case 3:
                        writeD((Integer) data.getObject());
                    case 4:
                        info = (IntIntHolder) data.getObject();
                        writeD(info.getId());
                        writeD(info.getValue());
                    case 7:
                        writeLoc((Location) data.getObject());
                }
            }
            if (this._time != 0)
                writeD(this._time);
            if (this._requesterId != 0)
                writeD(this._requesterId);
        }
    }

    private record CnfDlgData(int _type, Object _value) {

        public int getType() {
                return this._type;
            }

            public Object getObject() {
                return this._value;
            }
        }
}
