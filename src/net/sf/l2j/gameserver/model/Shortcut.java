package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.enums.ShortcutType;

public class Shortcut {
    private final int _slot;

    private final int _page;

    private final int _id;

    private final ShortcutType _type;

    private final int _characterType;

    private int _level;

    private int _sharedReuseGroup = -1;

    public Shortcut(int slotId, int pageId, ShortcutType type, int id, int level, int characterType) {
        this._slot = slotId;
        this._page = pageId;
        this._type = type;
        this._id = id;
        this._level = level;
        this._characterType = characterType;
    }

    public int getId() {
        return this._id;
    }

    public int getLevel() {
        return this._level;
    }

    public void setLevel(int level) {
        this._level = level;
    }

    public int getPage() {
        return this._page;
    }

    public int getSlot() {
        return this._slot;
    }

    public ShortcutType getType() {
        return this._type;
    }

    public int getCharacterType() {
        return this._characterType;
    }

    public int getSharedReuseGroup() {
        return this._sharedReuseGroup;
    }

    public void setSharedReuseGroup(int group) {
        this._sharedReuseGroup = group;
    }
}
