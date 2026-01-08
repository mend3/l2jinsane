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
    private final Map<Integer, Seed> _seeds;
    private final Map<Integer, List<CropProcure>> _procure;
    private final Map<Integer, List<CropProcure>> _procureNext;
    private final Map<Integer, List<SeedProduction>> _production;
    private final Map<Integer, List<SeedProduction>> _productionNext;
    private ManorStatus _mode;
    private Calendar _nextModeChange;

    protected CastleManorManager() {
        this._mode = ManorStatus.APPROVED;
        this._nextModeChange = null;
        this._seeds = new HashMap<>();
        this._procure = new HashMap<>();
        this._procureNext = new HashMap<>();
        this._production = new HashMap<>();
        this._productionNext = new HashMap<>();
        if (!Config.ALLOW_MANOR) {
            this._mode = ManorStatus.DISABLED;
            LOGGER.info("Manor system is deactivated.");
        } else {
            this.load();

            try (
                    Connection con = ConnectionPool.getConnection();
                    PreparedStatement stProduction = con.prepareStatement("SELECT * FROM castle_manor_production WHERE castle_id=?");
                    PreparedStatement stProcure = con.prepareStatement("SELECT * FROM castle_manor_procure WHERE castle_id=?");
            ) {
                for (Castle castle : CastleManager.getInstance().getCastles()) {
                    int castleId = castle.getCastleId();
                    List<SeedProduction> pCurrent = new ArrayList<>();
                    List<SeedProduction> pNext = new ArrayList<>();
                    stProduction.clearParameters();
                    stProduction.setInt(1, castleId);

                    try (ResultSet rs = stProduction.executeQuery()) {
                        while (rs.next()) {
                            SeedProduction sp = new SeedProduction(rs.getInt("seed_id"), rs.getInt("amount"), rs.getInt("price"), rs.getInt("start_amount"));
                            if (rs.getBoolean("next_period")) {
                                pNext.add(sp);
                            } else {
                                pCurrent.add(sp);
                            }
                        }
                    }

                    this._production.put(castleId, pCurrent);
                    this._productionNext.put(castleId, pNext);
                    List<CropProcure> current = new ArrayList<>();
                    List<CropProcure> next = new ArrayList<>();
                    stProcure.clearParameters();
                    stProcure.setInt(1, castleId);

                    try (ResultSet rs = stProcure.executeQuery()) {
                        while (rs.next()) {
                            CropProcure cp = new CropProcure(rs.getInt("crop_id"), rs.getInt("amount"), rs.getInt("reward_type"), rs.getInt("start_amount"), rs.getInt("price"));
                            if (rs.getBoolean("next_period")) {
                                next.add(cp);
                            } else {
                                current.add(cp);
                            }
                        }
                    }

                    this._procure.put(castleId, current);
                    this._procureNext.put(castleId, next);
                }
            } catch (Exception e) {
                LOGGER.error("Error restoring manor data.", e);
            }

            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int min = currentTime.get(Calendar.MINUTE);
            int maintenanceMin = Config.ALT_MANOR_REFRESH_MIN + Config.ALT_MANOR_MAINTENANCE_MIN;
            if ((hour < Config.ALT_MANOR_REFRESH_TIME || min < maintenanceMin) && hour >= Config.ALT_MANOR_APPROVE_TIME && (hour != Config.ALT_MANOR_APPROVE_TIME || min > Config.ALT_MANOR_APPROVE_MIN)) {
                if (hour == Config.ALT_MANOR_REFRESH_TIME && min >= Config.ALT_MANOR_REFRESH_MIN && min < maintenanceMin) {
                    this._mode = ManorStatus.MAINTENANCE;
                }
            } else {
                this._mode = ManorStatus.MODIFIABLE;
            }

            this.scheduleModeChange();
            ThreadPool.scheduleAtFixedRate(this::storeMe, Config.ALT_MANOR_SAVE_PERIOD_RATE, Config.ALT_MANOR_SAVE_PERIOD_RATE);
            LOGGER.debug("Current Manor mode is: {}.", new Object[]{this._mode.toString()});
        }
    }

    public static final void updateCurrentProduction(int castleId, Collection<SeedProduction> items) {
        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE castle_manor_production SET amount = ? WHERE castle_id = ? AND seed_id = ? AND next_period = 0");
        ) {
            for (SeedProduction sp : items) {
                ps.setLong(1, (long) sp.getAmount());
                ps.setInt(2, castleId);
                ps.setInt(3, sp.getId());
                ps.addBatch();
            }

            ps.executeBatch();
        } catch (Exception e) {
            LOGGER.error("Unable to store manor data.", e);
        }

    }

    public static final void updateCurrentProcure(int castleId, Collection<CropProcure> items) {
        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE castle_manor_procure SET amount = ? WHERE castle_id = ? AND crop_id = ? AND next_period = 0");
        ) {
            for (CropProcure sp : items) {
                ps.setLong(1, (long) sp.getAmount());
                ps.setInt(2, castleId);
                ps.setInt(3, sp.getId());
                ps.addBatch();
            }

            ps.executeBatch();
        } catch (Exception e) {
            LOGGER.error("Unable to store manor data.", e);
        }

    }

    public static final CastleManorManager getInstance() {
        return CastleManorManager.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/seeds.xml");
        LOGGER.info("Loaded {} seeds.", new Object[]{this._seeds.size()});
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> this.forEach(listNode, "seed", (seedNode) -> {
            StatSet set = this.parseAttributes(seedNode);
            this._seeds.put(set.getInteger("id"), new Seed(set));
        }));
    }

    private final void scheduleModeChange() {
        this._nextModeChange = Calendar.getInstance();
        this._nextModeChange.set(Calendar.SECOND, 0);
        switch (this._mode) {
            case MODIFIABLE:
                this._nextModeChange.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_APPROVE_TIME);
                this._nextModeChange.set(Calendar.MINUTE, Config.ALT_MANOR_APPROVE_MIN);
                if (this._nextModeChange.before(Calendar.getInstance())) {
                    this._nextModeChange.add(Calendar.DATE, 1);
                }
                break;
            case MAINTENANCE:
                this._nextModeChange.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_REFRESH_TIME);
                this._nextModeChange.set(Calendar.MINUTE, Config.ALT_MANOR_REFRESH_MIN + Config.ALT_MANOR_MAINTENANCE_MIN);
                break;
            case APPROVED:
                this._nextModeChange.set(Calendar.HOUR_OF_DAY, Config.ALT_MANOR_REFRESH_TIME);
                this._nextModeChange.set(Calendar.MINUTE, Config.ALT_MANOR_REFRESH_MIN);
        }

        ThreadPool.schedule(this::changeMode, this._nextModeChange.getTimeInMillis() - System.currentTimeMillis());
    }

    public final void changeMode() {
        switch (this._mode) {
            case MODIFIABLE:
                this._mode = ManorStatus.APPROVED;

                for (Castle castle : CastleManager.getInstance().getCastles()) {
                    Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
                    if (owner != null) {
                        int slots = 0;
                        int castleId = castle.getCastleId();
                        ItemContainer cwh = owner.getWarehouse();

                        for (CropProcure crop : this._procureNext.get(castleId)) {
                            if (crop.getStartAmount() > 0 && cwh.getItemsByItemId(this.getSeedByCrop(crop.getId()).getMatureId()) == null) {
                                ++slots;
                            }
                        }

                        long manorCost = this.getManorCost(castleId, true);
                        if (!cwh.validateCapacity(slots) && castle.getTreasury() < manorCost) {
                            (this._productionNext.get(castleId)).clear();
                            (this._procureNext.get(castleId)).clear();
                            ClanMember clanLeader = owner.getLeader();
                            if (clanLeader != null && clanLeader.isOnline()) {
                                clanLeader.getPlayerInstance().sendPacket(SystemMessageId.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION);
                            }
                        } else {
                            castle.addToTreasuryNoTax(-manorCost);
                        }
                    }
                }
                break;
            case MAINTENANCE:
                for (Castle castle : CastleManager.getInstance().getCastles()) {
                    Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
                    if (owner != null) {
                        ClanMember clanLeader = owner.getLeader();
                        if (clanLeader != null && clanLeader.isOnline()) {
                            clanLeader.getPlayerInstance().sendPacket(SystemMessageId.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED);
                        }
                    }
                }

                this._mode = ManorStatus.MODIFIABLE;
                break;
            case APPROVED:
                this._mode = ManorStatus.MAINTENANCE;

                for (Castle castle : CastleManager.getInstance().getCastles()) {
                    Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
                    if (owner != null) {
                        int castleId = castle.getCastleId();
                        ItemContainer cwh = owner.getWarehouse();

                        for (CropProcure crop : this._procure.get(castleId)) {
                            if (crop.getStartAmount() > 0) {
                                if (crop.getStartAmount() != crop.getAmount()) {
                                    int count = (int) ((double) (crop.getStartAmount() - crop.getAmount()) * 0.9);
                                    if (count < 1 && Rnd.nextInt(99) < 90) {
                                        count = 1;
                                    }

                                    if (count > 0) {
                                        cwh.addItem("Manor", this.getSeedByCrop(crop.getId()).getMatureId(), count, null, null);
                                    }
                                }

                                if (crop.getAmount() > 0) {
                                    castle.addToTreasuryNoTax(crop.getAmount() * crop.getPrice());
                                }
                            }
                        }

                        List<SeedProduction> _nextProduction = this._productionNext.get(castleId);
                        List<CropProcure> _nextProcure = this._procureNext.get(castleId);
                        this._production.put(castleId, _nextProduction);
                        this._procure.put(castleId, _nextProcure);
                        if (castle.getTreasury() < this.getManorCost(castleId, false)) {
                            this._productionNext.put(castleId, Collections.emptyList());
                            this._procureNext.put(castleId, Collections.emptyList());
                        } else {
                            List<SeedProduction> production = new ArrayList<>(_nextProduction);

                            for (SeedProduction s : production) {
                                s.setAmount(s.getStartAmount());
                            }

                            this._productionNext.put(castleId, production);
                            List<CropProcure> procure = new ArrayList<>(_nextProcure);

                            for (CropProcure cr : procure) {
                                cr.setAmount(cr.getStartAmount());
                            }

                            this._procureNext.put(castleId, procure);
                        }
                    }
                }

                this.storeMe();
        }

        this.scheduleModeChange();
        LOGGER.debug("Manor mode changed to: {}.", new Object[]{this._mode.toString()});
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
        for (SeedProduction sp : this.getSeedProduction(castleId, nextPeriod)) {
            if (sp.getId() == seedId) {
                return sp;
            }
        }

        return null;
    }

    public final List<CropProcure> getCropProcure(int castleId, boolean nextPeriod) {
        return nextPeriod ? this._procureNext.get(castleId) : this._procure.get(castleId);
    }

    public final CropProcure getCropProcure(int castleId, int cropId, boolean nextPeriod) {
        for (CropProcure cp : this.getCropProcure(castleId, nextPeriod)) {
            if (cp.getId() == cropId) {
                return cp;
            }
        }

        return null;
    }

    public final long getManorCost(int castleId, boolean nextPeriod) {
        List<CropProcure> procure = this.getCropProcure(castleId, nextPeriod);
        List<SeedProduction> production = this.getSeedProduction(castleId, nextPeriod);
        long total = 0L;

        for (SeedProduction seed : production) {
            Seed s = this.getSeed(seed.getId());
            total += s == null ? 1L : (long) (s.getSeedReferencePrice() * seed.getStartAmount());
        }

        for (CropProcure crop : procure) {
            total += crop.getPrice() * crop.getStartAmount();
        }

        return total;
    }

    public final boolean storeMe() {
        try {
            boolean var24;
            try (
                    Connection con = ConnectionPool.getConnection();
                    PreparedStatement ds = con.prepareStatement("DELETE FROM castle_manor_production");
                    PreparedStatement is = con.prepareStatement("INSERT INTO castle_manor_production VALUES (?, ?, ?, ?, ?, ?)");
                    PreparedStatement dp = con.prepareStatement("DELETE FROM castle_manor_procure");
                    PreparedStatement ip = con.prepareStatement("INSERT INTO castle_manor_procure VALUES (?, ?, ?, ?, ?, ?, ?)");
            ) {
                ds.executeUpdate();

                for (Map.Entry<Integer, List<SeedProduction>> entry : this._production.entrySet()) {
                    for (SeedProduction sp : entry.getValue()) {
                        is.setInt(1, (Integer) entry.getKey());
                        is.setInt(2, sp.getId());
                        is.setLong(3, (long) sp.getAmount());
                        is.setLong(4, (long) sp.getStartAmount());
                        is.setLong(5, (long) sp.getPrice());
                        is.setBoolean(6, false);
                        is.addBatch();
                    }
                }

                for (Map.Entry<Integer, List<SeedProduction>> entry : this._productionNext.entrySet()) {
                    for (SeedProduction sp : entry.getValue()) {
                        is.setInt(1, (Integer) entry.getKey());
                        is.setInt(2, sp.getId());
                        is.setLong(3, (long) sp.getAmount());
                        is.setLong(4, (long) sp.getStartAmount());
                        is.setLong(5, (long) sp.getPrice());
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
                        ip.setLong(3, (long) cp.getAmount());
                        ip.setLong(4, (long) cp.getStartAmount());
                        ip.setLong(5, (long) cp.getPrice());
                        ip.setInt(6, cp.getReward());
                        ip.setBoolean(7, false);
                        ip.addBatch();
                    }
                }

                for (Map.Entry<Integer, List<CropProcure>> entry : this._procureNext.entrySet()) {
                    for (CropProcure cp : entry.getValue()) {
                        ip.setInt(1, (Integer) entry.getKey());
                        ip.setInt(2, cp.getId());
                        ip.setLong(3, (long) cp.getAmount());
                        ip.setLong(4, (long) cp.getStartAmount());
                        ip.setLong(5, (long) cp.getPrice());
                        ip.setInt(6, cp.getReward());
                        ip.setBoolean(7, true);
                        ip.addBatch();
                    }
                }

                ip.executeBatch();
                var24 = true;
            }

            return var24;
        } catch (Exception e) {
            LOGGER.error("Unable to store manor data.", e);
            return false;
        }
    }

    public final void resetManorData(int castleId) {
        if (this._mode != ManorStatus.DISABLED) {
            (this._procure.get(castleId)).clear();
            (this._procureNext.get(castleId)).clear();
            (this._production.get(castleId)).clear();
            (this._productionNext.get(castleId)).clear();
        }
    }

    public final boolean isUnderMaintenance() {
        return this._mode == ManorStatus.MAINTENANCE;
    }

    public final boolean isManorApproved() {
        return this._mode == ManorStatus.APPROVED;
    }

    public final boolean isModifiablePeriod() {
        return this._mode == ManorStatus.MODIFIABLE;
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
        return this._seeds.values().stream().filter((s) -> s.getCastleId() == castleId).collect(Collectors.toSet());
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
        return this._seeds.values().stream().filter((s) -> s.getCastleId() == castleId && s.getCropId() == cropId).findFirst().orElse(null);
    }

    public final Seed getSeedByCrop(int cropId) {
        return this._seeds.values().stream().filter((s) -> s.getCropId() == cropId).findFirst().orElse(null);
    }

    private static class SingletonHolder {
        protected static final CastleManorManager INSTANCE = new CastleManorManager();
    }
}
