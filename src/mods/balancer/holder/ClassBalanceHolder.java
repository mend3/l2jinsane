package mods.balancer.holder;

import net.sf.l2j.gameserver.enums.AttackType;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ClassBalanceHolder {
    private final int _activeClass;
    private final int _targetClass;
    private final Map<AttackType, Double> _normalBalance = new ConcurrentHashMap<>();
    private final Map<AttackType, Double> _olyBalance = new ConcurrentHashMap<>();

    public ClassBalanceHolder(int activeClass, int target) {
        this._activeClass = activeClass;
        this._targetClass = target;
    }

    public void addNormalBalance(AttackType type, double value) {
        this._normalBalance.put(type, value);
    }

    public void addOlyBalance(AttackType type, double value) {
        this._olyBalance.put(type, value);
    }

    public int getTargetClass() {
        return this._targetClass;
    }

    public int getActiveClass() {
        return this._activeClass;
    }

    public Map<AttackType, Double> getNormalBalance() {
        Map<AttackType, Double> map = new TreeMap<>(new AttackTypeComparator(this));
        map.putAll(this._normalBalance);
        return map;
    }

    public void removeOlyBalance(AttackType type) {
        this._olyBalance.remove(type);

    }

    public double getOlyBalanceValue(AttackType type) {
        return this._olyBalance.getOrDefault(type, 1.0D);
    }

    public double getBalanceValue(AttackType type) {
        return this._normalBalance.getOrDefault(type, 1.0D);
    }

    public void remove(AttackType type) {
        this._normalBalance.remove(type);

    }

    public Map<AttackType, Double> getOlyBalance() {
        Map<AttackType, Double> map = new TreeMap<>(new AttackTypeComparator(this));
        map.putAll(this._olyBalance);
        return map;
    }

    private static class AttackTypeComparator implements Comparator<AttackType> {
        public AttackTypeComparator(final ClassBalanceHolder param1) {
        }

        public int compare(AttackType l, AttackType r) {
            int left = l.getId();
            int right = r.getId();
            return Integer.compare(left, right);
        }

    }
}