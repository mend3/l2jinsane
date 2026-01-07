/**/
package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.rift.DimensionalRift;
import net.sf.l2j.gameserver.model.rift.DimensionalRiftRoom;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DimensionalRiftManager implements IXmlReader {
    private static final int DIMENSIONAL_FRAGMENT = 7079;
    private final Map<Byte, HashMap<Byte, DimensionalRiftRoom>> _rooms = new HashMap(7);

    protected DimensionalRiftManager() {
    }

    private static int getNeededItems(byte type) {
        switch (type) {
            case 1:
                return Config.RIFT_ENTER_COST_RECRUIT;
            case 2:
                return Config.RIFT_ENTER_COST_SOLDIER;
            case 3:
                return Config.RIFT_ENTER_COST_OFFICER;
            case 4:
                return Config.RIFT_ENTER_COST_CAPTAIN;
            case 5:
                return Config.RIFT_ENTER_COST_COMMANDER;
            case 6:
                return Config.RIFT_ENTER_COST_HERO;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    public static DimensionalRiftManager getInstance() {
        return DimensionalRiftManager.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/dimensionalRift.xml");
        LOGGER.info("Loaded Dimensional Rift rooms.");
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "area", (areaNode) -> {
                NamedNodeMap areaAttrs = areaNode.getAttributes();
                byte type = Byte.parseByte(areaAttrs.getNamedItem("type").getNodeValue());
                if (!this._rooms.containsKey(type)) {
                    this._rooms.put(type, new HashMap(9));
                }

                this.forEach(areaNode, "room", (roomNode) -> {
                    DimensionalRiftRoom riftRoom = new DimensionalRiftRoom(type, this.parseAttributes(roomNode));
                    ((HashMap) this._rooms.get(type)).put(riftRoom.getId(), riftRoom);
                    this.forEach(roomNode, "spawn", (spawnNode) -> {
                        NamedNodeMap spawnAttrs = spawnNode.getAttributes();
                        int mobId = Integer.parseInt(spawnAttrs.getNamedItem("mobId").getNodeValue());
                        int delay = Integer.parseInt(spawnAttrs.getNamedItem("delay").getNodeValue());
                        int count = Integer.parseInt(spawnAttrs.getNamedItem("count").getNodeValue());
                        NpcTemplate template = NpcData.getInstance().getTemplate(mobId);
                        if (template == null) {
                            LOGGER.warn("Template " + mobId + " not found!");
                        } else {
                            try {
                                for (int i = 0; i < count; ++i) {
                                    L2Spawn spawnDat = new L2Spawn(template);
                                    spawnDat.setLoc(riftRoom.getRandomX(), riftRoom.getRandomY(), -6752, -1);
                                    spawnDat.setRespawnDelay(delay);
                                    SpawnTable.getInstance().addSpawn(spawnDat, false);
                                    riftRoom.getSpawns().add(spawnDat);
                                }
                            } catch (Exception var9) {
                                LOGGER.error("Failed to initialize a spawn.", var9);
                            }

                        }
                    });
                });
            });
        });
    }

    public void reload() {
        Iterator var1 = this._rooms.values().iterator();

        while (var1.hasNext()) {
            Map<Byte, DimensionalRiftRoom> area = (Map) var1.next();
            Iterator var3 = area.values().iterator();

            while (var3.hasNext()) {
                DimensionalRiftRoom room = (DimensionalRiftRoom) var3.next();
                room.getSpawns().clear();
            }

            area.clear();
        }

        this._rooms.clear();
        this.load();
    }

    public DimensionalRiftRoom getRoom(byte type, byte room) {
        Map<Byte, DimensionalRiftRoom> area = this._rooms.get(type);
        return area == null ? null : area.get(room);
    }

    public boolean checkIfInRiftZone(int x, int y, int z, boolean ignorePeaceZone) {
        Map<Byte, DimensionalRiftRoom> area = this._rooms.get((byte) 0);
        if (area == null) {
            return false;
        } else if (ignorePeaceZone) {
            return area.get((byte) 1).checkIfInZone(x, y, z);
        } else {
            return area.get((byte) 1).checkIfInZone(x, y, z) && !area.get((byte) 0).checkIfInZone(x, y, z);
        }
    }

    public boolean checkIfInPeaceZone(int x, int y, int z) {
        DimensionalRiftRoom room = this.getRoom((byte) 0, (byte) 0);
        return room != null && room.checkIfInZone(x, y, z);
    }

    public void teleportToWaitingRoom(Player player) {
        DimensionalRiftRoom room = this.getRoom((byte) 0, (byte) 0);
        if (room != null) {
            player.teleToLocation(room.getTeleportLoc());
        }
    }

    public synchronized void start(Player player, byte type, Npc npc) {
        Party party = player.getParty();
        if (party == null) {
            this.showHtmlFile(player, "data/html/seven_signs/rift/NoParty.htm", npc);
        } else if (!party.isLeader(player)) {
            this.showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
        } else if (!party.isInDimensionalRift()) {
            if (party.getMembersCount() < Config.RIFT_MIN_PARTY_SIZE) {
                NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
                html.setFile("data/html/seven_signs/rift/SmallParty.htm");
                html.replace("%npc_name%", npc.getName());
                html.replace("%count%", Integer.toString(Config.RIFT_MIN_PARTY_SIZE));
                player.sendPacket(html);
            } else {
                List<DimensionalRiftRoom> availableRooms = this.getFreeRooms(type, false);
                if (availableRooms.isEmpty()) {
                    NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
                    html.setFile("data/html/seven_signs/rift/Full.htm");
                    html.replace("%npc_name%", npc.getName());
                    player.sendPacket(html);
                } else {
                    Iterator var6 = party.getMembers().iterator();

                    while (var6.hasNext()) {
                        Player member = (Player) var6.next();
                        if (!this.checkIfInPeaceZone(member.getX(), member.getY(), member.getZ())) {
                            this.showHtmlFile(player, "data/html/seven_signs/rift/NotInWaitingRoom.htm", npc);
                            return;
                        }
                    }

                    int count = getNeededItems(type);
                    Iterator var14 = party.getMembers().iterator();

                    ItemInstance item;
                    do {
                        Player member;
                        if (!var14.hasNext()) {
                            var14 = party.getMembers().iterator();

                            while (var14.hasNext()) {
                                member = (Player) var14.next();
                                member.destroyItemByItemId("RiftEntrance", 7079, count, null, true);
                            }

                            new DimensionalRift(party, Rnd.get(availableRooms));
                            return;
                        }

                        member = (Player) var14.next();
                        item = member.getInventory().getItemByItemId(7079);
                    } while (item != null && item.getCount() >= getNeededItems(type));

                    NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
                    html.setFile("data/html/seven_signs/rift/NoFragments.htm");
                    html.replace("%npc_name%", npc.getName());
                    html.replace("%count%", Integer.toString(count));
                    player.sendPacket(html);
                }
            }
        }
    }

    public void showHtmlFile(Player player, String file, Npc npc) {
        NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
        html.setFile(file);
        html.replace("%npc_name%", npc.getName());
        player.sendPacket(html);
    }

    public List<DimensionalRiftRoom> getFreeRooms(byte type, boolean canUseBossRoom) {
        return (this._rooms.get(type)).values().stream().filter((r) -> {
            return !r.isPartyInside() && (canUseBossRoom || !r.isBossRoom());
        }).collect(Collectors.toList());
    }

    public void onPartyEdit(Party party) {
        if (party != null) {
            DimensionalRift rift = party.getDimensionalRift();
            if (rift != null) {
                Iterator var3 = party.getMembers().iterator();

                while (var3.hasNext()) {
                    Player member = (Player) var3.next();
                    this.teleportToWaitingRoom(member);
                }

                rift.killRift();
            }

        }
    }

    private static class SingletonHolder {
        protected static final DimensionalRiftManager INSTANCE = new DimensionalRiftManager();
    }
}