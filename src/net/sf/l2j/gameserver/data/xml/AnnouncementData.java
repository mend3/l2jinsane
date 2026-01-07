/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.Announcement;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class AnnouncementData implements IXmlReader {
    private static final String HEADER = "<?xml version='1.0' encoding='utf-8'?> \n<!-- \n@param String message - the message to be announced \n@param Boolean critical - type of announcement (true = critical,false = normal) \n@param Boolean auto - when the announcement will be displayed (true = auto,false = on player login) \n@param Integer initial_delay - time delay for the first announce (used only if auto=true;value in seconds) \n@param Integer delay - time delay for the announces following the first announce (used only if auto=true;value in seconds) \n@param Integer limit - limit of announces (used only if auto=true, 0 = unlimited) \n--> \n";
    private final Map<Integer, Announcement> _announcements = new ConcurrentHashMap<>();

    protected AnnouncementData() {
    }

    public static AnnouncementData getInstance() {
        return AnnouncementData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/announcements.xml");
        LOGGER.info("Loaded {} announcements.", this._announcements.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "announcement", (announcementNode) -> {
                NamedNodeMap attrs = announcementNode.getAttributes();
                String message = this.parseString(attrs, "message");
                if (message != null && !message.isEmpty()) {
                    boolean critical = this.parseBoolean(attrs, "critical", false);
                    boolean auto = this.parseBoolean(attrs, "auto", false);
                    if (auto) {
                        int initialDelay = this.parseInteger(attrs, "initial_delay");
                        int delay = this.parseInteger(attrs, "delay");
                        int limit = Math.max(this.parseInteger(attrs, "limit"), 0);
                        this._announcements.put(this._announcements.size(), new Announcement(message, critical, auto, initialDelay, delay, limit));
                    } else {
                        this._announcements.put(this._announcements.size(), new Announcement(message, critical));
                    }

                } else {
                    LOGGER.warn("The message is empty on an announcement. Ignoring it.");
                }
            });
        });
    }

    public void reload() {

        for (Announcement announce : this._announcements.values()) {
            announce.stopTask();
        }

        this.load();
    }

    public void showAnnouncements(Player player, boolean autoOrNot) {

        for (Announcement announce : this._announcements.values()) {
            if (autoOrNot) {
                announce.reloadTask();
            } else if (!announce.isAuto()) {
                player.sendPacket(new CreatureSay(0, announce.isCritical() ? 18 : 10, player.getName(), announce.getMessage()));
            }
        }

    }

    public void handleAnnounce(String command, int lengthToTrim, boolean critical) {
        try {
            World.announceToOnlinePlayers(command.substring(lengthToTrim), critical);
        } catch (StringIndexOutOfBoundsException ignored) {
        }

    }

    public void listAnnouncements(Player player) {
        StringBuilder sb = new StringBuilder("<br>");
        if (this._announcements.isEmpty()) {
            sb.append("<tr><td>The XML file doesn't contain any content.</td></tr>");
        } else {

            for (Entry<Integer, Announcement> entry : this._announcements.entrySet()) {
                int index = entry.getKey();
                Announcement announce = entry.getValue();
                StringUtil.append(sb, "<tr><td width=240>#", index, " - ", announce.getMessage(), "</td><td></td></tr><tr><td>Critical: ", announce.isCritical(), " | Auto: ", announce.isAuto(), "</td><td><button value=\"Delete\" action=\"bypass -h admin_announce del ", index, "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr>");
            }
        }

        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setHtml(HtmCache.getInstance().getHtmForce("data/html/admin/announce_list.htm"));
        html.replace("%announces%", sb.toString());
        player.sendPacket(html);
    }

    public boolean addAnnouncement(String message, boolean critical, boolean auto, int initialDelay, int delay, int limit) {
        if (message != null && !message.isEmpty()) {
            if (auto) {
                this._announcements.put(this._announcements.size(), new Announcement(message, critical, auto, initialDelay, delay, limit));
            } else {
                this._announcements.put(this._announcements.size(), new Announcement(message, critical));
            }

            this.regenerateXML();
            return true;
        } else {
            return false;
        }
    }

    public void delAnnouncement(int index) {
        this._announcements.remove(index).stopTask();
        this.regenerateXML();
    }

    private void regenerateXML() {
        StringBuilder sb = new StringBuilder("<?xml version='1.0' encoding='utf-8'?> \n<!-- \n@param String message - the message to be announced \n@param Boolean critical - type of announcement (true = critical,false = normal) \n@param Boolean auto - when the announcement will be displayed (true = auto,false = on player login) \n@param Integer initial_delay - time delay for the first announce (used only if auto=true;value in seconds) \n@param Integer delay - time delay for the announces following the first announce (used only if auto=true;value in seconds) \n@param Integer limit - limit of announces (used only if auto=true, 0 = unlimited) \n--> \n");
        sb.append("<list> \n");

        for (Announcement announce : this._announcements.values()) {
            StringUtil.append(sb, "<announcement message=\"", announce.getMessage(), "\" critical=\"", announce.isCritical(), "\" auto=\"", announce.isAuto(), "\" initial_delay=\"", announce.getInitialDelay(), "\" delay=\"", announce.getDelay(), "\" limit=\"", announce.getLimit(), "\" /> \n");
        }

        sb.append("</list>");

        try {
            FileWriter fw = new FileWriter("./data/xml/announcements.xml");

            try {
                fw.write(sb.toString());
            } catch (Throwable var6) {
                try {
                    fw.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }

                throw var6;
            }

            fw.close();
        } catch (Exception var7) {
            LOGGER.error("Error regenerating XML.", var7);
        }

    }

    private static class SingletonHolder {
        protected static final AnnouncementData INSTANCE = new AnnouncementData();
    }
}