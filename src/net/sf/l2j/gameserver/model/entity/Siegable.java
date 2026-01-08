package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.pledge.Clan;

import java.util.Calendar;
import java.util.List;

public interface Siegable {
    void startSiege();

    void endSiege();

    List<Clan> getAttackerClans();

    List<Clan> getDefenderClans();

    boolean checkSide(Clan var1, SiegeSide var2);

    boolean checkSides(Clan var1, SiegeSide... var2);

    boolean checkSides(Clan var1);

    Npc getFlag(Clan var1);

    Calendar getSiegeDate();
}
