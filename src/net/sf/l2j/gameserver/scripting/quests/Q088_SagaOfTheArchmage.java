package net.sf.l2j.gameserver.scripting.quests;

public class Q088_SagaOfTheArchmage extends SagasSuperClass {
    public Q088_SagaOfTheArchmage() {
        super(88, "Saga of the Archmage");
        this.NPC = new int[]{
                30176, 31627, 31282, 31282, 31590, 31646, 31647, 31650, 31654, 31655,
                31657, 31282};
        this.Items = new int[]{
                7080, 7529, 7081, 7503, 7286, 7317, 7348, 7379, 7410, 7441,
                7082, 0};
        this.Mob = new int[]{27250, 27237, 27254};
        this.classid = 94;
        this.prevclass = 12;
        this.X = new int[]{191046, 46066, 46087};
        this.Y = new int[]{-40640, -36396, -36372};
        this.Z = new int[]{-3042, -1685, -1685};
        registerNPCs();
    }
}
