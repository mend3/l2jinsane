package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
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

        parseFile("./data/xml/seeds.xml");
        LOGGER.info("Loaded {} seeds.", Integer.valueOf(this._seeds.size()));

        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement stProduction = con.prepareStatement("SELECT * FROM castle_manor_production WHERE castle_id=?");
                try {
                    PreparedStatement stProcure = con.prepareStatement("SELECT * FROM castle_manor_procure WHERE castle_id=?");
                    try {
                        for (Castle castle : CastleManager.getInstance().getCastles()) {
                            int castleId = castle.getCastleId();
                            List<SeedProduction> pCurrent = new ArrayList<>();
                            List<SeedProduction> pNext = new ArrayList<>();
                            stProduction.clearParameters();
                            stProduction.setInt(1, castleId);
                            ResultSet rs = stProduction.executeQuery();
                            try {
                                while (rs.next()) {
                                    SeedProduction sp = new SeedProduction(rs.getInt("seed_id"), rs.getInt("amount"), rs.getInt("price"), rs.getInt("start_amount"));
                                    if (rs.getBoolean("next_period")) {
                                        pNext.add(sp);
                                        continue;
                                    }
                                    pCurrent.add(sp);
                                }
                                if (rs != null)
                                    rs.close();
                            } catch (Throwable throwable) {
                                if (rs != null)
                                    try {
                                        rs.close();
                                    } catch (Throwable throwable1) {
                                        throwable.addSuppressed(throwable1);
                                    }
                                throw throwable;
                            }
                            this._production.put(Integer.valueOf(castleId), pCurrent);
                            this._productionNext.put(Integer.valueOf(castleId), pNext);
                            List<CropProcure> current = new ArrayList<>();
                            List<CropProcure> next = new ArrayList<>();
                            stProcure.clearParameters();
                            stProcure.setInt(1, castleId);
                            ResultSet resultSet1 = stProcure.executeQuery();
                            try {
                                while (resultSet1.next()) {
                                    CropProcure cp = new CropProcure(resultSet1.getInt("crop_id"), resultSet1.getInt("amount"), resultSet1.getInt("reward_type"), resultSet1.getInt("start_amount"), resultSet1.getInt("price"));
                                    if (resultSet1.getBoolean("next_period")) {
                                        next.add(cp);
                                        continue;
                                    }
                                    current.add(cp);
                                }
                                if (resultSet1 != null)
                                    resultSet1.close();
                            } catch (Throwable throwable) {
                                if (resultSet1 != null)
                                    try {
                                        resultSet1.close();
                                    } catch (Throwable throwable1) {
                                        throwable.addSuppressed(throwable1);
                                    }
                                throw throwable;
                            }
                            this._procure.put(Integer.valueOf(castleId), current);
                            this._procureNext.put(Integer.valueOf(castleId), next);
                        }
                        if (stProcure != null)
                            stProcure.close();
                    } catch (Throwable throwable) {
                        if (stProcure != null)
                            try {
                                stProcure.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (stProduction != null)
                        stProduction.close();
                } catch (Throwable throwable) {
                    if (stProduction != null)
                        try {
                            stProduction.close();
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
            LOGGER.error("Error restoring manor data.", e);
        }
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(11);
        int min = currentTime.get(12);
        int maintenanceMin = Config.ALT_MANOR_REFRESH_MIN + Config.ALT_MANOR_MAINTENANCE_MIN;
        if ((hour >= Config.ALT_MANOR_REFRESH_TIME && min >= maintenanceMin) || hour < Config.ALT_MANOR_APPROVE_TIME || (hour == Config.ALT_MANOR_APPROVE_TIME && min <= Config.ALT_MANOR_APPROVE_MIN)) {
            this._mode = ManorStatus.MODIFIABLE;
        } else if (hour == Config.ALT_MANOR_REFRESH_TIME && min >= Config.ALT_MANOR_REFRESH_MIN && min < maintenanceMin) {
            this._mode = ManorStatus.MAINTENANCE;
        }
        scheduleModeChange();
        ThreadPool.scheduleAtFixedRate(this::storeMe, Config.ALT_MANOR_SAVE_PERIOD_RATE, Config.ALT_MANOR_SAVE_PERIOD_RATE);
        LOGGER.debug("Current Manor mode is: {}.", this._mode.toString());
    }

    public void parseDocument(Document doc, Path path) {
        forEach(doc, "list", listNode -> forEach(listNode, "seed", nnn -> {
        }));
    }

    private final void scheduleModeChange() {
        this._nextModeChange = Calendar.getInstance();
        this._nextModeChange.set(13, 0);
        switch (this._mode) {
            case MODIFIABLE:
                this._nextModeChange.set(11, Config.ALT_MANOR_APPROVE_TIME);
                this._nextModeChange.set(12, Config.ALT_MANOR_APPROVE_MIN);
                if (this._nextModeChange.before(Calendar.getInstance()))
                    this._nextModeChange.add(5, 1);
                break;
            case MAINTENANCE:
                this._nextModeChange.set(11, Config.ALT_MANOR_REFRESH_TIME);
                this._nextModeChange.set(12, Config.ALT_MANOR_REFRESH_MIN + Config.ALT_MANOR_MAINTENANCE_MIN);
                break;
            case APPROVED:
                this._nextModeChange.set(11, Config.ALT_MANOR_REFRESH_TIME);
                this._nextModeChange.set(12, Config.ALT_MANOR_REFRESH_MIN);
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
                    for (CropProcure crop : this._procure.get(Integer.valueOf(castleId))) {
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
                    List<SeedProduction> _nextProduction = this._productionNext.get(Integer.valueOf(castleId));
                    List<CropProcure> _nextProcure = this._procureNext.get(Integer.valueOf(castleId));
                    this._production.put(Integer.valueOf(castleId), _nextProduction);
                    this._procure.put(Integer.valueOf(castleId), _nextProcure);
                    if (castle.getTreasury() < getManorCost(castleId, false)) {
                        this._productionNext.put(Integer.valueOf(castleId), Collections.emptyList());
                        this._procureNext.put(Integer.valueOf(castleId), Collections.emptyList());
                        continue;
                    }
                    List<SeedProduction> production = new ArrayList<>(_nextProduction);
                    for (SeedProduction s : production)
                        s.setAmount(s.getStartAmount());
                    this._productionNext.put(Integer.valueOf(castleId), production);
                    List<CropProcure> procure = new ArrayList<>(_nextProcure);
                    for (CropProcure cr : procure)
                        cr.setAmount(cr.getStartAmount());
                    this._procureNext.put(Integer.valueOf(castleId), procure);
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
                    for (CropProcure crop : this._procureNext.get(Integer.valueOf(castleId))) {
                        if (crop.getStartAmount() > 0 && cwh.getItemsByItemId(getSeedByCrop(crop.getId()).getMatureId()) == null)
                            slots++;
                    }
                    long manorCost = getManorCost(castleId, true);
                    if (!cwh.validateCapacity(slots) && castle.getTreasury() < manorCost) {
                        this._productionNext.get(Integer.valueOf(castleId)).clear();
                        this._procureNext.get(Integer.valueOf(castleId)).clear();
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
        this._productionNext.put(Integer.valueOf(castleId), list);
    }

    public final void setNextCropProcure(List<CropProcure> list, int castleId) {
        this._procureNext.put(Integer.valueOf(castleId), list);
    }

    public final List<SeedProduction> getSeedProduction(int castleId, boolean nextPeriod) {
        return nextPeriod ? this._productionNext.get(Integer.valueOf(castleId)) : this._production.get(Integer.valueOf(castleId));
    }

    public final SeedProduction getSeedProduct(int castleId, int seedId, boolean nextPeriod) {
        for (SeedProduction sp : getSeedProduction(castleId, nextPeriod)) {
            if (sp.getId() == seedId)
                return sp;
        }
        return null;
    }

    public final List<CropProcure> getCropProcure(int castleId, boolean nextPeriod) {
        return nextPeriod ? this._procureNext.get(Integer.valueOf(castleId)) : this._procure.get(Integer.valueOf(castleId));
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
        this._procure.get(Integer.valueOf(castleId)).clear();
        this._procureNext.get(Integer.valueOf(castleId)).clear();
        this._production.get(Integer.valueOf(castleId)).clear();
        this._productionNext.get(Integer.valueOf(castleId)).clear();
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
            if (!cropIds.contains(Integer.valueOf(seed.getCropId()))) {
                seeds.add(seed);
                cropIds.add(Integer.valueOf(seed.getCropId()));
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
        return this._seeds.get(Integer.valueOf(seedId));
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
