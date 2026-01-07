/**/
package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.SepulcherNpc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class FourSepulchersManager {
    protected static final CLogger LOGGER = new CLogger(FourSepulchersManager.class.getName());
    private static final String QUEST_ID = "Q620_FourGoblets";
    private static final int ENTRANCE_PASS = 7075;
    private static final int USED_PASS = 7261;
    private static final int CHAPEL_KEY = 7260;
    private static final int ANTIQUE_BROOCH = 7262;
    private final Map<Integer, Map<Integer, SpawnLocation>> _shadowSpawnLoc = new HashMap();
    private final Map<Integer, Boolean> _archonSpawned = new HashMap();
    private final Map<Integer, Boolean> _hallInUse = new HashMap();
    private final Map<Integer, Location> _startHallSpawns = new HashMap();
    private final Map<Integer, Integer> _hallGateKeepers = new HashMap();
    private final Map<Integer, Integer> _keyBoxNpc = new HashMap();
    private final Map<Integer, Integer> _victim = new HashMap();
    private final Map<Integer, L2Spawn> _executionerSpawns = new HashMap();
    private final Map<Integer, L2Spawn> _keyBoxSpawns = new HashMap();
    private final Map<Integer, L2Spawn> _mysteriousBoxSpawns = new HashMap();
    private final Map<Integer, L2Spawn> _shadowSpawns = new HashMap();
    private final Map<Integer, List<L2Spawn>> _dukeFinalMobs = new HashMap();
    private final Map<Integer, List<Npc>> _dukeMobs = new HashMap();
    private final Map<Integer, List<L2Spawn>> _emperorsGraveNpcs = new HashMap();
    private final Map<Integer, List<L2Spawn>> _magicalMonsters = new HashMap();
    private final Map<Integer, List<L2Spawn>> _physicalMonsters = new HashMap();
    private final Map<Integer, List<Npc>> _viscountMobs = new HashMap();
    private final List<L2Spawn> _managers = new ArrayList();
    private final List<Npc> _allMobs = new ArrayList();
    protected FourSepulchersManager.State _state = FourSepulchersManager.State.ENTRY;

    public static FourSepulchersManager getInstance() {
        return FourSepulchersManager.SingletonHolder.INSTANCE;
    }

    public void load() {
        this._state = FourSepulchersManager.State.ENTRY;
        this.initFixedInfo();
        this.loadMysteriousBox();
        this.initKeyBoxSpawns();
        this.loadSpawnsByType(1);
        this.loadSpawnsByType(2);
        this.initShadowSpawns();
        this.initExecutionerSpawns();
        this.loadSpawnsByType(5);
        this.loadSpawnsByType(6);
        this.spawnManagers();
        this.launchCycle();
    }

    protected void initFixedInfo() {
        Map<Integer, SpawnLocation> temp = new HashMap();
        temp.put(25339, new SpawnLocation(191231, -85574, -7216, 33380));
        temp.put(25349, new SpawnLocation(189534, -88969, -7216, 32768));
        temp.put(25346, new SpawnLocation(173195, -76560, -7215, 49277));
        temp.put(25342, new SpawnLocation(175591, -72744, -7215, 49317));
        this._shadowSpawnLoc.put(0, temp);
        temp = new HashMap();
        temp.put(25342, new SpawnLocation(191231, -85574, -7216, 33380));
        temp.put(25339, new SpawnLocation(189534, -88969, -7216, 32768));
        temp.put(25349, new SpawnLocation(173195, -76560, -7215, 49277));
        temp.put(25346, new SpawnLocation(175591, -72744, -7215, 49317));
        this._shadowSpawnLoc.put(1, temp);
        temp = new HashMap();
        temp.put(25346, new SpawnLocation(191231, -85574, -7216, 33380));
        temp.put(25342, new SpawnLocation(189534, -88969, -7216, 32768));
        temp.put(25339, new SpawnLocation(173195, -76560, -7215, 49277));
        temp.put(25349, new SpawnLocation(175591, -72744, -7215, 49317));
        this._shadowSpawnLoc.put(2, temp);
        temp = new HashMap();
        temp.put(25349, new SpawnLocation(191231, -85574, -7216, 33380));
        temp.put(25346, new SpawnLocation(189534, -88969, -7216, 32768));
        temp.put(25342, new SpawnLocation(173195, -76560, -7215, 49277));
        temp.put(25339, new SpawnLocation(175591, -72744, -7215, 49317));
        this._shadowSpawnLoc.put(3, temp);
        this._startHallSpawns.put(31921, new Location(181632, -85587, -7218));
        this._startHallSpawns.put(31922, new Location(179963, -88978, -7218));
        this._startHallSpawns.put(31923, new Location(173217, -86132, -7218));
        this._startHallSpawns.put(31924, new Location(175608, -82296, -7218));
        this._hallInUse.put(31921, false);
        this._hallInUse.put(31922, false);
        this._hallInUse.put(31923, false);
        this._hallInUse.put(31924, false);
        this._hallGateKeepers.put(31925, 25150012);
        this._hallGateKeepers.put(31926, 25150013);
        this._hallGateKeepers.put(31927, 25150014);
        this._hallGateKeepers.put(31928, 25150015);
        this._hallGateKeepers.put(31929, 25150016);
        this._hallGateKeepers.put(31930, 25150002);
        this._hallGateKeepers.put(31931, 25150003);
        this._hallGateKeepers.put(31932, 25150004);
        this._hallGateKeepers.put(31933, 25150005);
        this._hallGateKeepers.put(31934, 25150006);
        this._hallGateKeepers.put(31935, 25150032);
        this._hallGateKeepers.put(31936, 25150033);
        this._hallGateKeepers.put(31937, 25150034);
        this._hallGateKeepers.put(31938, 25150035);
        this._hallGateKeepers.put(31939, 25150036);
        this._hallGateKeepers.put(31940, 25150022);
        this._hallGateKeepers.put(31941, 25150023);
        this._hallGateKeepers.put(31942, 25150024);
        this._hallGateKeepers.put(31943, 25150025);
        this._hallGateKeepers.put(31944, 25150026);
        this._keyBoxNpc.put(18120, 31455);
        this._keyBoxNpc.put(18121, 31455);
        this._keyBoxNpc.put(18122, 31455);
        this._keyBoxNpc.put(18123, 31455);
        this._keyBoxNpc.put(18124, 31456);
        this._keyBoxNpc.put(18125, 31456);
        this._keyBoxNpc.put(18126, 31456);
        this._keyBoxNpc.put(18127, 31456);
        this._keyBoxNpc.put(18128, 31457);
        this._keyBoxNpc.put(18129, 31457);
        this._keyBoxNpc.put(18130, 31457);
        this._keyBoxNpc.put(18131, 31457);
        this._keyBoxNpc.put(18149, 31458);
        this._keyBoxNpc.put(18150, 31459);
        this._keyBoxNpc.put(18151, 31459);
        this._keyBoxNpc.put(18152, 31459);
        this._keyBoxNpc.put(18153, 31459);
        this._keyBoxNpc.put(18154, 31460);
        this._keyBoxNpc.put(18155, 31460);
        this._keyBoxNpc.put(18156, 31460);
        this._keyBoxNpc.put(18157, 31460);
        this._keyBoxNpc.put(18158, 31461);
        this._keyBoxNpc.put(18159, 31461);
        this._keyBoxNpc.put(18160, 31461);
        this._keyBoxNpc.put(18161, 31461);
        this._keyBoxNpc.put(18162, 31462);
        this._keyBoxNpc.put(18163, 31462);
        this._keyBoxNpc.put(18164, 31462);
        this._keyBoxNpc.put(18165, 31462);
        this._keyBoxNpc.put(18183, 31463);
        this._keyBoxNpc.put(18184, 31464);
        this._keyBoxNpc.put(18212, 31465);
        this._keyBoxNpc.put(18213, 31465);
        this._keyBoxNpc.put(18214, 31465);
        this._keyBoxNpc.put(18215, 31465);
        this._keyBoxNpc.put(18216, 31466);
        this._keyBoxNpc.put(18217, 31466);
        this._keyBoxNpc.put(18218, 31466);
        this._keyBoxNpc.put(18219, 31466);
        this._victim.put(18150, 18158);
        this._victim.put(18151, 18159);
        this._victim.put(18152, 18160);
        this._victim.put(18153, 18161);
        this._victim.put(18154, 18162);
        this._victim.put(18155, 18163);
        this._victim.put(18156, 18164);
        this._victim.put(18157, 18165);
    }

    private void loadMysteriousBox() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM spawnlist_4s WHERE spawntype = 0 ORDER BY id");

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            NpcTemplate template = NpcData.getInstance().getTemplate(rs.getInt("npc_templateid"));
                            if (template == null) {
                                LOGGER.warn("Data missing in NPC table for ID: {}.", rs.getInt("npc_templateid"));
                            } else {
                                L2Spawn spawn = new L2Spawn(template);
                                spawn.setLoc(rs.getInt("locx"), rs.getInt("locy"), rs.getInt("locz"), rs.getInt("heading"));
                                spawn.setRespawnDelay(rs.getInt("respawn_delay"));
                                SpawnTable.getInstance().addSpawn(spawn, false);
                                this._mysteriousBoxSpawns.put(rs.getInt("key_npc_id"), spawn);
                            }
                        }
                    } catch (Throwable var9) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var10) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var11) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var6) {
                        var11.addSuppressed(var6);
                    }
                }

                throw var11;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var12) {
            LOGGER.error("Failed to initialize a spawn.", var12);
        }

        LOGGER.info("Loaded {} Mysterious-Box.", this._mysteriousBoxSpawns.size());
    }

    private void initKeyBoxSpawns() {
        Iterator var1 = this._keyBoxNpc.entrySet().iterator();

        while (var1.hasNext()) {
            Entry keyNpc = (Entry) var1.next();

            try {
                NpcTemplate template = NpcData.getInstance().getTemplate((Integer) keyNpc.getValue());
                if (template == null) {
                    LOGGER.warn("Data missing in NPC table for ID: {}.", keyNpc.getValue());
                } else {
                    L2Spawn spawn = new L2Spawn(template);
                    SpawnTable.getInstance().addSpawn(spawn, false);
                    this._keyBoxSpawns.put((Integer) keyNpc.getKey(), spawn);
                }
            } catch (Exception var5) {
                LOGGER.error("Failed to initialize a spawn.", var5);
            }
        }

        LOGGER.info("Loaded {} Key-Box.", this._keyBoxNpc.size());
    }

    private void loadSpawnsByType(int type) {
        int loaded = 0;

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT Distinct key_npc_id FROM spawnlist_4s WHERE spawntype = ? ORDER BY key_npc_id");

                try {
                    ps.setInt(1, type);
                    ResultSet rs = ps.executeQuery();

                    try {
                        PreparedStatement ps2 = con.prepareStatement("SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM spawnlist_4s WHERE key_npc_id = ? AND spawntype = ? ORDER BY id");

                        try {
                            while (rs.next()) {
                                int keyNpcId = rs.getInt("key_npc_id");
                                ps2.setInt(1, keyNpcId);
                                ps2.setInt(2, type);
                                ResultSet rs2 = ps2.executeQuery();

                                try {
                                    ps2.clearParameters();
                                    ArrayList spawns = new ArrayList();

                                    while (rs2.next()) {
                                        NpcTemplate template = NpcData.getInstance().getTemplate(rs2.getInt("npc_templateid"));
                                        if (template == null) {
                                            LOGGER.warn("Data missing in NPC table for ID: {}.", rs2.getInt("npc_templateid"));
                                        } else {
                                            L2Spawn spawn = new L2Spawn(template);
                                            spawn.setLoc(rs2.getInt("locx"), rs2.getInt("locy"), rs2.getInt("locz"), rs2.getInt("heading"));
                                            spawn.setRespawnDelay(rs2.getInt("respawn_delay"));
                                            SpawnTable.getInstance().addSpawn(spawn, false);
                                            spawns.add(spawn);
                                            ++loaded;
                                        }
                                    }

                                    if (type == 1) {
                                        this._physicalMonsters.put(keyNpcId, spawns);
                                    } else if (type == 2) {
                                        this._magicalMonsters.put(keyNpcId, spawns);
                                    } else if (type == 5) {
                                        this._dukeFinalMobs.put(keyNpcId, spawns);
                                        this._archonSpawned.put(keyNpcId, false);
                                    } else if (type == 6) {
                                        this._emperorsGraveNpcs.put(keyNpcId, spawns);
                                    }
                                } catch (Throwable var17) {
                                    if (rs2 != null) {
                                        try {
                                            rs2.close();
                                        } catch (Throwable var16) {
                                            var17.addSuppressed(var16);
                                        }
                                    }

                                    throw var17;
                                }

                                if (rs2 != null) {
                                    rs2.close();
                                }
                            }
                        } catch (Throwable var18) {
                            if (ps2 != null) {
                                try {
                                    ps2.close();
                                } catch (Throwable var15) {
                                    var18.addSuppressed(var15);
                                }
                            }

                            throw var18;
                        }

                        if (ps2 != null) {
                            ps2.close();
                        }
                    } catch (Throwable var19) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var14) {
                                var19.addSuppressed(var14);
                            }
                        }

                        throw var19;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var20) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var13) {
                            var20.addSuppressed(var13);
                        }
                    }

                    throw var20;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var21) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var12) {
                        var21.addSuppressed(var12);
                    }
                }

                throw var21;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var22) {
            LOGGER.error("Failed to initialize a spawn.", var22);
        }

        if (type == 1) {
            LOGGER.info("Loaded {} physical type monsters.", loaded);
        } else if (type == 2) {
            LOGGER.info("Loaded {} magical type monsters.", loaded);
        } else if (type == 5) {
            LOGGER.info("Loaded {} Duke's Hall Gatekeepers.", loaded);
        } else if (type == 6) {
            LOGGER.info("Loaded {} Emperor's Grave monsters.", loaded);
        }

    }

    private void initShadowSpawns() {
        int[] gateKeeper = new int[]{31929, 31934, 31939, 31944};
        Map<Integer, SpawnLocation> newLoc = this._shadowSpawnLoc.get(Rnd.get(4));
        int index = 0;
        Iterator var4 = newLoc.entrySet().iterator();

        while (var4.hasNext()) {
            Entry<Integer, SpawnLocation> entry = (Entry) var4.next();
            NpcTemplate template = NpcData.getInstance().getTemplate(entry.getKey());
            if (template == null) {
                LOGGER.warn("Data missing in NPC table for ID: {}.", entry.getKey());
            } else {
                try {
                    L2Spawn spawn = new L2Spawn(template);
                    spawn.setLoc(entry.getValue());
                    SpawnTable.getInstance().addSpawn(spawn, false);
                    this._shadowSpawns.put(gateKeeper[index], spawn);
                    ++index;
                } catch (Exception var8) {
                    LOGGER.error("Failed to initialize a spawn.", var8);
                }
            }
        }

        LOGGER.info("Loaded {} Shadows of Halisha.", this._shadowSpawns.size());
    }

    private void initExecutionerSpawns() {
        Iterator var1 = this._victim.entrySet().iterator();

        while (var1.hasNext()) {
            Entry victimNpc = (Entry) var1.next();

            try {
                NpcTemplate template = NpcData.getInstance().getTemplate((Integer) victimNpc.getValue());
                if (template == null) {
                    LOGGER.warn("Data missing in NPC table for ID: {}.", victimNpc.getValue());
                } else {
                    L2Spawn spawn = new L2Spawn(template);
                    SpawnTable.getInstance().addSpawn(spawn, false);
                    this._executionerSpawns.put((Integer) victimNpc.getKey(), spawn);
                }
            } catch (Exception var5) {
                LOGGER.error("Failed to initialize a spawn.", var5);
            }
        }

    }

    private void spawnManagers() {
        for (int i = 31921; i <= 31924; ++i) {
            NpcTemplate template = NpcData.getInstance().getTemplate(i);
            if (template != null) {
                try {
                    L2Spawn spawn = new L2Spawn(template);
                    spawn.setRespawnDelay(60);
                    switch (i) {
                        case 31921:
                            spawn.setLoc(181061, -85595, -7200, -32584);
                            break;
                        case 31922:
                            spawn.setLoc(179292, -88981, -7200, -33272);
                            break;
                        case 31923:
                            spawn.setLoc(173202, -87004, -7200, -16248);
                            break;
                        case 31924:
                            spawn.setLoc(175606, -82853, -7200, -16248);
                    }

                    SpawnTable.getInstance().addSpawn(spawn, false);
                    spawn.doSpawn(false);
                    spawn.setRespawnState(true);
                    this._managers.add(spawn);
                } catch (Exception var4) {
                    LOGGER.error("Failed to spawn managers.", var4);
                }
            }
        }

        LOGGER.info("Loaded {} managers.", this._managers.size());
    }

    private void launchCycle() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        ThreadPool.scheduleAtFixedRate(new FourSepulchersManager.Cycle(), cal.getTimeInMillis() - System.currentTimeMillis(), 60000L);
    }

    public boolean isEntryTime() {
        return this._state == FourSepulchersManager.State.ENTRY;
    }

    public boolean isAttackTime() {
        return this._state == FourSepulchersManager.State.ATTACK;
    }

    public Map<Integer, Integer> getHallGateKeepers() {
        return this._hallGateKeepers;
    }

    public synchronized void tryEntry(Npc npc, Player player) {
        int npcId = npc.getNpcId();
        switch (npcId) {
            case 31921:
            case 31922:
            case 31923:
            case 31924:
                if (this._hallInUse.get(npcId)) {
                    this.showHtmlFile(player, npcId + "-FULL.htm", npc, null);
                    return;
                } else {
                    Party party = player.getParty();
                    if (party != null && party.getMembersCount() >= Config.FS_PARTY_MEMBER_COUNT) {
                        if (!party.isLeader(player)) {
                            this.showHtmlFile(player, npcId + "-NL.htm", npc, null);
                            return;
                        }

                        Iterator var5 = party.getMembers().iterator();

                        Player member;
                        do {
                            if (!var5.hasNext()) {
                                if (!this.isEntryTime()) {
                                    this.showHtmlFile(player, npcId + "-NE.htm", npc, null);
                                    return;
                                }

                                this.showHtmlFile(player, npcId + "-OK.htm", npc, null);
                                Location loc = this._startHallSpawns.get(npcId);
                                Iterator var10 = ((List) party.getMembers().stream().filter((m) -> {
                                    return !m.isDead() && MathUtil.checkIfInRange(700, player, m, true);
                                }).collect(Collectors.toList())).iterator();

                                while (var10.hasNext()) {
                                    member = (Player) var10.next();
                                    ZoneManager.getInstance().getZone(loc.getX(), loc.getY(), loc.getZ(), BossZone.class).allowPlayerEntry(member, 30);
                                    member.teleportTo(loc, 80);
                                    member.destroyItemByItemId("Quest", 7075, 1, member, true);
                                    if (member.getInventory().getItemByItemId(7262) == null) {
                                        member.addItem("Quest", 7261, 1, member, true);
                                    }

                                    ItemInstance key = member.getInventory().getItemByItemId(7260);
                                    if (key != null) {
                                        member.destroyItemByItemId("Quest", 7260, key.getCount(), member, true);
                                    }
                                }

                                this._hallInUse.put(npcId, true);
                                return;
                            }

                            member = (Player) var5.next();
                            QuestState qs = member.getQuestState("Q620_FourGoblets");
                            if (qs == null || !qs.isStarted() && !qs.isCompleted()) {
                                this.showHtmlFile(player, npcId + "-NS.htm", npc, member);
                                return;
                            }

                            if (member.getInventory().getItemByItemId(7075) == null) {
                                this.showHtmlFile(player, npcId + "-SE.htm", npc, member);
                                return;
                            }
                        } while (member.getWeightPenalty() <= 2);

                        member.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
                        return;
                    }

                    this.showHtmlFile(player, npcId + "-SP.htm", npc, null);
                    return;
                }
            default:
        }
    }

    public void spawnMysteriousBox(int npcId) {
        if (this.isAttackTime()) {
            L2Spawn spawn = this._mysteriousBoxSpawns.get(npcId);
            if (spawn != null) {
                this._allMobs.add(spawn.doSpawn(false));
                spawn.setRespawnState(false);
            }

        }
    }

    public void spawnMonster(int npcId) {
        if (this.isAttackTime()) {
            List<L2Spawn> monsterList = Rnd.nextBoolean() ? this._physicalMonsters.get(npcId) : this._magicalMonsters.get(npcId);
            List<Npc> mobs = new ArrayList();
            boolean spawnKeyBoxMob = false;
            boolean spawnedKeyBoxMob = false;
            Iterator var6 = monsterList.iterator();

            while (true) {
                Npc mob;
                while (true) {
                    if (!var6.hasNext()) {
                        switch (npcId) {
                            case 31469:
                            case 31474:
                            case 31479:
                            case 31484:
                                this._viscountMobs.put(npcId, mobs);
                            case 31470:
                            case 31471:
                            case 31473:
                            case 31475:
                            case 31476:
                            case 31478:
                            case 31480:
                            case 31481:
                            case 31483:
                            case 31485:
                            case 31486:
                            default:
                                break;
                            case 31472:
                            case 31477:
                            case 31482:
                            case 31487:
                                this._dukeMobs.put(npcId, mobs);
                        }

                        return;
                    }

                    L2Spawn spawn = (L2Spawn) var6.next();
                    if (spawnedKeyBoxMob) {
                        spawnKeyBoxMob = false;
                    } else {
                        switch (npcId) {
                            case 31469:
                            case 31474:
                            case 31479:
                            case 31484:
                                if (Rnd.get(48) == 0) {
                                    spawnKeyBoxMob = true;
                                }
                                break;
                            default:
                                spawnKeyBoxMob = false;
                        }
                    }

                    mob = null;
                    if (spawnKeyBoxMob) {
                        try {
                            NpcTemplate template = NpcData.getInstance().getTemplate(18149);
                            if (template == null) {
                                LOGGER.warn("Data missing in NPC table for ID: 18149.");
                                continue;
                            }

                            L2Spawn keyBoxMobSpawn = new L2Spawn(template);
                            keyBoxMobSpawn.setLoc(spawn.getLoc());
                            keyBoxMobSpawn.setRespawnDelay(3600);
                            SpawnTable.getInstance().addSpawn(keyBoxMobSpawn, false);
                            mob = keyBoxMobSpawn.doSpawn(false);
                            keyBoxMobSpawn.setRespawnState(false);
                        } catch (Exception var11) {
                            LOGGER.error("Failed to initialize a spawn.", var11);
                        }

                        spawnedKeyBoxMob = true;
                        break;
                    }

                    mob = spawn.doSpawn(false);
                    spawn.setRespawnState(false);
                    break;
                }

                if (mob != null) {
                    mob.setScriptValue(npcId);
                    switch (npcId) {
                        case 31469:
                        case 31472:
                        case 31474:
                        case 31477:
                        case 31479:
                        case 31482:
                        case 31484:
                        case 31487:
                            mobs.add(mob);
                        case 31470:
                        case 31471:
                        case 31473:
                        case 31475:
                        case 31476:
                        case 31478:
                        case 31480:
                        case 31481:
                        case 31483:
                        case 31485:
                        case 31486:
                        default:
                            this._allMobs.add(mob);
                    }
                }
            }
        }
    }

    public synchronized void testViscountMobsAnnihilation(int npcId) {
        List<Npc> mobs = this._viscountMobs.get(npcId);
        if (mobs != null) {
            Iterator var3 = mobs.iterator();

            Npc mob;
            do {
                if (!var3.hasNext()) {
                    this.spawnMonster(npcId);
                    return;
                }

                mob = (Npc) var3.next();
            } while (mob.isDead());

        }
    }

    public synchronized void testDukeMobsAnnihilation(int npcId) {
        List<Npc> mobs = this._dukeMobs.get(npcId);
        if (mobs != null) {
            Iterator var3 = mobs.iterator();

            Npc mob;
            do {
                if (!var3.hasNext()) {
                    this.spawnArchonOfHalisha(npcId);
                    return;
                }

                mob = (Npc) var3.next();
            } while (mob.isDead());

        }
    }

    public void spawnKeyBox(Npc npc) {
        if (this.isAttackTime()) {
            L2Spawn spawn = this._keyBoxSpawns.get(npc.getNpcId());
            if (spawn != null) {
                spawn.setLoc(npc.getPosition());
                spawn.setRespawnDelay(3600);
                this._allMobs.add(spawn.doSpawn(false));
                spawn.setRespawnState(false);
            }

        }
    }

    public void spawnExecutionerOfHalisha(Npc npc) {
        if (this.isAttackTime()) {
            L2Spawn spawn = this._executionerSpawns.get(npc.getNpcId());
            if (spawn != null) {
                spawn.setLoc(npc.getPosition());
                spawn.setRespawnDelay(3600);
                this._allMobs.add(spawn.doSpawn(false));
                spawn.setRespawnState(false);
            }

        }
    }

    public void spawnArchonOfHalisha(int npcId) {
        if (this.isAttackTime()) {
            if (!(Boolean) this._archonSpawned.get(npcId)) {
                List<L2Spawn> monsterList = this._dukeFinalMobs.get(npcId);
                if (monsterList != null) {
                    Iterator var3 = monsterList.iterator();

                    while (var3.hasNext()) {
                        L2Spawn spawn = (L2Spawn) var3.next();
                        Npc mob = spawn.doSpawn(false);
                        spawn.setRespawnState(false);
                        if (mob != null) {
                            mob.setScriptValue(npcId);
                            this._allMobs.add(mob);
                        }
                    }

                    this._archonSpawned.put(npcId, true);
                }

            }
        }
    }

    public void spawnEmperorsGraveNpc(int npcId) {
        if (this.isAttackTime()) {
            List<L2Spawn> monsterList = this._emperorsGraveNpcs.get(npcId);
            if (monsterList != null) {
                Iterator var3 = monsterList.iterator();

                while (var3.hasNext()) {
                    L2Spawn spawn = (L2Spawn) var3.next();
                    this._allMobs.add(spawn.doSpawn(false));
                    spawn.setRespawnState(false);
                }
            }

        }
    }

    public void spawnShadow(int npcId) {
        if (this.isAttackTime()) {
            L2Spawn spawn = this._shadowSpawns.get(npcId);
            if (spawn != null) {
                Npc mob = spawn.doSpawn(false);
                spawn.setRespawnState(false);
                if (mob != null) {
                    mob.setScriptValue(npcId);
                    this._allMobs.add(mob);
                }
            }

        }
    }

    public void showHtmlFile(Player player, String file, Npc npc, Player member) {
        NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
        html.setFile("data/html/sepulchers/" + file);
        if (member != null) {
            html.replace("%member%", member.getName());
        }

        player.sendPacket(html);
    }

    protected void onEntryEvent() {
        String msg1 = "You may now enter the Sepulcher.";
        String msg2 = "If you place your hand on the stone statue in front of each sepulcher, you will be able to enter.";
        Iterator var3 = this._managers.iterator();

        while (var3.hasNext()) {
            L2Spawn temp = (L2Spawn) var3.next();
            ((SepulcherNpc) temp.getNpc()).sayInShout(msg1);
            ((SepulcherNpc) temp.getNpc()).sayInShout(msg2);
        }

    }

    protected void onAttackEvent() {
        Map<Integer, SpawnLocation> newLoc = this._shadowSpawnLoc.get(Rnd.get(4));
        Iterator var2 = this._shadowSpawns.values().iterator();

        while (var2.hasNext()) {
            L2Spawn spawn = (L2Spawn) var2.next();
            SpawnLocation spawnLoc = newLoc.get(spawn.getNpcId());
            if (spawnLoc != null) {
                spawn.setLoc(spawnLoc);
            }
        }

        this.spawnMysteriousBox(31921);
        this.spawnMysteriousBox(31922);
        this.spawnMysteriousBox(31923);
        this.spawnMysteriousBox(31924);
    }

    protected void onEndEvent() {
        Iterator var1 = this._managers.iterator();

        while (var1.hasNext()) {
            L2Spawn temp = (L2Spawn) var1.next();
            if (this._hallInUse.get(temp.getNpcId())) {
                ((SepulcherNpc) temp.getNpc()).sayInShout("Game over. The teleport will appear momentarily.");
            }
        }

        var1 = this._startHallSpawns.values().iterator();

        while (var1.hasNext()) {
            Location loc = (Location) var1.next();
            List<Player> players = ZoneManager.getInstance().getZone(loc.getX(), loc.getY(), loc.getZ(), BossZone.class).oustAllPlayers();
            Iterator var4 = players.iterator();

            while (var4.hasNext()) {
                Player player = (Player) var4.next();
                ItemInstance key = player.getInventory().getItemByItemId(7260);
                if (key != null) {
                    player.destroyItemByItemId("Quest", 7260, key.getCount(), player, false);
                }
            }
        }

        Npc mob;
        for (var1 = this._allMobs.iterator(); var1.hasNext(); mob.deleteMe()) {
            mob = (Npc) var1.next();
            if (mob.getSpawn() != null) {
                mob.getSpawn().setRespawnState(false);
            }
        }

        this._allMobs.clear();
        var1 = this._hallGateKeepers.values().iterator();

        while (var1.hasNext()) {
            int doorId = (Integer) var1.next();
            Door door = DoorData.getInstance().getDoor(doorId);
            if (door != null) {
                door.closeMe();
            }
        }

        this._hallInUse.replaceAll((npcId, used) -> {
            return false;
        });
        this._archonSpawned.replaceAll((npcId, spawned) -> {
            return false;
        });
    }

    protected void managersShout(int currentMinute) {
        if (this._state == FourSepulchersManager.State.ATTACK && currentMinute != 0) {
            int modulo = currentMinute % 5;
            if (modulo == 0) {
                String msg = currentMinute + " minute(s) have passed.";
                Iterator var4 = this._managers.iterator();

                while (var4.hasNext()) {
                    L2Spawn temp = (L2Spawn) var4.next();
                    if (this._hallInUse.get(temp.getNpcId())) {
                        ((SepulcherNpc) temp.getNpc()).sayInShout(msg);
                    }
                }
            }
        }

    }

    private enum State {
        ENTRY,
        ATTACK,
        END;

        // $FF: synthetic method
        private static FourSepulchersManager.State[] $values() {
            return new FourSepulchersManager.State[]{ENTRY, ATTACK, END};
        }
    }

    private static class SingletonHolder {
        protected static final FourSepulchersManager INSTANCE = new FourSepulchersManager();
    }

    protected class Cycle implements Runnable {
        public void run() {
            int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
            FourSepulchersManager.State newState = FourSepulchersManager.State.ATTACK;
            if (currentMinute >= Config.FS_TIME_ENTRY) {
                newState = FourSepulchersManager.State.ENTRY;
            } else if (currentMinute >= Config.FS_TIME_END) {
                newState = FourSepulchersManager.State.END;
            }

            if (newState != FourSepulchersManager.this._state) {
                FourSepulchersManager.this._state = newState;
                switch (FourSepulchersManager.this._state.ordinal()) {
                    case 0:
                        FourSepulchersManager.this.onEntryEvent();
                        break;
                    case 1:
                        FourSepulchersManager.this.onAttackEvent();
                        break;
                    case 2:
                        FourSepulchersManager.this.onEndEvent();
                }
            }

            FourSepulchersManager.this.managersShout(currentMinute);
        }
    }
}