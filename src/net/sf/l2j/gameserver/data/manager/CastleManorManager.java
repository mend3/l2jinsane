package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.ManorStatus;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2j.gameserver.model.manor.CropProcure;
import net.sf.l2j.gameserver.model.manor.Seed;
import net.sf.l2j.gameserver.model.manor.SeedProduction;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.network.SystemMessageId;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CastleManorManager implements IXmlReader {
    private static final String LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?";

    private static final String LOAD_PRODUCTION = "SELECT * FROM castle_manor_production WHERE castle_id=?";

    private static final String UPDATE_PRODUCTION = "UPDATE castle_manor_production SET amount = ? WHERE castle_id = ? AND seed_id = ? AND next_period = 0";

    private static final String UPDATE_PROCURE = "UPDATE castle_manor_procure SET amount = ? WHERE castle_id = ? AND crop_id = ? AND next_period = 0";

    private static final String DELETE_ALL_PRODUCTS = "DELETE FROM castle_manor_production";

    private static final String INSERT_PRODUCT = "INSERT INTO castle_manor_production VALUES (?, ?, ?, ?, ?, ?)";

    private static final String DELETE_ALL_PROCURE = "DELETE FROM castle_manor_procure";

    private static final String INSERT_CROP = "INSERT INTO castle_manor_procure VALUES (?, ?, ?, ?, ?, ?, ?)";
    private final Map<Integer, Seed> _seeds = new HashMap<>();
    private final Map<Integer, List<CropProcure>> _procure = new HashMap<>();
    private final Map<Integer, List<CropProcure>> _procureNext = new HashMap<>();
    private final Map<Integer, List<SeedProduction>> _production = new HashMap<>();
    private final Map<Integer, List<SeedProduction>> _productionNext = new HashMap<>();
    private ManorStatus _mode = ManorStatus.APPROVED;
    private Calendar _nextModeChange = null;

    protected CastleManorManager() {
    }

    public static void updateCurrentProduction(int castleId, Collection<SeedProduction> items) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE castle_manor_production SET amount = ? WHERE castle_id = ? AND seed_id = ? AND next_period = 0");
                try {
                    for (SeedProduction sp : items) {
                        ps.setLong(1, sp.getAmount());
                        ps.setInt(2, castleId);
                        ps.setInt(3, sp.getId());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to store manor data.", e);
        }
    }

    public static void updateCurrentProcure(int castleId, Collection<CropProcure> items) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE castle_manor_procure SET amount = ? WHERE castle_id = ? AND crop_id = ? AND next_period = 0");
                try {
                    for (CropProcure sp : items) {
                        ps.setLong(1, sp.getAmount());
                        ps.setInt(2, castleId);
                        ps.setInt(3, sp.getId());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to store manor data.", e);
        }
    }

    public static CastleManorManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        if (!Config.ALLOW_MANOR) {
            this._mode = ManorStatus.DISABLED;
            LOGGER.info("Manor system is deactivated.");
            return;
        }

        parseFile("./data/xml/manors.xml");
        LOGGER.info("Loaded {} seeds.", this._seeds.size());


        // Load dynamic data.
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement stProduction = con.prepareStatement(LOAD_PRODUCTION);
             PreparedStatement stProcure = con.prepareStatement(LOAD_PROCURE)) {
            for (Castle castle : CastleManager.getInstance().getCastles()) {
                // Seed production
                final List<SeedProduction> pCurrent = new ArrayList<>();
                final List<SeedProduction> pNext = new ArrayList<>();

                stProduction.clearParameters();
                stProduction.setInt(1, castle.getId());

                try (ResultSet rs = stProduction.executeQuery()) {
                    while (rs.next()) {
                        final SeedProduction sp = new SeedProduction(rs.getInt("seed_id"), rs.getInt("amount"), rs.getInt("price"), rs.getInt("start_amount"));
                        if (rs.getBoolean("next_period"))
                            pNext.add(sp);
                        else
                            pCurrent.add(sp);
                    }
                }
                _production.put(castle.getId(), pCurrent);
                _productionNext.put(castle.getId(), pNext);

                // Seed procure
                final List<CropProcure> current = new ArrayList<>();
                final List<CropProcure> next = new ArrayList<>();

                stProcure.clearParameters();
                stProcure.setInt(1, castle.getId());

                try (ResultSet rs = stProcure.executeQuery()) {
                    while (rs.next()) {
                        final CropProcure cp = new CropProcure(rs.getInt("crop_id"), rs.getInt("amount"), rs.getInt("reward_type"), rs.getInt("start_amount"), rs.getInt("price"));
                        if (rs.getBoolean("next_period"))
                            next.add(cp);
                        else
                            current.add(cp);
                    }
                }
                _procure.put(castle.getId(), current);
                _procureNext.put(castle.getId(), next);
            }
        } catch (Exception e) {
            LOGGER.error("Error restoring manor data.", e);
        }

        // Set mode and start timer
        final Calendar currentTime = Calendar.getInstance();
        final int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        final int min = currentTime.get(Calendar.MINUTE);
        final int maintenanceMin = Config.ALT_MANOR_REFRESH_MIN + Config.ALT_MANOR_MAINTENANCE_MIN;

        if ((hour >= Config.ALT_MANOR_REFRESH_TIME && min >= maintenanceMin) || hour < Config.ALT_MANOR_APPROVE_TIME || (hour == Config.ALT_MANOR_APPROVE_TIME && min <= Config.ALT_MANOR_APPROVE_MIN))
            _mode = ManorStatus.MODIFIABLE;
        else if (hour == Config.ALT_MANOR_REFRESH_TIME && (min >= Config.ALT_MANOR_REFRESH_MIN && min < maintenanceMin))
            _mode = ManorStatus.MAINTENANCE;

        // Schedule mode change
        scheduleModeChange();
        scheduleModeChange();
        ThreadPool.scheduleAtFixedRate(this::storeMe, Config.ALT_MANOR_SAVE_PERIOD_RATE, Config.ALT_MANOR_SAVE_PERIOD_RATE);
        LOGGER.debug("Current Manor mode is: {}.", this._mode.toString());
    }

    public void parseDocument(Document doc, Path path) {
        AtomicInteger cropId = new AtomicInteger();
        forEach(doc, "list", listNode -> forEach(listNode, "manor", manorNode ->
        {
            final int castleId = parseInteger(manorNode.getAttributes(), "id");
            forEach(manorNode, "crop", cropNode ->
            {
                final StatSet set = parseAttributes(cropNode);
                set.set("castleId", castleId);
                set.set("cropId", cropId.incrementAndGet());

                _seeds.put(set.getInteger("seedId"), new Seed(set));
            });
        }));
    }

    private final void scheduleModeChange() {
        this._nextModeChange = Calendar.getInstance();
        this._nextModeChange.set(Calendar.SECOND, 0);
        switch (this._mode) {
            case MODIFIABLE:
                this._nextModeChange.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_APPROVE_TIME);
                this._nextModeChange.set(Calendar.MINUTE, Config.ALT_MANOR_APPROVE_MIN);
                if (this._nextModeChange.before(Calendar.getInstance()))
                    this._nextModeChange.add(Calendar.DATE, 1);
                break;
            case MAINTENANCE:
                this._nextModeChange.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_REFRESH_TIME);
                this._nextModeChange.set(Calendar.MINUTE, Config.ALT_MANOR_REFRESH_MIN + Config.ALT_MANOR_MAINTENANCE_MIN);
                break;
            case APPROVED:
                this._nextModeChange.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_REFRESH_TIME);
                this._nextModeChange.set(Calendar.MINUTE, Config.ALT_MANOR_REFRESH_MIN);
                break;
        }
        ThreadPool.schedule(this::changeMode, this._nextModeChange.getTimeInMillis() - System.currentTimeMillis());
    }

    public final void changeMode() {
        switch (this._mode) {
            case APPROVED:
                this._mode = ManorStatus.MAINTENANCE;
                for (Castle castle : CastleManager.getInstance().getCastles()) {
                    Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
                    if (owner == null)
                        continue;
                    int castleId = castle.getCastleId();
                    ItemContainer cwh = owner.getWarehouse();
                    for (CropProcure crop : this._procure.get(castleId)) {
                        if (crop.getStartAmount() > 0) {
                            if (crop.getStartAmount() != crop.getAmount()) {
                                int count = (int) ((crop.getStartAmount() - crop.getAmount()) * 0.9D);
                                if (count < 1 && Rnd.nextInt(99) < 90)
                                    count = 1;
                                if (count > 0)
                                    cwh.addItem("Manor", getSeedByCrop(crop.getId()).getMatureId(), count, null, null);
                            }
                            if (crop.getAmount() > 0)
                                castle.addToTreasuryNoTax(((long) crop.getAmount() * crop.getPrice()));
                        }
                    }
                    List<SeedProduction> _nextProduction = this._productionNext.get(castleId);
                    List<CropProcure> _nextProcure = this._procureNext.get(castleId);
                    this._production.put(castleId, _nextProduction);
                    this._procure.put(castleId, _nextProcure);
                    if (castle.getTreasury() < getManorCost(castleId, false)) {
                        this._productionNext.put(castleId, Collections.emptyList());
                        this._procureNext.put(castleId, Collections.emptyList());
                        continue;
                    }
                    List<SeedProduction> production = new ArrayList<>(_nextProduction);
                    for (SeedProduction s : production)
                        s.setAmount(s.getStartAmount());
                    this._productionNext.put(castleId, production);
                    List<CropProcure> procure = new ArrayList<>(_nextProcure);
                    for (CropProcure cr : procure)
                        cr.setAmount(cr.getStartAmount());
                    this._procureNext.put(castleId, procure);
                }
                storeMe();
                break;
            case MAINTENANCE:
                for (Castle castle : CastleManager.getInstance().getCastles()) {
                    Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
                    if (owner != null) {
                        ClanMember clanLeader = owner.getLeader();
                        if (clanLeader != null && clanLeader.isOnline())
                            clanLeader.getPlayerInstance().sendPacket(SystemMessageId.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED);
                    }
                }
                this._mode = ManorStatus.MODIFIABLE;
                break;
            case MODIFIABLE:
                this._mode = ManorStatus.APPROVED;
                for (Castle castle : CastleManager.getInstance().getCastles()) {
                    Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
                    if (owner == null)
                        continue;
                    int slots = 0;
                    int castleId = castle.getCastleId();
                    ItemContainer cwh = owner.getWarehouse();
                    for (CropProcure crop : this._procureNext.get(castleId)) {
                        if (crop.getStartAmount() > 0 && cwh.getItemsByItemId(getSeedByCrop(crop.getId()).getMatureId()) == null)
                            slots++;
                    }
                    long manorCost = getManorCost(castleId, true);
                    if (!cwh.validateCapacity(slots) && castle.getTreasury() < manorCost) {
                        this._productionNext.get(castleId).clear();
                        this._procureNext.get(castleId).clear();
                        ClanMember clanLeader = owner.getLeader();
                        if (clanLeader != null && clanLeader.isOnline())
                            clanLeader.getPlayerInstance().sendPacket(SystemMessageId.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION);
                        continue;
                    }
                    castle.addToTreasuryNoTax(-manorCost);
                }
                break;
        }
        scheduleModeChange();
        LOGGER.debug("Manor mode changed to: {}.", this._mode.toString());
    }

    public final void setNextSeedProduction(List<SeedProduction> list, int castleId) {
        this._productionNext.put(castleId, list);
    }

    public final void setNextCropProcure(List<CropProcure> list, int castleId) {
        this._procureNext.put(castleId, list);
    }

    public final List<SeedProduction> getSeedProduction(int castleId, boolean nextPeriod) {
        return nextPeriod ? this._productionNext.get(castleId) : this._production.get(castleId);
    }

    public final SeedProduction getSeedProduct(int castleId, int seedId, boolean nextPeriod) {
        for (SeedProduction sp : getSeedProduction(castleId, nextPeriod)) {
            if (sp.getId() == seedId)
                return sp;
        }
        return null;
    }

    public final List<CropProcure> getCropProcure(int castleId, boolean nextPeriod) {
        return nextPeriod ? this._procureNext.get(castleId) : this._procure.get(castleId);
    }

    public final CropProcure getCropProcure(int castleId, int cropId, boolean nextPeriod) {
        for (CropProcure cp : getCropProcure(castleId, nextPeriod)) {
            if (cp.getId() == cropId)
                return cp;
        }
        return null;
    }

    public final long getManorCost(int castleId, boolean nextPeriod) {
        List<CropProcure> procure = getCropProcure(castleId, nextPeriod);
        List<SeedProduction> production = getSeedProduction(castleId, nextPeriod);
        long total = 0L;
        for (SeedProduction seed : production) {
            Seed s = getSeed(seed.getId());
            total += (s == null) ? 1L : ((long) s.getSeedReferencePrice() * seed.getStartAmount());
        }
        for (CropProcure crop : procure)
            total += ((long) crop.getPrice() * crop.getStartAmount());
        return total;
    }

    public final boolean storeMe() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ds = con.prepareStatement("DELETE FROM castle_manor_production");
                try {
                    PreparedStatement is = con.prepareStatement("INSERT INTO castle_manor_production VALUES (?, ?, ?, ?, ?, ?)");
                    try {
                        PreparedStatement dp = con.prepareStatement("DELETE FROM castle_manor_procure");
                        try {
                            PreparedStatement ip = con.prepareStatement("INSERT INTO castle_manor_procure VALUES (?, ?, ?, ?, ?, ?, ?)");
                            try {
                                ds.executeUpdate();
                                for (Map.Entry<Integer, List<SeedProduction>> entry : this._production.entrySet()) {
                                    for (SeedProduction sp : entry.getValue()) {
                                        is.setInt(1, (Integer) entry.getKey());
                                        is.setInt(2, sp.getId());
                                        is.setLong(3, sp.getAmount());
                                        is.setLong(4, sp.getStartAmount());
                                        is.setLong(5, sp.getPrice());
                                        is.setBoolean(6, false);
                                        is.addBatch();
                                    }
                                }
                                for (Map.Entry<Integer, List<SeedProduction>> entry : this._productionNext.entrySet()) {
                                    for (SeedProduction sp : entry.getValue()) {
                                        is.setInt(1, (Integer) entry.getKey());
                                        is.setInt(2, sp.getId());
                                        is.setLong(3, sp.getAmount());
                                        is.setLong(4, sp.getStartAmount());
                                        is.setLong(5, sp.getPrice());
                                        is.setBoolean(6, true);
                                        is.addBatch();
                                    }
                                }
                                is.executeBatch();
                                dp.executeUpdate();
                                for (Map.Entry<Integer, List<CropProcure>> entry : this._procure.entrySet()) {
                                    for (CropProcure cp : entry.getValue()) {
                                        ip.setInt(1, (Integer) entry.getKey());
                                        ip.setInt(2, cp.getId());
                                        ip.setLong(3, cp.getAmount());
                                        ip.setLong(4, cp.getStartAmount());
                                        ip.setLong(5, cp.getPrice());
                                        ip.setInt(6, cp.getReward());
                                        ip.setBoolean(7, false);
                                        ip.addBatch();
                                    }
                                }
                                for (Map.Entry<Integer, List<CropProcure>> entry : this._procureNext.entrySet()) {
                                    for (CropProcure cp : entry.getValue()) {
                                        ip.setInt(1, (Integer) entry.getKey());
                                        ip.setInt(2, cp.getId());
                                        ip.setLong(3, cp.getAmount());
                                        ip.setLong(4, cp.getStartAmount());
                                        ip.setLong(5, cp.getPrice());
                                        ip.setInt(6, cp.getReward());
                                        ip.setBoolean(7, true);
                                        ip.addBatch();
                                    }
                                }
                                ip.executeBatch();
                                boolean bool = true;
                                if (ip != null)
                                    ip.close();
                                if (dp != null)
                                    dp.close();
                                if (is != null)
                                    is.close();
                                if (ds != null)
                                    ds.close();
                                if (con != null)
                                    con.close();
                                return bool;
                            } catch (Throwable throwable) {
                                if (ip != null)
                                    try {
                                        ip.close();
                                    } catch (Throwable throwable1) {
                                        throwable.addSuppressed(throwable1);
                                    }
                                throw throwable;
                            }
                        } catch (Throwable throwable) {
                            if (dp != null)
                                try {
                                    dp.close();
                                } catch (Throwable throwable1) {
                                    throwable.addSuppressed(throwable1);
                                }
                            throw throwable;
                        }
                    } catch (Throwable throwable) {
                        if (is != null)
                            try {
                                is.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                } catch (Throwable throwable) {
                    if (ds != null)
                        try {
                            ds.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to store manor data.", e);
            return false;
        }
    }

    public final void resetManorData(int castleId) {
        if (this._mode == ManorStatus.DISABLED)
            return;
        this._procure.get(castleId).clear();
        this._procureNext.get(castleId).clear();
        this._production.get(castleId).clear();
        this._productionNext.get(castleId).clear();
    }

    public final boolean isUnderMaintenance() {
        return (this._mode == ManorStatus.MAINTENANCE);
    }

    public final boolean isManorApproved() {
        return (this._mode == ManorStatus.APPROVED);
    }

    public final boolean isModifiablePeriod() {
        return (this._mode == ManorStatus.MODIFIABLE);
    }

    public final String getCurrentModeName() {
        return this._mode.toString();
    }

    public final String getNextModeChange() {
        return (new SimpleDateFormat("dd/MM HH:mm:ss")).format(this._nextModeChange.getTime());
    }

    public final List<Seed> getCrops() {
        List<Seed> seeds = new ArrayList<>();
        List<Integer> cropIds = new ArrayList<>();
        for (Seed seed : this._seeds.values()) {
            if (!cropIds.contains(seed.getCropId())) {
                seeds.add(seed);
                cropIds.add(seed.getCropId());
            }
        }
        cropIds.clear();
        return seeds;
    }

    public final Set<Seed> getSeedsForCastle(int castleId) {
        return this._seeds.values().stream().filter(s -> (s.getCastleId() == castleId)).collect(Collectors.toSet());
    }

    public final Set<Integer> getSeedIds() {
        return this._seeds.keySet();
    }

    public final Set<Integer> getCropIds() {
        return this._seeds.values().stream().map(Seed::getCropId).collect(Collectors.toSet());
    }

    public final Seed getSeed(int seedId) {
        return this._seeds.get(seedId);
    }

    public final Seed getSeedByCrop(int cropId, int castleId) {
        return this._seeds.values().stream().filter(s -> (s.getCastleId() == castleId && s.getCropId() == cropId)).findFirst().orElse(null);
    }

    public final Seed getSeedByCrop(int cropId) {
        return this._seeds.values().stream().filter(s -> (s.getCropId() == cropId)).findFirst().orElse(null);
    }

    private static class SingletonHolder {
        protected static final CastleManorManager INSTANCE = new CastleManorManager();
    }
}
