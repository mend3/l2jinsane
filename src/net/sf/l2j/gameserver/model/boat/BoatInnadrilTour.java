package net.sf.l2j.gameserver.model.boat;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.manager.BoatManager;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public class BoatInnadrilTour implements Runnable {
    private static final Location OUST_LOC = new Location(107092, 219098, -3952);

    private static final BoatLocation[] TOUR = new BoatLocation[]{
            new BoatLocation(105129, 226240, -3610, 150, 800), new BoatLocation(90604, 238797, -3610, 150, 800), new BoatLocation(74853, 237943, -3610, 150, 800), new BoatLocation(68207, 235399, -3610, 150, 800), new BoatLocation(63226, 230487, -3610, 150, 800), new BoatLocation(61843, 224797, -3610, 150, 800), new BoatLocation(61822, 203066, -3610, 150, 800), new BoatLocation(59051, 197685, -3610, 150, 800), new BoatLocation(54048, 195298, -3610, 150, 800), new BoatLocation(41609, 195687, -3610, 150, 800),
            new BoatLocation(35821, 200284, -3610, 150, 800), new BoatLocation(35567, 205265, -3610, 150, 800), new BoatLocation(35617, 222471, -3610, 150, 800), new BoatLocation(37932, 226588, -3610, 150, 800), new BoatLocation(42932, 229394, -3610, 150, 800), new BoatLocation(74324, 245231, -3610, 150, 800), new BoatLocation(81872, 250314, -3610, 150, 800), new BoatLocation(101692, 249882, -3610, 150, 800), new BoatLocation(107907, 256073, -3610, 150, 800), new BoatLocation(112317, 257133, -3610, 150, 800),
            new BoatLocation(126273, 255313, -3610, 150, 800), new BoatLocation(128067, 250961, -3610, 150, 800), new BoatLocation(128520, 238249, -3610, 150, 800), new BoatLocation(126428, 235072, -3610, 150, 800), new BoatLocation(121843, 234656, -3610, 150, 800), new BoatLocation(120096, 234268, -3610, 150, 800), new BoatLocation(118572, 233046, -3610, 150, 800), new BoatLocation(117671, 228951, -3610, 150, 800), new BoatLocation(115936, 226540, -3610, 150, 800), new BoatLocation(113628, 226240, -3610, 150, 800),
            new BoatLocation(111300, 226240, -3610, 150, 800), new BoatLocation(111264, 226240, -3610, 150, 800)};

    private static final BoatLocation DOCK = TOUR[TOUR.length - 1];

    private final Boat _boat;
    private final CreatureSay ARRIVED_AT_INNADRIL;
    private final CreatureSay LEAVE_INNADRIL5;
    private final CreatureSay LEAVE_INNADRIL1;
    private final CreatureSay LEAVE_INNADRIL0;
    private final CreatureSay LEAVING_INNADRIL;
    private final CreatureSay ARRIVAL20;
    private final CreatureSay ARRIVAL15;
    private final CreatureSay ARRIVAL10;
    private final CreatureSay ARRIVAL5;
    private final CreatureSay ARRIVAL1;
    private final PlaySound INNADRIL_SOUND;
    private final PlaySound INNADRIL_SOUND_LEAVE_5MIN;
    private final PlaySound INNADRIL_SOUND_LEAVE_1MIN;
    private int _cycle = 0;

    public BoatInnadrilTour(Boat boat) {
        this._boat = boat;
        this.ARRIVED_AT_INNADRIL = new CreatureSay(0, 11, 801, SystemMessageId.INNADRIL_BOAT_ANCHOR_10_MINUTES);
        this.LEAVE_INNADRIL5 = new CreatureSay(0, 11, 801, SystemMessageId.INNADRIL_BOAT_LEAVE_IN_5_MINUTES);
        this.LEAVE_INNADRIL1 = new CreatureSay(0, 11, 801, SystemMessageId.INNADRIL_BOAT_LEAVE_IN_1_MINUTE);
        this.LEAVE_INNADRIL0 = new CreatureSay(0, 11, 801, SystemMessageId.INNADRIL_BOAT_LEAVE_SOON);
        this.LEAVING_INNADRIL = new CreatureSay(0, 11, 801, SystemMessageId.INNADRIL_BOAT_LEAVING);
        this.ARRIVAL20 = new CreatureSay(0, 11, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_20_MINUTES);
        this.ARRIVAL15 = new CreatureSay(0, 11, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_15_MINUTES);
        this.ARRIVAL10 = new CreatureSay(0, 11, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_10_MINUTES);
        this.ARRIVAL5 = new CreatureSay(0, 11, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_5_MINUTES);
        this.ARRIVAL1 = new CreatureSay(0, 11, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_1_MINUTE);
        this.INNADRIL_SOUND = new PlaySound(0, "itemsound.ship_arrival_departure", this._boat);
        this.INNADRIL_SOUND_LEAVE_5MIN = new PlaySound(0, "itemsound.ship_5min", this._boat);
        this.INNADRIL_SOUND_LEAVE_1MIN = new PlaySound(0, "itemsound.ship_1min", this._boat);
    }

    public static void load() {
        Boat boat = BoatManager.getInstance().getNewBoat(4, 111264, 226240, -3610, 32768);
        if (boat != null) {
            boat.registerEngine(new BoatInnadrilTour(boat));
            boat.runEngine(180000);
        }
    }

    public void run() {
        switch (this._cycle) {
            case 0:
                BoatManager.getInstance().broadcastPacket(DOCK, DOCK, this.LEAVE_INNADRIL5);
                this._boat.broadcastPacket(this.INNADRIL_SOUND_LEAVE_5MIN);
                ThreadPool.schedule(this, 240000L);
                break;
            case 1:
                BoatManager.getInstance().broadcastPacket(DOCK, DOCK, this.LEAVE_INNADRIL1);
                this._boat.broadcastPacket(this.INNADRIL_SOUND_LEAVE_1MIN);
                ThreadPool.schedule(this, 40000L);
                break;
            case 2:
                BoatManager.getInstance().broadcastPacket(DOCK, DOCK, this.LEAVE_INNADRIL0);
                this._boat.broadcastPacket(this.INNADRIL_SOUND_LEAVE_1MIN);
                ThreadPool.schedule(this, 20000L);
                break;
            case 3:
                BoatManager.getInstance().broadcastPackets(DOCK, DOCK, this.LEAVING_INNADRIL, this.INNADRIL_SOUND);
                this._boat.payForRide(0, 1, OUST_LOC);
                this._boat.executePath(TOUR);
                ThreadPool.schedule(this, 650000L);
                break;
            case 4:
                BoatManager.getInstance().broadcastPacket(DOCK, DOCK, this.ARRIVAL20);
                ThreadPool.schedule(this, 300000L);
                break;
            case 5:
                BoatManager.getInstance().broadcastPacket(DOCK, DOCK, this.ARRIVAL15);
                ThreadPool.schedule(this, 300000L);
                break;
            case 6:
                BoatManager.getInstance().broadcastPacket(DOCK, DOCK, this.ARRIVAL10);
                ThreadPool.schedule(this, 300000L);
                break;
            case 7:
                BoatManager.getInstance().broadcastPacket(DOCK, DOCK, this.ARRIVAL5);
                ThreadPool.schedule(this, 240000L);
                break;
            case 8:
                BoatManager.getInstance().broadcastPacket(DOCK, DOCK, this.ARRIVAL1);
                break;
            case 9:
                BoatManager.getInstance().broadcastPackets(DOCK, DOCK, this.ARRIVED_AT_INNADRIL, this.INNADRIL_SOUND);
                ThreadPool.schedule(this, 300000L);
                break;
        }
        this._cycle++;
        if (this._cycle > 9)
            this._cycle = 0;
    }
}
