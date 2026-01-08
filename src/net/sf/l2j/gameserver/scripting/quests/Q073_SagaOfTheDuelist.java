package net.sf.l2j.gameserver.scripting.quests;

public class Q073_SagaOfTheDuelist extends SagasSuperClass {
    public Q073_SagaOfTheDuelist() {
        super(73, "Saga of the Duelist");
        this.NPC = new int[]{
                30849, 31624, 31226, 31331, 31639, 31646, 31647, 31653, 31654, 31655,
                31656, 31277};
        this.Items = new int[]{
                7080, 7537, 7081, 7488, 7271, 7302, 7333, 7364, 7395, 7426,
                7096, 7546};
        this.Mob = new int[]{27289, 27222, 27281};
        this.classid = 88;
        this.prevclass = 2;
        this.X = new int[]{164650, 47429, 47391};
        this.Y = new int[]{-74121, -56923, -56929};
        this.Z = new int[]{-2871, -2383, -2370};
        registerNPCs();
    }
}
