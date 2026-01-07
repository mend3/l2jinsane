package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.CreatureTemplate;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

import java.util.HashMap;
import java.util.Map;

public class BoatManager {
    public static final int TALKING_ISLAND = 0;

    public static final int GLUDIN_HARBOR = 1;

    public static final int RUNE_HARBOR = 2;

    public static final int BOAT_BROADCAST_RADIUS = 20000;

    private final Map<Integer, Boat> _boats = new HashMap<>();

    private final boolean[] _docksBusy = new boolean[3];

    public static BoatManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Boat getNewBoat(int boatId, int x, int y, int z, int heading) {
        StatSet set = new StatSet();
        set.set("id", boatId);
        set.set("level", 0);
        set.set("str", 0);
        set.set("con", 0);
        set.set("dex", 0);
        set.set("int", 0);
        set.set("wit", 0);
        set.set("men", 0);
        set.set("hp", 50000);
        set.set("mp", 0);
        set.set("hpRegen", 0.003000000026077032D);
        set.set("mpRegen", 0.003000000026077032D);
        set.set("radius", 0);
        set.set("height", 0);
        set.set("type", "");
        set.set("exp", 0);
        set.set("sp", 0);
        set.set("pAtk", 0);
        set.set("mAtk", 0);
        set.set("pDef", 100);
        set.set("mDef", 100);
        set.set("rHand", 0);
        set.set("lHand", 0);
        set.set("walkSpd", 0);
        set.set("runSpd", 0);
        Boat boat = new Boat(IdFactory.getInstance().getNextId(), new CreatureTemplate(set));
        boat.spawnMe(x, y, z, heading);
        this._boats.put(Integer.valueOf(boat.getObjectId()), boat);
        return boat;
    }

    public Boat getBoat(int boatId) {
        return this._boats.get(Integer.valueOf(boatId));
    }

    public void dockBoat(int dockId, boolean value) {
        this._docksBusy[dockId] = value;
    }

    public boolean isBusyDock(int dockId) {
        return this._docksBusy[dockId];
    }

    public void broadcastPacket(BoatLocation point1, BoatLocation point2, L2GameServerPacket packet) {
        for (Player player : World.getInstance().getPlayers()) {
            double dx = player.getX() - point1.getX();
            double dy = player.getY() - point1.getY();
            if (Math.sqrt(dx * dx + dy * dy) < 20000.0D) {
                player.sendPacket(packet);
                continue;
            }
            dx = player.getX() - point2.getX();
            dy = player.getY() - point2.getY();
            if (Math.sqrt(dx * dx + dy * dy) < 20000.0D)
                player.sendPacket(packet);
        }
    }

    public void broadcastPackets(BoatLocation point1, BoatLocation point2, L2GameServerPacket... packets) {
        for (Player player : World.getInstance().getPlayers()) {
            double dx = player.getX() - point1.getX();
            double dy = player.getY() - point1.getY();
            if (Math.sqrt(dx * dx + dy * dy) < 20000.0D) {
                for (L2GameServerPacket p : packets)
                    player.sendPacket(p);
                continue;
            }
            dx = player.getX() - point2.getX();
            dy = player.getY() - point2.getY();
            if (Math.sqrt(dx * dx + dy * dy) < 20000.0D)
                for (L2GameServerPacket p : packets)
                    player.sendPacket(p);
        }
    }

    private static class SingletonHolder {
        protected static final BoatManager INSTANCE = new BoatManager();
    }
}
