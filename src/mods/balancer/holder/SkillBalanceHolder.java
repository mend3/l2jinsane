package mods.balancer.holder;

import net.sf.l2j.gameserver.enums.skills.SkillChangeType;

import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class SkillBalanceHolder {
    private final int _skillId;
    private final int _targetId;
    private final Map<SkillChangeType, Double> _list = new ConcurrentHashMap<>();
    private final Map<SkillChangeType, Double> _olyList = new ConcurrentHashMap<>();

    public SkillBalanceHolder(int SkillId, int target) {
        this._skillId = SkillId;
        this._targetId = target;
    }

    public int getSkillId() {
        return this._skillId;
    }

    public String getSkillIcon() {
        return this._skillId < 1000 ? (this._skillId < 100 ? "00" : (this._skillId < 10 ? "000" : "0")) + this._skillId : String.valueOf(this._skillId);
    }

    public int getTarget() {
        return this._targetId;
    }

    public Map<SkillChangeType, Double> getNormalBalance() {
        Map<SkillChangeType, Double> map = new TreeMap<>(new AttackTypeComparator(this));
        map.putAll(this._list);
        return map;
    }

    public Map<SkillChangeType, Double> getOlyBalance() {
        Map<SkillChangeType, Double> map = new TreeMap<>(new AttackTypeComparator(this));
        map.putAll(this._olyList);
        return map;
    }

    public void remove(SkillChangeType sct) {
        this._list.remove(sct);

    }

    public void addSkillBalance(SkillChangeType sct, double value) {
        this._list.put(sct, value);
    }

    public double getValue(SkillChangeType sct) {
        return this._list.containsKey(sct) ? this._list.get(sct) : 1.0D;
    }

    public void removeOly(SkillChangeType sct) {
        this._olyList.remove(sct);

    }

    public void addOlySkillBalance(SkillChangeType sct, double value) {
        this._olyList.put(sct, value);
    }

    public double getOlyBalanceValue(SkillChangeType sct) {
        return this._olyList.containsKey(sct) ? this._olyList.get(sct) : 1.0D;
    }

    private static class AttackTypeComparator implements Comparator<SkillChangeType> {
        public AttackTypeComparator(final SkillBalanceHolder param1) {
        }

        public int compare(SkillChangeType l, SkillChangeType r) {
            int left = l.getId();
            int right = r.getId();
            if (left > right) {
                return 1;
            } else if (left < right) {
                return -1;
            } else {
                Random rnd = new Random();
                return 1;
            }
        }
    }
}