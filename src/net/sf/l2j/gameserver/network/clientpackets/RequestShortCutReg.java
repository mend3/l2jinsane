package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.ShortcutType;
import net.sf.l2j.gameserver.model.Shortcut;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutRegister;

public final class RequestShortCutReg extends L2GameClientPacket {
    private int _type;

    private int _id;

    private int _slot;

    private int _page;

    private int _characterType;

    protected void readImpl() {
        this._type = readD();
        int slot = readD();
        this._id = readD();
        this._characterType = readD();
        this._slot = slot % 12;
        this._page = slot / 12;
    }

    protected void runImpl() {
        Shortcut shortcut;
        int level;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (this._page < 0 || this._page > 10)
            return;
        if (this._type < 1 || this._type > ShortcutType.VALUES.length)
            return;
        ShortcutType type = ShortcutType.VALUES[this._type];
        switch (type) {
            case ITEM:
            case ACTION:
            case MACRO:
            case RECIPE:
                shortcut = new Shortcut(this._slot, this._page, type, this._id, -1, this._characterType);
                sendPacket(new ShortCutRegister(shortcut));
                player.getShortcutList().addShortcut(shortcut);
                break;
            case SKILL:
                level = player.getSkillLevel(this._id);
                if (level > 0) {
                    shortcut = new Shortcut(this._slot, this._page, type, this._id, level, this._characterType);
                    sendPacket(new ShortCutRegister(shortcut));
                    player.getShortcutList().addShortcut(shortcut);
                }
                break;
        }
    }
}
