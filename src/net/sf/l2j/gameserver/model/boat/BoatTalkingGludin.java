package net.sf.l2j.gameserver.model.boat;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.manager.BoatManager;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public class BoatTalkingGludin implements Runnable {
    private static final Location OUST_LOC_1 = new Location(-96777, 258970, -3623);

    private static final Location OUST_LOC_2 = new Location(-90015, 150422, -3610);

    private static final BoatLocation[] TALKING_TO_GLUDIN = new BoatLocation[]{new BoatLocation(-121385, 261660, -3610, 180, 800), new BoatLocation(-127694, 253312, -3610, 200, 800), new BoatLocation(-129274, 237060, -3610, 250, 800), new BoatLocation(-114688, 139040, -3610, 200, 800), new BoatLocation(-109663, 135704, -3610, 180, 800), new BoatLocation(-102151, 135704, -3610, 180, 800), new BoatLocation(-96686, 140595, -3610, 180, 800), new BoatLocation(-95686, 147718, -3610, 180, 800), new BoatLocation(-95686, 148718, -3610, 180, 800), new BoatLocation(-95686, 149718, -3610, 150, 800)};

    private static final BoatLocation[] GLUDIN_DOCK = new BoatLocation[]{new BoatLocation(-95686, 150514, -3610, 150, 800)};

    private static final BoatLocation[] GLUDIN_TO_TALKING = new BoatLocation[]{new BoatLocation(-95686, 155514, -3610, 180, 800), new BoatLocation(-95686, 185514, -3610, 250, 800), new BoatLocation(-60136, 238816, -3610, 200, 800), new BoatLocation(-60520, 259609, -3610, 180, 1800), new BoatLocation(-65344, 261460, -3610, 180, 1800), new BoatLocation(-83344, 261560, -3610, 180, 1800), new BoatLocation(-88344, 261660, -3610, 180, 1800), new BoatLocation(-92344, 261660, -3610, 150, 1800), new BoatLocation(-94242, 261659, -3610, 150, 1800)};

    private static final BoatLocation[] TALKING_DOCK = new BoatLocation[]{new BoatLocation(-96622, 261660, -3610, 150, 1800)};

    private final Boat _boat;
    private final CreatureSay ARRIVED_AT_TALKING;
    private final CreatureSay ARRIVED_AT_TALKING_2;
    private final CreatureSay LEAVE_TALKING5;
    private final CreatureSay LEAVE_TALKING1;
    private final CreatureSay LEAVE_TALKING1_2;
    private final CreatureSay LEAVE_TALKING0;
    private final CreatureSay LEAVING_TALKING;
    private final CreatureSay ARRIVED_AT_GLUDIN;
    private final CreatureSay ARRIVED_AT_GLUDIN_2;
    private final CreatureSay LEAVE_GLUDIN5;
    private final CreatureSay LEAVE_GLUDIN1;
    private final CreatureSay LEAVE_GLUDIN0;
    private final CreatureSay LEAVING_GLUDIN;
    private final CreatureSay BUSY_TALKING;
    private final CreatureSay BUSY_GLUDIN;
    private final CreatureSay ARRIVAL_GLUDIN10;
    private final CreatureSay ARRIVAL_GLUDIN5;
    private final CreatureSay ARRIVAL_GLUDIN1;
    private final CreatureSay ARRIVAL_TALKING10;
    private final CreatureSay ARRIVAL_TALKING5;
    private final CreatureSay ARRIVAL_TALKING1;
    private final PlaySound TALKING_SOUND;
    private final PlaySound GLUDIN_SOUND;
    private final PlaySound TALKING_SOUND_LEAVE_5MIN;
    private final PlaySound TALKING_SOUND_LEAVE_1MIN;
    private final PlaySound GLUDIN_SOUND_LEAVE_5MIN;
    private final PlaySound GLUDIN_SOUND_LEAVE_1MIN;
    private int _cycle = 0;
    private int _shoutCount = 0;

    public BoatTalkingGludin(Boat boat) {
        this._boat = boat;
        this._cycle = 0;
        this.ARRIVED_AT_TALKING = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_ARRIVED_AT_TALKING);
        this.ARRIVED_AT_TALKING_2 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES);
        this.LEAVE_TALKING5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_IN_5_MINUTES);
        this.LEAVE_TALKING1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_IN_1_MINUTE);
        this.LEAVE_TALKING1_2 = new CreatureSay(0, 11, 801, SystemMessageId.MAKE_HASTE_GET_ON_BOAT);
        this.LEAVE_TALKING0 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_GLUDIN);
        this.LEAVING_TALKING = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVING_FOR_GLUDIN);
        this.ARRIVED_AT_GLUDIN = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_ARRIVED_AT_GLUDIN);
        this.ARRIVED_AT_GLUDIN_2 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES);
        this.LEAVE_GLUDIN5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES);
        this.LEAVE_GLUDIN1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE);
        this.LEAVE_GLUDIN0 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_TALKING);
        this.LEAVING_GLUDIN = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_LEAVING_FOR_TALKING);
        this.BUSY_TALKING = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_GLUDIN_TALKING_DELAYED);
        this.BUSY_GLUDIN = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_TALKING_GLUDIN_DELAYED);
        this.ARRIVAL_GLUDIN10 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_10_MINUTES);
        this.ARRIVAL_GLUDIN5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_5_MINUTES);
        this.ARRIVAL_GLUDIN1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_1_MINUTE);
        this.ARRIVAL_TALKING10 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_10_MINUTES);
        this.ARRIVAL_TALKING5 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_5_MINUTES);
        this.ARRIVAL_TALKING1 = new CreatureSay(0, 11, 801, SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_1_MINUTE);
        this.TALKING_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", this._boat);
        this.GLUDIN_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", this._boat);
        this.TALKING_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", this._boat);
        this.TALKING_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", this._boat);
        this.GLUDIN_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", this._boat);
        this.GLUDIN_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", this._boat);
    }

    public static void load() {
        Boat boat = BoatManager.getInstance().getNewBoat(1, -96622, 261660, -3610, 32768);
        if (boat != null) {
            boat.registerEngine(new BoatTalkingGludin(boat));
            boat.runEngine(180000);
            BoatManager.getInstance().dockBoat(0, true);
        }
    }

    public void run() {
        switch (this._cycle) {
            case 0:
                BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_TALKING5);
                this._boat.broadcastPacket(this.TALKING_SOUND_LEAVE_5MIN);
                ThreadPool.schedule(this, 240000L);
                break;
            case 1:
                BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_TALKING1, this.LEAVE_TALKING1_2);
                this._boat.broadcastPacket(this.TALKING_SOUND_LEAVE_1MIN);
                ThreadPool.schedule(this, 40000L);
                break;
            case 2:
                BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_TALKING0);
                this._boat.broadcastPacket(this.TALKING_SOUND_LEAVE_1MIN);
                ThreadPool.schedule(this, 20000L);
                break;
            case 3:
                BoatManager.getInstance().dockBoat(0, false);
                BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVING_TALKING);
                this._boat.broadcastPacket(this.TALKING_SOUND);
                this._boat.payForRide(1074, 1, OUST_LOC_1);
                this._boat.executePath(TALKING_TO_GLUDIN);
                ThreadPool.schedule(this, 300000L);
                break;
            case 4:
                BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], this.ARRIVAL_GLUDIN10);
                ThreadPool.schedule(this, 300000L);
                break;
            case 5:
                BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], this.ARRIVAL_GLUDIN5);
                ThreadPool.schedule(this, 240000L);
                break;
            case 6:
                BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], this.ARRIVAL_GLUDIN1);
                break;
            case 7:
                if (BoatManager.getInstance().isBusyDock(1)) {
                    if (this._shoutCount == 0)
                        BoatManager.getInstance().broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], this.BUSY_GLUDIN);
                    this._shoutCount++;
                    if (this._shoutCount > 35)
                        this._shoutCount = 0;
                    ThreadPool.schedule(this, 5000L);
                    return;
                }
                BoatManager.getInstance().dockBoat(1, true);
                this._boat.executePath(GLUDIN_DOCK);
                break;
            case 8:
                BoatManager.getInstance().broadcastPackets(GLUDIN_DOCK[0], TALKING_DOCK[0], this.ARRIVED_AT_GLUDIN, this.ARRIVED_AT_GLUDIN_2);
                this._boat.broadcastPacket(this.GLUDIN_SOUND);
                ThreadPool.schedule(this, 300000L);
                break;
            case 9:
                BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_GLUDIN5);
                this._boat.broadcastPacket(this.GLUDIN_SOUND_LEAVE_5MIN);
                ThreadPool.schedule(this, 240000L);
                break;
            case 10:
                BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_GLUDIN1, this.LEAVE_TALKING1_2);
                this._boat.broadcastPacket(this.GLUDIN_SOUND_LEAVE_1MIN);
                ThreadPool.schedule(this, 40000L);
                break;
            case 11:
                BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVE_GLUDIN0);
                this._boat.broadcastPacket(this.GLUDIN_SOUND_LEAVE_1MIN);
                ThreadPool.schedule(this, 20000L);
                break;
            case 12:
                BoatManager.getInstance().dockBoat(1, false);
                BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], this.LEAVING_GLUDIN);
                this._boat.broadcastPacket(this.GLUDIN_SOUND);
                this._boat.payForRide(1075, 1, OUST_LOC_2);
                this._boat.executePath(GLUDIN_TO_TALKING);
                ThreadPool.schedule(this, 150000L);
                break;
            case 13:
                BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.ARRIVAL_TALKING10);
                ThreadPool.schedule(this, 300000L);
                break;
            case 14:
                BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.ARRIVAL_TALKING5);
                ThreadPool.schedule(this, 240000L);
                break;
            case 15:
                BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.ARRIVAL_TALKING1);
                break;
            case 16:
                if (BoatManager.getInstance().isBusyDock(0)) {
                    if (this._shoutCount == 0)
                        BoatManager.getInstance().broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], this.BUSY_TALKING);
                    this._shoutCount++;
                    if (this._shoutCount > 35)
                        this._shoutCount = 0;
                    ThreadPool.schedule(this, 5000L);
                    return;
                }
                BoatManager.getInstance().dockBoat(0, true);
                this._boat.executePath(TALKING_DOCK);
                break;
            case 17:
                BoatManager.getInstance().broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], this.ARRIVED_AT_TALKING, this.ARRIVED_AT_TALKING_2);
                this._boat.broadcastPacket(this.TALKING_SOUND);
                ThreadPool.schedule(this, 300000L);
                break;
        }
        this._shoutCount = 0;
        this._cycle++;
        if (this._cycle > 17)
            this._cycle = 0;
    }
}
