package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;

public final class NpcHtmlMessage extends L2GameServerPacket {
    private final int _npcObjId;

    private String _html;

    private int _itemId = 0;

    private boolean _validate = true;

    public NpcHtmlMessage(int npcObjId) {
        this._npcObjId = npcObjId;
    }

    public void runImpl() {
        if (!this._validate)
            return;
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        activeChar.clearBypass();
        for (int i = 0; i < this._html.length(); i++) {
            int start = this._html.indexOf("\"bypass ", i);
            int finish = this._html.indexOf("\"", start + 1);
            if (start < 0 || finish < 0)
                break;
            if (this._html.startsWith("-h", start + 8)) {
                start += 11;
            } else {
                start += 8;
            }
            i = finish;
            int finish2 = this._html.indexOf("$", start);
            if (finish2 < finish && finish2 > 0) {
                activeChar.addBypass2(this._html.substring(start, finish2).trim());
            } else {
                activeChar.addBypass(this._html.substring(start, finish).trim());
            }
        }
    }

    protected void writeImpl() {
        writeC(15);
        writeD(this._npcObjId);
        writeS(this._html);
        writeD(this._itemId);
    }

    public void disableValidation() {
        this._validate = false;
    }

    public void setItemId(int itemId) {
        this._itemId = itemId;
    }

    public void setFile(String filename) {
        setHtml(HtmCache.getInstance().getHtmForce(filename));
    }

    public void basicReplace(String pattern, String value) {
        this._html = this._html.replaceAll(pattern, value);
    }

    public void replace(String pattern, String value) {
        this._html = this._html.replaceAll(pattern, value.replaceAll("\\$", "\\\\\\$"));
    }

    public void replace(String pattern, int value) {
        this._html = this._html.replaceAll(pattern, Integer.toString(value));
    }

    public void replace(String pattern, long value) {
        this._html = this._html.replaceAll(pattern, Long.toString(value));
    }

    public void replace(String pattern, double value) {
        this._html = this._html.replaceAll(pattern, Double.toString(value));
    }

    public String getHtml() {
        return this._html;
    }

    public void setHtml(String text) {
        if (text.length() > 8192) {
            this._html = "<html><body>Html was too long.</body></html>";
            LOGGER.warn("An html content was too long.");
            return;
        }
        this._html = text;
    }
}
