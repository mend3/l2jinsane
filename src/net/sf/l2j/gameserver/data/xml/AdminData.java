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
    private final TreeMap<Integer, AccessLevel> _accessLevels = new TreeMap<>();
    private final Map<String, Integer> _adminCommandAccessRights = new HashMap<>();
    private final Map<Player, Boolean> _gmList = new ConcurrentHashMap<>();

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
        List<Player> list = new ArrayList<>();
        Iterator<Entry<Player, Boolean>> var3 = this._gmList.entrySet().iterator();

        while (true) {
            Entry<Player, Boolean> entry;
            do {
                if (!var3.hasNext()) {
                    return list;
                }

                entry = var3.next();
            } while (!includeHidden && entry.getValue());

            list.add(entry.getKey());
        }
    }

    public List<String> getAllGmNames(boolean includeHidden) {
        List<String> list = new ArrayList<>();

        for (Entry<Player, Boolean> entry : this._gmList.entrySet()) {
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
        return Boolean.TRUE.equals(this._gmList.computeIfPresent(player, (k, v) -> !v));
    }

    public boolean isGmOnline(boolean includeHidden) {
        Iterator<Entry<Player, Boolean>> var2 = this._gmList.entrySet().iterator();

        Entry<Player, Boolean> entry;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            entry = var2.next();
        } while (!includeHidden && entry.getValue());

        return true;
    }

    public boolean isRegisteredAsGM(Player player) {
        return this._gmList.containsKey(player);
    }

    public void sendListToPlayer(Player player) {
        if (this.isGmOnline(player.isGM())) {
            player.sendPacket(SystemMessageId.GM_LIST);

            for (String name : this.getAllGmNames(player.isGM())) {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GM_S1).addString(name));
            }
        } else {
            player.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
            player.sendPacket(new PlaySound("systemmsg_e.702"));
        }

    }

    public void broadcastToGMs(L2GameServerPacket packet) {
        for (Player gm : this.getAllGms(true)) {
            gm.sendPacket(packet);
        }
    }

    public void broadcastMessageToGMs(String message) {
        for (Player gm : this.getAllGms(true)) {
            gm.sendMessage(message);
        }
    }

    private static class SingletonHolder {
        protected static final AdminData INSTANCE = new AdminData();
    }
}