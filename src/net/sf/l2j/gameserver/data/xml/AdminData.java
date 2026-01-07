/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.AccessLevel;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class AdminData implements IXmlReader {
    private final TreeMap<Integer, AccessLevel> _accessLevels = new TreeMap();
    private final Map<String, Integer> _adminCommandAccessRights = new HashMap();
    private final Map<Player, Boolean> _gmList = new ConcurrentHashMap();

    private AdminData() {
    }

    public static AdminData getInstance() {
        return AdminData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/accessLevels.xml");
        LOGGER.info("Loaded {} access levels.", this._accessLevels.size());
        this.parseFile("./data/xml/adminCommands.xml");
        LOGGER.info("Loaded {} admin command rights.", this._adminCommandAccessRights.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "access", (accessNode) -> {
                StatSet set = this.parseAttributes(accessNode);
                this._accessLevels.put(set.getInteger("level"), new AccessLevel(set));
            });
            this.forEach(listNode, "aCar", (aCarNode) -> {
                StatSet set = this.parseAttributes(aCarNode);
                this._adminCommandAccessRights.put(set.getString("name"), set.getInteger("accessLevel"));
            });
        });
    }

    public void reload() {
        this._accessLevels.clear();
        this._adminCommandAccessRights.clear();
        this.load();
    }

    public AccessLevel getAccessLevel(int level) {
        return this._accessLevels.get(level < 0 ? -1 : level);
    }

    public int getMasterAccessLevel() {
        return this._accessLevels.isEmpty() ? 0 : this._accessLevels.lastKey();
    }

    public boolean hasAccessLevel(int level) {
        return this._accessLevels.containsKey(level);
    }

    public boolean hasAccess(String command, AccessLevel accessToCheck) {
        Integer level = this._adminCommandAccessRights.get(command);
        if (level == null) {
            LOGGER.warn("No rights defined for admin command '{}'.", command);
            return false;
        } else {
            AccessLevel access = getInstance().getAccessLevel(level);
            return access != null && (access.getLevel() == accessToCheck.getLevel() || accessToCheck.hasChildAccess(access));
        }
    }

    public List<Player> getAllGms(boolean includeHidden) {
        List<Player> list = new ArrayList();
        Iterator var3 = this._gmList.entrySet().iterator();

        while (true) {
            Entry entry;
            do {
                if (!var3.hasNext()) {
                    return list;
                }

                entry = (Entry) var3.next();
            } while (!includeHidden && (Boolean) entry.getValue());

            list.add((Player) entry.getKey());
        }
    }

    public List<String> getAllGmNames(boolean includeHidden) {
        List<String> list = new ArrayList();
        Iterator var3 = this._gmList.entrySet().iterator();

        while (var3.hasNext()) {
            Entry<Player, Boolean> entry = (Entry) var3.next();
            if (!(Boolean) entry.getValue()) {
                list.add(entry.getKey().getName());
            } else if (includeHidden) {
                list.add(entry.getKey().getName() + " (invis)");
            }
        }

        return list;
    }

    public void addGm(Player player, boolean hidden) {
        this._gmList.put(player, hidden);
    }

    public void deleteGm(Player player) {
        this._gmList.remove(player);
    }

    public boolean showOrHideGm(Player player) {
        return this._gmList.computeIfPresent(player, (k, v) -> {
            return !v;
        });
    }

    public boolean isGmOnline(boolean includeHidden) {
        Iterator var2 = this._gmList.entrySet().iterator();

        Entry entry;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            entry = (Entry) var2.next();
        } while (!includeHidden && (Boolean) entry.getValue());

        return true;
    }

    public boolean isRegisteredAsGM(Player player) {
        return this._gmList.containsKey(player);
    }

    public void sendListToPlayer(Player player) {
        if (this.isGmOnline(player.isGM())) {
            player.sendPacket(SystemMessageId.GM_LIST);
            Iterator var2 = this.getAllGmNames(player.isGM()).iterator();

            while (var2.hasNext()) {
                String name = (String) var2.next();
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GM_S1).addString(name));
            }
        } else {
            player.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
            player.sendPacket(new PlaySound("systemmsg_e.702"));
        }

    }

    public void broadcastToGMs(L2GameServerPacket packet) {
        Iterator var2 = this.getAllGms(true).iterator();

        while (var2.hasNext()) {
            Player gm = (Player) var2.next();
            gm.sendPacket(packet);
        }

    }

    public void broadcastMessageToGMs(String message) {
        Iterator var2 = this.getAllGms(true).iterator();

        while (var2.hasNext()) {
            Player gm = (Player) var2.next();
            gm.sendMessage(message);
        }

    }

    private static class SingletonHolder {
        protected static final AdminData INSTANCE = new AdminData();
    }
}