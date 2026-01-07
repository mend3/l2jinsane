package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q080_SagaOfTheWindRider extends SagasSuperClass {
    public Q080_SagaOfTheWindRider() {
        super(80, "Saga of the Wind Rider");
        this.NPC = new int[]{
                31603, 31624, 31284, 31615, 31612, 31646, 31648, 31652, 31654, 31655,
                31659, 31616};
        this.Items = new int[]{
                7080, 7517, 7081, 7495, 7278, 7309, 7340, 7371, 7402, 7433,
                7103, 0};
        this.Mob = new int[]{27300, 27229, 27303};
        this.classid = 101;
        this.prevclass = 23;
        this.X = new int[]{161719, 124314, 124355};
        this.Y = new int[]{-92823, 82155, 82155};
        this.Z = new int[]{-1893, -2803, -2803};
        registerNPCs();
    }
}
