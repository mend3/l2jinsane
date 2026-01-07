package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;

public final class RequestExMagicSkillUseGround extends L2GameClientPacket {
    private int _x;

    private int _y;

    private int _z;

    private int _skillId;

    private boolean _ctrlPressed;

    private boolean _shiftPressed;

    protected void readImpl() {
        this._x = readD();
        this._y = readD();
        this._z = readD();
        this._skillId = readD();
        this._ctrlPressed = (readD() != 0);
        this._shiftPressed = (readC() != 0);
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        L2Skill skill = player.getSkill(this._skillId);
        if (skill == null)
            return;
        player.setCurrentSkillWorldPosition(new Location(this._x, this._y, this._z));
        player.getPosition().setHeading(MathUtil.calculateHeadingFrom(player.getX(), player.getY(), this._x, this._y));
        player.broadcastPacket(new ValidateLocation(player));
        player.useMagic(skill, this._ctrlPressed, this._shiftPressed);
    }
}
