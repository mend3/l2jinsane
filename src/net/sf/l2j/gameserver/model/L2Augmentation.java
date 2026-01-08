package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.AugmentationData;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2j.gameserver.skills.basefuncs.FuncAdd;
import net.sf.l2j.gameserver.skills.basefuncs.LambdaConst;

import java.util.List;

public final class L2Augmentation {
    private int _effectsId;
    private AugmentationStatBoni _boni;
    private L2Skill _skill;

    public L2Augmentation(int effects, L2Skill skill) {
        this._effectsId = 0;
        this._boni = null;
        this._skill = null;
        this._effectsId = effects;
        this._boni = new AugmentationStatBoni(this._effectsId);
        this._skill = skill;
    }

    public L2Augmentation(int effects, int skill, int skillLevel) {
        this(effects, skill != 0 ? SkillTable.getInstance().getInfo(skill, skillLevel) : null);
    }

    public int getAttributes() {
        return this._effectsId;
    }

    public int getAugmentationId() {
        return this._effectsId;
    }

    public L2Skill getSkill() {
        return this._skill;
    }

    public void applyBonus(Player player) {
        boolean updateTimeStamp = false;
        this._boni.applyBonus(player);
        if (this._skill != null) {
            player.addSkill(this._skill, false);
            if (this._skill.isActive() && player.getReuseTimeStamp().containsKey(this._skill.getReuseHashCode())) {
                long delay = player.getReuseTimeStamp().get(this._skill.getReuseHashCode()).getRemaining();
                if (delay > 0L) {
                    player.disableSkill(this._skill, delay);
                    updateTimeStamp = true;
                }
            }

            player.sendSkillList();
            if (updateTimeStamp) {
                player.sendPacket(new SkillCoolTime(player));
            }
        }

    }

    public void removeBonus(Player player) {
        this._boni.removeBonus(player);
        if (this._skill != null) {
            player.removeSkill(this._skill.getId(), false, this._skill.isPassive() || this._skill.isToggle());
            player.sendSkillList();
        }

    }

    public static class AugmentationStatBoni {
        private final Stats[] _stats;
        private final float[] _values;
        private boolean _active = false;

        public AugmentationStatBoni(int augmentationId) {
            List<AugmentationData.AugStat> as = AugmentationData.getInstance().getAugStatsById(augmentationId);
            this._stats = new Stats[as.size()];
            this._values = new float[as.size()];
            int i = 0;

            for (AugmentationData.AugStat aStat : as) {
                this._stats[i] = aStat.getStat();
                this._values[i] = aStat.getValue();
                ++i;
            }

        }

        public void applyBonus(Player player) {
            if (!this._active) {
                for (int i = 0; i < this._stats.length; ++i) {
                    player.addStatFunc(new FuncAdd(this._stats[i], 64, this, new LambdaConst(this._values[i])));
                }

                this._active = true;
            }
        }

        public void removeBonus(Player player) {
            if (this._active) {
                player.removeStatsByOwner(this);
                this._active = false;
            }
        }
    }
}
