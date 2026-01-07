package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q092_SagaOfTheElementalMaster extends SagasSuperClass {
    public Q092_SagaOfTheElementalMaster() {
        super(92, "Saga of the Elemental Master");
        this.NPC = new int[]{
                30174, 31281, 31614, 31614, 31629, 31646, 31648, 31652, 31654, 31655,
                31659, 31614};
        this.Items = new int[]{
                7080, 7605, 7081, 7507, 7290, 7321, 7352, 7383, 7414, 7445,
                7111, 0};
        this.Mob = new int[]{27314, 27241, 27311};
        this.classid = 104;
        this.prevclass = 28;
        this.X = new int[]{161719, 124376, 124355};
        this.Y = new int[]{-92823, 82127, 82155};
        this.Z = new int[]{-1893, -2796, -2803};
        registerNPCs();
    }
}
