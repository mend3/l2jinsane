package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ShowTownMap;
import net.sf.l2j.gameserver.network.serverpackets.StaticObjectInfo;

public class StaticObject extends WorldObject {
    private int _staticObjectId;
    private int _type = -1;
    private boolean _isBusy;
    private ShowTownMap _map;

    public StaticObject(int objectId) {
        super(objectId);
    }

    public int getStaticObjectId() {
        return this._staticObjectId;
    }

    public void setStaticObjectId(int StaticObjectId) {
        this._staticObjectId = StaticObjectId;
    }

    public int getType() {
        return this._type;
    }

    public void setType(int type) {
        this._type = type;
    }

    public boolean isBusy() {
        return this._isBusy;
    }

    public void setBusy(boolean busy) {
        this._isBusy = busy;
    }

    public void setMap(String texture, int x, int y) {
        this._map = new ShowTownMap("town_map." + texture, x, y);
    }

    public ShowTownMap getMap() {
        return this._map;
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (!player.isInsideRadius(this, 150, false, false)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            if (this.getType() == 2) {
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                html.setFile("data/html/signboard.htm");
                player.sendPacket(html);
            } else if (this.getType() == 0) {
                player.sendPacket(this.getMap());
            }

            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public void onActionShift(Player player) {
        if (player.isGM()) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile("data/html/admin/staticinfo.htm");
            html.replace("%x%", this.getX());
            html.replace("%y%", this.getY());
            html.replace("%z%", this.getZ());
            html.replace("%objid%", this.getObjectId());
            html.replace("%staticid%", this.getStaticObjectId());
            html.replace("%class%", this.getClass().getSimpleName());
            player.sendPacket(html);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

        if (player.getTarget() != this) {
            player.setTarget(this);
        } else {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public boolean isAutoAttackable(Creature attacker) {
        return false;
    }

    public void sendInfo(Player activeChar) {
        activeChar.sendPacket(new StaticObjectInfo(this));
    }
}
