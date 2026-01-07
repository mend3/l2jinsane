package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;

public class PetStatusUpdate extends L2GameServerPacket {
    private final Summon _summon;

    private final int _maxHp;

    private final int _maxMp;

    private int _maxFed;

    private int _curFed;

    public PetStatusUpdate(Summon summon) {
        this._summon = summon;
        this._maxHp = this._summon.getMaxHp();
        this._maxMp = this._summon.getMaxMp();
        if (this._summon instanceof Pet pet) {
            this._curFed = pet.getCurrentFed();
            this._maxFed = pet.getPetData().getMaxMeal();
        } else if (this._summon instanceof Servitor sum) {
            this._curFed = sum.getTimeRemaining();
            this._maxFed = sum.getTotalLifeTime();
        }
    }

    protected final void writeImpl() {
        writeC(181);
        writeD(this._summon.getSummonType());
        writeD(this._summon.getObjectId());
        writeD(this._summon.getX());
        writeD(this._summon.getY());
        writeD(this._summon.getZ());
        writeS(this._summon.getTitle());
        writeD(this._curFed);
        writeD(this._maxFed);
        writeD((int) this._summon.getCurrentHp());
        writeD(this._maxHp);
        writeD((int) this._summon.getCurrentMp());
        writeD(this._maxMp);
        writeD(this._summon.getLevel());
        writeQ(this._summon.getStat().getExp());
        writeQ(this._summon.getExpForThisLevel());
        writeQ(this._summon.getExpForNextLevel());
    }
}
