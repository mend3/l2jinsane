package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class PetStat extends SummonStat {
    public PetStat(Pet activeChar) {
        super(activeChar);
    }

    public boolean addExp(int value) {
        if (!addExp(value))
            return false;
        getActiveChar().updateAndBroadcastStatus(1);
        return true;
    }

    public boolean addExpAndSp(long addToExp, int addToSp) {
        if (!super.addExpAndSp(addToExp, addToSp))
            return false;
        getActiveChar().getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_EARNED_S1_EXP).addNumber((int) addToExp));
        return true;
    }

    public final boolean addLevel(byte value) {
        if (getLevel() + value > getMaxLevel() - 1)
            return false;
        boolean levelIncreased = super.addLevel(value);
        if (levelIncreased)
            getActiveChar().broadcastPacket(new SocialAction(getActiveChar(), 15));
        return levelIncreased;
    }

    public final long getExpForLevel(int level) {
        return getActiveChar().getTemplate().getPetDataEntry(level).getMaxExp();
    }

    public Pet getActiveChar() {
        return (Pet) super.getActiveChar();
    }

    public void setLevel(byte value) {
        getActiveChar().setPetData(value);
        super.setLevel(value);
        ItemInstance controlItem = getActiveChar().getControlItem();
        if (controlItem != null && controlItem.getEnchantLevel() != getLevel()) {
            getActiveChar().sendPetInfosToOwner();
            controlItem.setEnchantLevel(getLevel());
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(controlItem);
            getActiveChar().getOwner().sendPacket(iu);
        }
    }

    public int getMaxHp() {
        return (int) calcStat(Stats.MAX_HP, getActiveChar().getPetData().getMaxHp(), null, null);
    }

    public int getMaxMp() {
        return (int) calcStat(Stats.MAX_MP, getActiveChar().getPetData().getMaxMp(), null, null);
    }

    public int getMAtk(Creature target, L2Skill skill) {
        double attack = getActiveChar().getPetData().getMAtk();
        if (skill != null)
            attack += skill.getPower();
        return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
    }

    public int getMAtkSpd() {
        double base = 333.0D;
        if (getActiveChar().checkHungryState())
            base /= 2.0D;
        return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, base, null, null);
    }

    public int getMDef(Creature target, L2Skill skill) {
        return (int) calcStat(Stats.MAGIC_DEFENCE, getActiveChar().getPetData().getMDef(), target, skill);
    }

    public int getPAtk(Creature target) {
        return (int) calcStat(Stats.POWER_ATTACK, getActiveChar().getPetData().getPAtk(), target, null);
    }

    public int getPAtkSpd() {
        double base = getActiveChar().getTemplate().getBasePAtkSpd();
        if (getActiveChar().checkHungryState())
            base /= 2.0D;
        return (int) calcStat(Stats.POWER_ATTACK_SPEED, base, null, null);
    }

    public int getPDef(Creature target) {
        return (int) calcStat(Stats.POWER_DEFENCE, getActiveChar().getPetData().getPDef(), target, null);
    }
}
