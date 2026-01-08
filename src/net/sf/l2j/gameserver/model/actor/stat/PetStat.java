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
        if (!super.addExp(value)) {
            return false;
        } else {
            this.getActiveChar().updateAndBroadcastStatus(1);
            return true;
        }
    }

    public boolean addExpAndSp(long addToExp, int addToSp) {
        if (!super.addExpAndSp(addToExp, addToSp)) {
            return false;
        } else {
            this.getActiveChar().getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_EARNED_S1_EXP).addNumber((int) addToExp));
            return true;
        }
    }

    public final boolean addLevel(byte value) {
        if (this.getLevel() + value > this.getMaxLevel() - 1) {
            return false;
        } else {
            boolean levelIncreased = super.addLevel(value);
            if (levelIncreased) {
                this.getActiveChar().broadcastPacket(new SocialAction(this.getActiveChar(), 15));
            }

            return levelIncreased;
        }
    }

    public final long getExpForLevel(int level) {
        return this.getActiveChar().getTemplate().getPetDataEntry(level).getMaxExp();
    }

    public Pet getActiveChar() {
        return (Pet) super.getActiveChar();
    }

    public void setLevel(byte value) {
        this.getActiveChar().setPetData(value);
        super.setLevel(value);
        ItemInstance controlItem = this.getActiveChar().getControlItem();
        if (controlItem != null && controlItem.getEnchantLevel() != this.getLevel()) {
            this.getActiveChar().sendPetInfosToOwner();
            controlItem.setEnchantLevel(this.getLevel());
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(controlItem);
            this.getActiveChar().getOwner().sendPacket(iu);
        }

    }

    public int getMaxHp() {
        return (int) this.calcStat(Stats.MAX_HP, this.getActiveChar().getPetData().getMaxHp(), null, null);
    }

    public int getMaxMp() {
        return (int) this.calcStat(Stats.MAX_MP, this.getActiveChar().getPetData().getMaxMp(), null, null);
    }

    public int getMAtk(Creature target, L2Skill skill) {
        double attack = this.getActiveChar().getPetData().getMAtk();
        if (skill != null) {
            attack += skill.getPower();
        }

        return (int) this.calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
    }

    public int getMAtkSpd() {
        double base = 333.0F;
        if (this.getActiveChar().checkHungryState()) {
            base /= 2.0F;
        }

        return (int) this.calcStat(Stats.MAGIC_ATTACK_SPEED, base, null, null);
    }

    public int getMDef(Creature target, L2Skill skill) {
        return (int) this.calcStat(Stats.MAGIC_DEFENCE, this.getActiveChar().getPetData().getMDef(), target, skill);
    }

    public int getPAtk(Creature target) {
        return (int) this.calcStat(Stats.POWER_ATTACK, this.getActiveChar().getPetData().getPAtk(), target, null);
    }

    public int getPAtkSpd() {
        double base = this.getActiveChar().getTemplate().getBasePAtkSpd();
        if (this.getActiveChar().checkHungryState()) {
            base /= 2.0F;
        }

        return (int) this.calcStat(Stats.POWER_ATTACK_SPEED, base, null, null);
    }

    public int getPDef(Creature target) {
        return (int) this.calcStat(Stats.POWER_DEFENCE, this.getActiveChar().getPetData().getPDef(), target, null);
    }
}
