package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.enums.actors.ClassType;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;

public final class VillageMasterMystic extends VillageMaster {
    public VillageMasterMystic(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    protected boolean checkVillageMasterRace(ClassId pclass) {
        if (pclass == null) {
            return false;
        } else {
            return pclass.getRace() == ClassRace.HUMAN || pclass.getRace() == ClassRace.ELF;
        }
    }

    protected boolean checkVillageMasterTeachType(ClassId pclass) {
        if (pclass == null) {
            return false;
        } else {
            return pclass.getType() == ClassType.MYSTIC;
        }
    }
}
