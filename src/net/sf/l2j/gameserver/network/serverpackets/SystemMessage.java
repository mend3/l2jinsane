package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.Arrays;

public final class SystemMessage extends L2GameServerPacket {
    private static final SMParam[] EMPTY_PARAM_ARRAY = new SMParam[0];

    private static final byte TYPE_ZONE_NAME = 7;

    private static final byte TYPE_ITEM_NUMBER = 6;

    private static final byte TYPE_CASTLE_NAME = 5;

    private static final byte TYPE_SKILL_NAME = 4;

    private static final byte TYPE_ITEM_NAME = 3;

    private static final byte TYPE_NPC_NAME = 2;

    private static final byte TYPE_NUMBER = 1;

    private static final byte TYPE_TEXT = 0;

    private final SystemMessageId _smId;

    private SMParam[] _params;

    private int _paramIndex;

    public SystemMessage(SystemMessageId smId) {
        int paramCount = smId.getParamCount();
        this._smId = smId;
        this._params = (paramCount != 0) ? new SMParam[paramCount] : EMPTY_PARAM_ARRAY;
    }

    public static SystemMessage sendString(String text) {
        if (text == null)
            throw new NullPointerException();
        return getSystemMessage(SystemMessageId.S1).addString(text);
    }

    public static SystemMessage getSystemMessage(SystemMessageId smId) {
        SystemMessage sm = smId.getStaticSystemMessage();
        if (sm != null)
            return sm;
        sm = new SystemMessage(smId);
        if (smId.getParamCount() == 0)
            smId.setStaticSystemMessage(sm);
        return sm;
    }

    public static SystemMessage getSystemMessage(int id) {
        return getSystemMessage(SystemMessageId.getSystemMessageId(id));
    }

    private void append(SMParam param) {
        if (this._paramIndex >= this._params.length) {
            this._params = Arrays.copyOf(this._params, this._paramIndex + 1);
            this._smId.setParamCount(this._paramIndex + 1);
            LOGGER.warn("Wrong parameter count '{}' for {}.", this._paramIndex + 1, this._smId);
        }
        this._params[this._paramIndex++] = param;
    }

    public SystemMessage addString(String text) {
        append(new SMParam((byte) 0, text));
        return this;
    }

    public SystemMessage addFortId(int number) {
        append(new SMParam((byte) 5, number));
        return this;
    }

    public SystemMessage addNumber(int number) {
        append(new SMParam((byte) 1, number));
        return this;
    }

    public SystemMessage addItemNumber(int number) {
        append(new SMParam((byte) 6, number));
        return this;
    }

    public SystemMessage addCharName(Creature cha) {
        return addString(cha.getName());
    }

    public SystemMessage addItemName(ItemInstance item) {
        return addItemName(item.getItem().getItemId());
    }

    public SystemMessage addItemName(Item item) {
        return addItemName(item.getItemId());
    }

    public SystemMessage addItemName(int id) {
        append(new SMParam((byte) 3, id));
        return this;
    }

    public SystemMessage addZoneName(Location loc) {
        append(new SMParam((byte) 7, loc));
        return this;
    }

    public void addSkillName(L2Effect effect) {
        addSkillName(effect.getSkill());
    }

    public SystemMessage addSkillName(L2Skill skill) {
        return addSkillName(skill.getId(), skill.getLevel());
    }

    public SystemMessage addSkillName(int id) {
        return addSkillName(id, 1);
    }

    public SystemMessage addSkillName(int id, int lvl) {
        append(new SMParam((byte) 4, new IntIntHolder(id, lvl)));
        return this;
    }

    public SystemMessageId getSystemMessageId() {
        return this._smId;
    }

    protected void writeImpl() {
        writeC(100);
        writeD(this._smId.getId());
        writeD(this._paramIndex);
        for (int i = 0; i < this._paramIndex; i++) {
            IntIntHolder info;
            SMParam param = this._params[i];
            writeD(param.getType());
            switch (param.getType()) {
                case 0:
                    writeS((String) param.getObject());
                    break;
                case 1:
                case 2:
                case 3:
                case 5:
                case 6:
                    writeD((Integer) param.getObject());
                    break;
                case 4:
                    info = (IntIntHolder) param.getObject();
                    writeD(info.getId());
                    writeD(info.getValue());
                    break;
                case 7:
                    writeLoc((Location) param.getObject());
                    break;
            }
        }
    }

    private record SMParam(byte _type, Object _value) {

        public int getType() {
            return this._type;
        }

        public Object getObject() {
            return this._value;
        }
    }
}
