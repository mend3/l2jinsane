package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.clanhall.ClanHallFunction;

public class ClanHallDecoration extends L2GameServerPacket {
    private final ClanHall _clanHall;

    private ClanHallFunction _function;

    public ClanHallDecoration(ClanHall clanHall) {
        this._clanHall = clanHall;
    }

    protected final void writeImpl() {
        writeC(247);
        writeD(this._clanHall.getId());
        this._function = this._clanHall.getFunction(3);
        if (this._function == null || this._function.getLvl() == 0) {
            writeC(0);
        } else if ((this._clanHall.getGrade() == 0 && this._function.getLvl() < 220) || (this._clanHall.getGrade() == 1 && this._function.getLvl() < 160) || (this._clanHall.getGrade() == 2 && this._function.getLvl() < 260) || (this._clanHall.getGrade() == 3 && this._function.getLvl() < 300)) {
            writeC(1);
        } else {
            writeC(2);
        }
        this._function = this._clanHall.getFunction(4);
        if (this._function == null || this._function.getLvl() == 0) {
            writeC(0);
            writeC(0);
        } else if (((this._clanHall.getGrade() == 0 || this._clanHall.getGrade() == 1) && this._function.getLvl() < 25) || (this._clanHall.getGrade() == 2 && this._function.getLvl() < 30) || (this._clanHall.getGrade() == 3 && this._function.getLvl() < 40)) {
            writeC(1);
            writeC(1);
        } else {
            writeC(2);
            writeC(2);
        }
        this._function = this._clanHall.getFunction(5);
        if (this._function == null || this._function.getLvl() == 0) {
            writeC(0);
        } else if ((this._clanHall.getGrade() == 0 && this._function.getLvl() < 25) || (this._clanHall.getGrade() == 1 && this._function.getLvl() < 30) || (this._clanHall.getGrade() == 2 && this._function.getLvl() < 40) || (this._clanHall.getGrade() == 3 && this._function.getLvl() < 50)) {
            writeC(1);
        } else {
            writeC(2);
        }
        this._function = this._clanHall.getFunction(1);
        if (this._function == null || this._function.getLvl() == 0) {
            writeC(0);
        } else if (this._function.getLvl() < 2) {
            writeC(1);
        } else {
            writeC(2);
        }
        writeC(0);
        this._function = this._clanHall.getFunction(8);
        if (this._function == null || this._function.getLvl() == 0) {
            writeC(0);
        } else if (this._function.getLvl() <= 1) {
            writeC(1);
        } else {
            writeC(2);
        }
        this._function = this._clanHall.getFunction(2);
        if (this._function == null || this._function.getLvl() == 0) {
            writeC(0);
        } else if ((this._clanHall.getGrade() == 0 && this._function.getLvl() < 2) || this._function.getLvl() < 3) {
            writeC(1);
        } else {
            writeC(2);
        }
        this._function = this._clanHall.getFunction(6);
        if (this._function == null || this._function.getLvl() == 0) {
            writeC(0);
            writeC(0);
        } else if ((this._clanHall.getGrade() == 0 && this._function.getLvl() < 2) || (this._clanHall.getGrade() == 1 && this._function.getLvl() < 4) || (this._clanHall.getGrade() == 2 && this._function.getLvl() < 5) || (this._clanHall.getGrade() == 3 && this._function.getLvl() < 8)) {
            writeC(1);
            writeC(1);
        } else {
            writeC(2);
            writeC(2);
        }
        this._function = this._clanHall.getFunction(7);
        if (this._function == null || this._function.getLvl() == 0) {
            writeC(0);
        } else if (this._function.getLvl() <= 1) {
            writeC(1);
        } else {
            writeC(2);
        }
        this._function = this._clanHall.getFunction(2);
        if (this._function == null || this._function.getLvl() == 0) {
            writeC(0);
        } else if ((this._clanHall.getGrade() == 0 && this._function.getLvl() < 2) || this._function.getLvl() < 3) {
            writeC(1);
        } else {
            writeC(2);
        }
        writeD(0);
        writeD(0);
    }
}
