package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;

public class PetInfo extends L2GameServerPacket {
    private final Summon _summon;

    private final int _val;

    private int _maxFed;

    private int _curFed;

    public PetInfo(Summon summon, int val) {
        this._summon = summon;
        this._val = val;
        if (this._summon instanceof Pet pet) {
            this._curFed = pet.getCurrentFed();
            this._maxFed = pet.getPetData().getMaxMeal();
        } else if (this._summon instanceof Servitor sum) {
            this._curFed = sum.getTimeRemaining();
            this._maxFed = sum.getTotalLifeTime();
        }
    }

    protected final void writeImpl() {
        writeC(177);
        writeD(this._summon.getSummonType());
        writeD(this._summon.getObjectId());
        writeD(this._summon.getTemplate().getIdTemplate() + 1000000);
        writeD(0);
        writeD(this._summon.getX());
        writeD(this._summon.getY());
        writeD(this._summon.getZ());
        writeD(this._summon.getHeading());
        writeD(0);
        writeD(this._summon.getMAtkSpd());
        writeD(this._summon.getPAtkSpd());
        int runSpd = this._summon.getStat().getBaseRunSpeed();
        int walkSpd = this._summon.getStat().getBaseWalkSpeed();
        writeD(runSpd);
        writeD(walkSpd);
        writeD(runSpd);
        writeD(walkSpd);
        writeD(runSpd);
        writeD(walkSpd);
        writeD(runSpd);
        writeD(walkSpd);
        writeF(this._summon.getStat().getMovementSpeedMultiplier());
        writeF(1.0D);
        writeF(this._summon.getCollisionRadius());
        writeF(this._summon.getCollisionHeight());
        writeD(this._summon.getWeapon());
        writeD(this._summon.getArmor());
        writeD(0);
        writeC((this._summon.getOwner() != null) ? 1 : 0);
        writeC(1);
        writeC(this._summon.isInCombat() ? 1 : 0);
        writeC(this._summon.isAlikeDead() ? 1 : 0);
        writeC(this._summon.isShowSummonAnimation() ? 2 : this._val);
        writeS(this._summon.getName());
        writeS(this._summon.getTitle());
        writeD(1);
        writeD(this._summon.getPvpFlag());
        writeD(this._summon.getKarma());
        writeD(this._curFed);
        writeD(this._maxFed);
        writeD((int) this._summon.getCurrentHp());
        writeD(this._summon.getMaxHp());
        writeD((int) this._summon.getCurrentMp());
        writeD(this._summon.getMaxMp());
        writeD(this._summon.getStat().getSp());
        writeD(this._summon.getLevel());
        writeQ(this._summon.getStat().getExp());
        writeQ(this._summon.getExpForThisLevel());
        writeQ(this._summon.getExpForNextLevel());
        writeD((this._summon instanceof Pet) ? this._summon.getInventory().getTotalWeight() : 0);
        writeD(this._summon.getMaxLoad());
        writeD(this._summon.getPAtk(null));
        writeD(this._summon.getPDef(null));
        writeD(this._summon.getMAtk(null, null));
        writeD(this._summon.getMDef(null, null));
        writeD(this._summon.getAccuracy());
        writeD(this._summon.getEvasionRate(null));
        writeD(this._summon.getCriticalHit(null, null));
        writeD(this._summon.getMoveSpeed());
        writeD(this._summon.getPAtkSpd());
        writeD(this._summon.getMAtkSpd());
        writeD(this._summon.getAbnormalEffect());
        writeH(this._summon.isMountable() ? 1 : 0);
        writeC(this._summon.isInsideZone(ZoneId.WATER) ? 1 : (this._summon.isFlying() ? 2 : 0));
        writeH(0);
        writeC(this._summon.getTeam().getId());
        writeD(this._summon.getSoulShotsPerHit());
        writeD(this._summon.getSpiritShotsPerHit());
    }
}
