package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.HeroManager;

import java.util.Collection;

public class ExHeroList extends L2GameServerPacket {
    private final Collection<StatSet> _heroList = HeroManager.getInstance().getHeroes().values();

    protected void writeImpl() {
        writeC(254);
        writeH(35);
        writeD(this._heroList.size());
        for (StatSet hero : this._heroList) {
            writeS(hero.getString("char_name"));
            writeD(hero.getInteger("class_id"));
            writeS(hero.getString("clan_name", ""));
            writeD(hero.getInteger("clan_crest", 0));
            writeS(hero.getString("ally_name", ""));
            writeD(hero.getInteger("ally_crest", 0));
            writeD(hero.getInteger("count"));
        }
    }
}
