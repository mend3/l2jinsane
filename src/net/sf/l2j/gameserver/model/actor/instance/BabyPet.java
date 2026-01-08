/**/
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate.SkillType;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.concurrent.Future;

public final class BabyPet extends Pet {
    private IntIntHolder _majorHeal = null;
    private IntIntHolder _minorHeal = null;
    private Future<?> _castTask;

    public BabyPet(int objectId, NpcTemplate template, Player owner, ItemInstance control) {
        super(objectId, template, owner, control);
    }

    public void onSpawn() {
        super.onSpawn();
        double healPower = 0.0F;

        for (L2Skill skill : this.getTemplate().getSkills(SkillType.HEAL)) {
            if (skill.getTargetType() == SkillTargetType.TARGET_OWNER_PET && skill.getSkillType() == L2SkillType.HEAL) {
                int skillLevel = this.getSkillLevel(skill.getId());
                if (skillLevel > 0) {
                    if (healPower == (double) 0.0F) {
                        this._majorHeal = new IntIntHolder(skill.getId(), skillLevel);
                        this._minorHeal = this._majorHeal;
                        healPower = skill.getPower();
                    } else if (skill.getPower() > healPower) {
                        this._majorHeal = new IntIntHolder(skill.getId(), skillLevel);
                    } else {
                        this._minorHeal = new IntIntHolder(skill.getId(), skillLevel);
                    }
                }
            }
        }

        this.startCastTask();
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        } else {
            this.stopCastTask();
            this.abortCast();
            return true;
        }
    }

    public synchronized void unSummon(Player owner) {
        this.stopCastTask();
        this.abortCast();
        super.unSummon(owner);
    }

    public void doRevive() {
        super.doRevive();
        this.startCastTask();
    }

    private void startCastTask() {
        if (this._majorHeal != null && this._castTask == null && !this.isDead()) {
            this._castTask = ThreadPool.scheduleAtFixedRate(new CastTask(this), 3000L, 1000L);
        }
    }

    private void stopCastTask() {
        if (this._castTask != null) {
            this._castTask.cancel(false);
            this._castTask = null;
        }
    }

    private void castSkill(L2Skill skill) {
        boolean previousFollowStatus = this.getFollowStatus();
        if (previousFollowStatus || this.isInsideRadius(this.getOwner(), skill.getCastRange(), true, true)) {
            this.setTarget(this.getOwner());
            this.useMagic(skill, false, false);
            this.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1).addSkillName(skill));
            if (previousFollowStatus != this.getFollowStatus()) {
                this.setFollowStatus(previousFollowStatus);
            }

        }
    }

    private class CastTask implements Runnable {
        private final BabyPet _baby;

        public CastTask(BabyPet baby) {
            this._baby = baby;
        }

        public void run() {
            Player owner = this._baby.getOwner();
            if (owner != null && !owner.isDead() && !owner.isInvul() && !this._baby.isCastingNow() && !this._baby.isBetrayed() && !this._baby.isMuted() && !this._baby.isOutOfControl() && this._baby.getAI().getDesire().getIntention() != IntentionType.CAST) {
                L2Skill skill = null;
                if (BabyPet.this._majorHeal != null) {
                    double hpPercent = owner.getCurrentHp() / (double) owner.getMaxHp();
                    if (hpPercent < 0.15) {
                        skill = BabyPet.this._majorHeal.getSkill();
                        if (!this._baby.isSkillDisabled(skill) && Rnd.get(100) <= 75 && this._baby.getCurrentMp() >= (double) skill.getMpConsume()) {
                            BabyPet.this.castSkill(skill);
                        }
                    } else if (BabyPet.this._majorHeal.getSkill() != BabyPet.this._minorHeal.getSkill() && hpPercent < 0.8) {
                        skill = BabyPet.this._minorHeal.getSkill();
                        if (!this._baby.isSkillDisabled(skill) && Rnd.get(100) <= 25 && this._baby.getCurrentMp() >= (double) skill.getMpConsume()) {
                            BabyPet.this.castSkill(skill);
                        }
                    }
                }
            }

        }
    }
}
