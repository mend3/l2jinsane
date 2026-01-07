package net.sf.l2j.gameserver.model.holder;

public final class BuffSkillHolder extends IntIntHolder {
    private final String _type;

    private final String _description;

    public BuffSkillHolder(int id, int price, String type, String description) {
        super(id, price);
        this._type = type;
        this._description = description;
    }

    public String getType() {
        return this._type;
    }

    public String getDescription() {
        return this._description;
    }
}
