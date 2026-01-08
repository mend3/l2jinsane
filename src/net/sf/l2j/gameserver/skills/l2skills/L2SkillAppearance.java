package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.logging.Level;

public class L2SkillAppearance extends L2Skill {
    private final int _faceId;
    private final int _hairColorId;
    private final int _hairStyleId;

    public L2SkillAppearance(StatSet set) {
        super(set);
        this._faceId = set.getInteger("faceId", -1);
        this._hairColorId = set.getInteger("hairColorId", -1);
        this._hairStyleId = set.getInteger("hairStyleId", -1);
    }

    public void useSkill(Creature caster, WorldObject[] targets) {
        try {
            for (WorldObject target : targets) {
                if (target instanceof Player targetPlayer) {
                    if (this._faceId >= 0) {
                        targetPlayer.getAppearance().setFace(this._faceId);
                    }

                    if (this._hairColorId >= 0) {
                        targetPlayer.getAppearance().setHairColor(this._hairColorId);
                    }

                    if (this._hairStyleId >= 0) {
                        targetPlayer.getAppearance().setHairStyle(this._hairStyleId);
                    }

                    targetPlayer.broadcastUserInfo();
                }
            }
        } catch (Exception e) {
            _log.log(Level.SEVERE, "", e);
        }

    }
}
