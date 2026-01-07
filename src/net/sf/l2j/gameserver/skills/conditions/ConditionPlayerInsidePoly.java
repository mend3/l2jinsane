package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.zone.form.ZoneNPoly;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerInsidePoly extends Condition {
    private final ZoneNPoly _zoneNPoly;
    private final boolean _checkInside;

    public ConditionPlayerInsidePoly(ZoneNPoly zoneNPoly, boolean checkInside) {
        _zoneNPoly = zoneNPoly;
        _checkInside = checkInside;
    }

    @Override
    boolean testImpl(Env env) {
        final boolean isInside = _zoneNPoly.isInsideZone(env.getCharacter().getX(), env.getCharacter().getY(), env.getCharacter().getZ());
        return _checkInside ? isInside : !isInside;
    }
}
