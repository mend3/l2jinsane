package mods.dungeon;

import enginemods.main.xmlfactory.XMLDocumentFactory;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DungeonManager {
    private static final Logger log = Logger.getLogger(DungeonManager.class.getName());

    private final Map<Integer, DungeonTemplate> templates = new ConcurrentHashMap<>();

    private final List<Dungeon> running = new CopyOnWriteArrayList<>();

    private final List<Integer> dungeonParticipants = new CopyOnWriteArrayList<>();
    private final Map<String, Long[]> dungeonPlayerData = new ConcurrentHashMap<>();
    private boolean reloading = false;

    protected DungeonManager() {
    }

    public static DungeonManager getInstance() {
        return SingletonHolder.instance;
    }

    public void updateDatabase() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement stm = con.prepareStatement("DELETE FROM dungeon");
                try {
                    PreparedStatement stm2 = con.prepareStatement("INSERT INTO dungeon VALUES (?,?,?)");
                    try {
                        stm.execute();
                        for (String ip : this.dungeonPlayerData.keySet()) {
                            for (int i = 1; i < this.dungeonPlayerData.get(ip).length; i++) {
                                stm2.setInt(1, i);
                                stm2.setString(2, ip);
                                stm2.setLong(3, ((Long[]) this.dungeonPlayerData.get(ip))[i]);
                                stm2.execute();
                            }
                        }
                        if (stm2 != null)
                            stm2.close();
                    } catch (Throwable throwable) {
                        if (stm2 != null)
                            try {
                                stm2.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (stm != null)
                        stm.close();
                } catch (Throwable throwable) {
                    if (stm != null)
                        try {
                            stm.close();
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
            e.printStackTrace();
        }
    }

    public boolean reload() {
        if (!this.running.isEmpty()) {
            this.reloading = true;
            return false;
        }
        this.templates.clear();
        this.running.clear();
        this.dungeonParticipants.clear();
        this.dungeonPlayerData.clear();
        load();
        return true;
    }

    public void load() {
        try {
            File f = new File("./data/xml/dungeons.xml");
            Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
            Node n = doc.getFirstChild();
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                if (d.getNodeName().equals("dungeon")) {
                    int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
                    String name = d.getAttributes().getNamedItem("name").getNodeValue();
                    int players = Integer.parseInt(d.getAttributes().getNamedItem("players").getNodeValue());
                    Map<Integer, Integer> rewards = new HashMap<>();
                    String rewardHtm = d.getAttributes().getNamedItem("rewardHtm").getNodeValue();
                    Map<Integer, DungeonStage> stages = new HashMap<>();
                    String rewards_data = d.getAttributes().getNamedItem("rewards").getNodeValue();
                    if (!rewards_data.isEmpty()) {
                        String[] rewards_data_split = rewards_data.split(";");
                        for (String reward : rewards_data_split) {
                            String[] reward_split = reward.split(",");
                            rewards.put(Integer.parseInt(reward_split[0]), Integer.parseInt(reward_split[1]));
                        }
                    }
                    for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                        NamedNodeMap attrs = cd.getAttributes();
                        if (cd.getNodeName().equals("stage")) {
                            int order = Integer.parseInt(attrs.getNamedItem("order").getNodeValue());
                            String loc_data = attrs.getNamedItem("loc").getNodeValue();
                            String[] loc_data_split = loc_data.split(",");
                            Location loc = new Location(Integer.parseInt(loc_data_split[0]), Integer.parseInt(loc_data_split[1]), Integer.parseInt(loc_data_split[2]));
                            boolean teleport = Boolean.parseBoolean(attrs.getNamedItem("teleport").getNodeValue());
                            int minutes = Integer.parseInt(attrs.getNamedItem("minutes").getNodeValue());
                            Map<Integer, List<Location>> mobs = new HashMap<>();
                            for (Node ccd = cd.getFirstChild(); ccd != null; ccd = ccd.getNextSibling()) {
                                NamedNodeMap attrs2 = ccd.getAttributes();
                                if (ccd.getNodeName().equals("mob")) {
                                    int npcId = Integer.parseInt(attrs2.getNamedItem("npcId").getNodeValue());
                                    List<Location> locs = new ArrayList<>();
                                    String locs_data = attrs2.getNamedItem("locs").getNodeValue();
                                    String[] locs_data_split = locs_data.split(";");
                                    for (String locc : locs_data_split) {
                                        String[] locc_data = locc.split(",");
                                        locs.add(new Location(Integer.parseInt(locc_data[0]), Integer.parseInt(locc_data[1]), Integer.parseInt(locc_data[2])));
                                    }
                                    mobs.put(npcId, locs);
                                }
                            }
                            stages.put(order, new DungeonStage(order, loc, teleport, minutes, mobs));
                        }
                    }
                    this.templates.put(id, new DungeonTemplate(id, name, players, rewards, rewardHtm, stages));
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "DungeonManager: Error loading dungeons.xml", e);
            e.printStackTrace();
        }
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement stm = con.prepareStatement("SELECT * FROM dungeon");
                try {
                    ResultSet rset = stm.executeQuery();
                    try {
                        while (rset.next()) {
                            int dungid = rset.getInt("dungid");
                            String ipaddr = rset.getString("ipaddr");
                            long lastjoin = rset.getLong("lastjoin");
                            if (!this.dungeonPlayerData.containsKey(ipaddr)) {
                                Long[] times = new Long[this.templates.size() + 1];
                                for (int i = 0; i < times.length; i++)
                                    times[i] = 0L;
                                times[dungid] = lastjoin;
                                this.dungeonPlayerData.put(ipaddr, times);
                                continue;
                            }
                            this.dungeonPlayerData.get(ipaddr)[dungid] = lastjoin;
                        }
                        if (rset != null)
                            rset.close();
                    } catch (Throwable throwable) {
                        if (rset != null)
                            try {
                                rset.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (stm != null)
                        stm.close();
                } catch (Throwable throwable) {
                    if (stm != null)
                        try {
                            stm.close();
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
            e.printStackTrace();
        }
        log.info("DungeonManager: Loaded " + this.templates.size() + " dungeon templates");
        ThreadPool.scheduleAtFixedRate(this::updateDatabase, 1800000L, 3600000L);
    }

    public synchronized void removeDungeon(Dungeon dungeon) {
        this.running.remove(dungeon);
        if (this.reloading && this.running.isEmpty()) {
            this.reloading = false;
            reload();
        }
    }

    public synchronized void enterDungeon(int id, Player player) {
        if (this.reloading) {
            player.sendMessage("The Dungeon system is reloading, please try again in a few minutes.");
            return;
        }
        DungeonTemplate template = this.templates.get(id);
        if (template.players() > 1 && (!player.isInParty() || player.getParty().getMembersCount() != template.players())) {
            player.sendMessage("You need a party of " + template.players() + " players to enter this Dungeon.");
            return;
        }
        if (template.players() == 1 && player.isInParty()) {
            player.sendMessage("You can only enter this Dungeon alone.");
            return;
        }
        List<Player> players = new ArrayList<>();
        if (player.isInParty()) {
            for (Player pm : player.getParty().getMembers()) {
                String pmip = pm.getHWID();
                if (this.dungeonPlayerData.containsKey(pmip) && System.currentTimeMillis() - ((Long[]) this.dungeonPlayerData.get(pmip))[template.id()] < 43200000L) {
                    player.sendMessage("One of your party members cannot join this Dungeon because 12 hours have not passed since they last joined.");
                    return;
                }
            }
            for (Player pm : player.getParty().getMembers()) {
                String pmip = pm.getHWID();
                this.dungeonParticipants.add(pm.getObjectId());
                players.add(pm);
                if (this.dungeonPlayerData.containsKey(pmip)) {
                    this.dungeonPlayerData.get(pmip)[template.id()] = System.currentTimeMillis();
                    continue;
                }
                Long[] times = new Long[this.templates.size() + 1];
                for (int i = 0; i < times.length; i++)
                    times[i] = 0L;
                times[template.id()] = System.currentTimeMillis();
                this.dungeonPlayerData.put(pmip, times);
            }
        } else {
            String pmip = player.getHWID();
            if (this.dungeonPlayerData.containsKey(pmip) && System.currentTimeMillis() - ((Long[]) this.dungeonPlayerData.get(pmip))[template.id()] < 43200000L) {
                player.sendMessage("12 hours have not passed since you last entered this Dungeon.");
                return;
            }
            this.dungeonParticipants.add(player.getObjectId());
            players.add(player);
            if (this.dungeonPlayerData.containsKey(pmip)) {
                this.dungeonPlayerData.get(pmip)[template.id()] = System.currentTimeMillis();
            } else {
                Long[] times = new Long[this.templates.size() + 1];
                for (int i = 0; i < times.length; i++)
                    times[i] = 0L;
                times[template.id()] = System.currentTimeMillis();
                this.dungeonPlayerData.put(pmip, times);
            }
        }
        Dungeon dungeon = new Dungeon(template, players);
        this.running.add(dungeon);
    }

    public boolean isReloading() {
        return this.reloading;
    }

    public boolean isInDungeon(Player player) {
        for (Dungeon dungeon : this.running) {
            for (Player p : dungeon.getPlayers()) {
                if (p == player)
                    return true;
            }
        }
        return false;
    }

    public int getDungeonsCount() {
        return this.templates.size();
    }

    public Map<String, Long[]> getPlayerData() {
        return this.dungeonPlayerData;
    }

    public List<Integer> getDungeonParticipants() {
        return this.dungeonParticipants;
    }

    private static class SingletonHolder {
        protected static final DungeonManager instance = new DungeonManager();
    }
}
