package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;

public class MagicSkillLaunched extends L2GameServerPacket {
    private final int _charObjId;

    private final int _skillId;

    private final int _skillLevel;

    private final int _numberOfTargets;
    private final int _singleTargetId;
    private WorldObject[] _targets;

    public MagicSkillLaunched(Creature cha, int skillId, int skillLevel, WorldObject[] targets) {
        this._charObjId = cha.getObjectId();
        this._skillId = skillId;
        this._skillLevel = skillLevel;
        this._numberOfTargets = targets.length;
        this._targets = targets;
        this._singleTargetId = 0;
    }

    public MagicSkillLaunched(Creature cha, int skillId, int skillLevel) {
        this._charObjId = cha.getObjectId();
        this._skillId = skillId;
        this._skillLevel = skillLevel;
        this._numberOfTargets = 1;
        this._singleTargetId = cha.getTargetId();
    }

    protected final void writeImpl() {
        writeC(118);
        writeD(this._charObjId);
        writeD(this._skillId);
        writeD(this._skillLevel);
        writeD(this._numberOfTargets);
        if (this._singleTargetId != 0 || this._numberOfTargets == 0) {
            writeD(this._singleTargetId);
        } else {
            for (WorldObject target : this._targets) {
                try {
                    writeD(target.getObjectId());
                } catch (NullPointerException e) {
                    writeD(0);
                }
            }
        }
    }
}
