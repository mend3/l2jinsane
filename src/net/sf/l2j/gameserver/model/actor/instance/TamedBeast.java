/**/
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate.SkillType;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

public final class TamedBeast extends FeedableBeast {
    private static final String[] FOOD_CHAT = new String[]{"Refills! Yeah!", "I am such a gluttonous beast, it is embarrassing! Ha ha.", "Your cooperative feeling has been getting better and better.", "I will help you!", "The weather is really good. Wanna go for a picnic?", "I really like you! This is tasty...", "If you do not have to leave this place, then I can help you.", "What can I help you with?", "I am not here only for food!", "Yam, yam, yam, yam, yam!"};
    private static final int MAX_DISTANCE_FROM_HOME = 13000;
    private static final int TASK_INTERVAL = 5000;
    private int _foodId;
    private Player _owner;
    private Future<?> _aiTask = null;

    public TamedBeast(int objectId, NpcTemplate template, Player owner, int foodId, Location loc) {
        super(objectId, template);
        this.disableCoreAI(true);
        this.setCurrentHp(this.getMaxHp());
        this.setCurrentMp(this.getMaxMp());
        this.setTitle(owner.getName());
        this._owner = owner;
        owner.setTrainedBeast(this);
        this._foodId = foodId;
        this._aiTask = ThreadPool.scheduleAtFixedRate(new TamedBeast.AiTask(), 5000L, 5000L);
        this.spawnMe(loc);
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        } else {
            if (this._aiTask != null) {
                this._aiTask.cancel(true);
                this._aiTask = null;
            }

            if (this._owner != null) {
                this._owner.setTrainedBeast(null);
            }

            return true;
        }
    }

    public void deleteMe() {
        if (this._aiTask != null) {
            this._aiTask.cancel(true);
            this._aiTask = null;
        }

        this.stopHpMpRegeneration();
        this.getAI().stopFollow();
        if (this._owner != null) {
            this._owner.setTrainedBeast(null);
        }

        super.deleteMe();
    }

    public void onOwnerGotAttacked(Creature attacker) {
        if (this._owner != null && this._owner.isOnline()) {
            if (!this._owner.isDead() && !this.isCastingNow()) {
                int proba = Rnd.get(3);
                float MPRatio;
                Iterator var4;
                L2Skill skill;
                if (proba == 0) {
                    MPRatio = (float) this._owner.getCurrentHp() / (float) this._owner.getMaxHp();
                    if ((double) MPRatio < 0.5D) {
                        var4 = this.getTemplate().getSkills(SkillType.HEAL).iterator();

                        while (var4.hasNext()) {
                            skill = (L2Skill) var4.next();
                            switch (skill.getSkillType()) {
                                case HEAL:
                                case HOT:
                                case BALANCE_LIFE:
                                case HEAL_PERCENT:
                                case HEAL_STATIC:
                                    this.sitCastAndFollow(skill, this._owner);
                                    return;
                            }
                        }
                    }
                } else if (proba == 1) {
                    Iterator var6 = this.getTemplate().getSkills(SkillType.DEBUFF).iterator();

                    while (var6.hasNext()) {
                        skill = (L2Skill) var6.next();
                        if (attacker.getFirstEffect(skill) == null) {
                            this.sitCastAndFollow(skill, attacker);
                            return;
                        }
                    }
                } else if (proba == 2) {
                    MPRatio = (float) this._owner.getCurrentMp() / (float) this._owner.getMaxMp();
                    if ((double) MPRatio < 0.5D) {
                        var4 = this.getTemplate().getSkills(SkillType.HEAL).iterator();

                        while (var4.hasNext()) {
                            skill = (L2Skill) var4.next();
                            switch (skill.getSkillType()) {
                                case MANARECHARGE:
                                case MANAHEAL_PERCENT:
                                    this.sitCastAndFollow(skill, this._owner);
                                    return;
                            }
                        }
                    }
                }

            }
        } else {
            this.deleteMe();
        }
    }

    private void sitCastAndFollow(L2Skill skill, Creature target) {
        this.stopMove(null);
        this.getAI().setIntention(IntentionType.IDLE);
        this.setTarget(target);
        this.doCast(skill);
        this.getAI().setIntention(IntentionType.FOLLOW, this._owner);
    }

    private class AiTask implements Runnable {
        private int _step;

        public AiTask() {
        }

        public void run() {
            Player owner = TamedBeast.this._owner;
            if (owner != null && owner.isOnline()) {
                if (++this._step > 12) {
                    if (!TamedBeast.this.isInsideRadius(52335, -83086, 13000, true)) {
                        TamedBeast.this.deleteMe();
                        return;
                    }

                    if (!owner.destroyItemByItemId("BeastMob", TamedBeast.this._foodId, 1, TamedBeast.this, true)) {
                        TamedBeast.this.deleteMe();
                        return;
                    }

                    TamedBeast.this.broadcastPacket(new SocialAction(TamedBeast.this, 2));
                    TamedBeast.this.broadcastPacket(new NpcSay(TamedBeast.this.getObjectId(), 0, TamedBeast.this.getNpcId(), Rnd.get(TamedBeast.FOOD_CHAT)));
                    this._step = 0;
                }

                if (!owner.isDead() && !TamedBeast.this.isCastingNow()) {
                    int totalBuffsOnOwner = 0;
                    int i = 0;
                    L2Skill buffToGive = null;
                    List<L2Skill> skills = TamedBeast.this.getTemplate().getSkills(SkillType.BUFF);
                    int rand = Rnd.get(skills.size());
                    Iterator var7 = skills.iterator();

                    while (var7.hasNext()) {
                        L2Skill skill = (L2Skill) var7.next();
                        if (i == rand) {
                            buffToGive = skill;
                        }

                        ++i;
                        if (owner.getFirstEffect(skill) != null) {
                            ++totalBuffsOnOwner;
                        }
                    }

                    if (totalBuffsOnOwner < 2 && owner.getFirstEffect(buffToGive) == null) {
                        TamedBeast.this.sitCastAndFollow(buffToGive, owner);
                    } else {
                        TamedBeast.this.getAI().setIntention(IntentionType.FOLLOW, owner);
                    }

                }
            } else {
                TamedBeast.this.deleteMe();
            }
        }
    }
}