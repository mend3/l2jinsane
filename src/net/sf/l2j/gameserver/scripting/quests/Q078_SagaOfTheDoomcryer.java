package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q078_SagaOfTheDoomcryer extends SagasSuperClass {
    public Q078_SagaOfTheDoomcryer() {
        super(78, "Saga of the Doomcryer");
        this.NPC = new int[]{
                31336, 31624, 31589, 31290, 31642, 31646, 31649, 31650, 31654, 31655,
                31657, 31290};
        this.Items = new int[]{
                7080, 7539, 7081, 7493, 7276, 7307, 7338, 7369, 7400, 7431,
                7101, 0};
        this.Mob = new int[]{27295, 27227, 27285};
        this.classid = 116;
        this.prevclass = 52;
        this.X = new int[]{191046, 46087, 46066};
        this.Y = new int[]{-40640, -36372, -36396};
        this.Z = new int[]{-3042, -1685, -1685};
        registerNPCs();
    }
}
