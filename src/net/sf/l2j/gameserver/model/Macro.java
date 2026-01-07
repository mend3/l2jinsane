package net.sf.l2j.gameserver.model;

public class Macro {
    public static final int CMD_TYPE_SKILL = 1;

    public static final int CMD_TYPE_ACTION = 3;

    public static final int CMD_TYPE_SHORTCUT = 4;
    public final int icon;
    public final String name;
    public final String descr;
    public final String acronym;
    public final MacroCmd[] commands;
    public int id;

    public Macro(int pId, int pIcon, String pName, String pDescr, String pAcronym, MacroCmd[] pCommands) {
        this.id = pId;
        this.icon = pIcon;
        this.name = pName;
        this.descr = pDescr;
        this.acronym = pAcronym;
        this.commands = pCommands;
    }

    public String toString() {
        return "macro id=" + this.id + ", icon=" + this.icon + ", name=" + this.name + ", descr=" + this.descr + ", acronym=" + this.acronym;
    }

    public record MacroCmd(int entry, int type, int d1, int d2, String cmd) {
    }
}
