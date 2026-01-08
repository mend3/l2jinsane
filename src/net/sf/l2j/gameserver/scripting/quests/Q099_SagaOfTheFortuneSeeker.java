package net.sf.l2j.gameserver.scripting.quests;

public class Q099_SagaOfTheFortuneSeeker extends SagasSuperClass {
    public Q099_SagaOfTheFortuneSeeker() {
        super(99, "Saga of the Fortune Seeker");
        this.NPC = new int[]{
                31594, 31623, 31600, 31600, 31601, 31646, 31649, 31650, 31654, 31655,
                31657, 31600};
        this.Items = new int[]{
                7080, 7608, 7081, 7514, 7297, 7328, 7359, 7390, 7421, 7452,
                7109, 0};
        this.Mob = new int[]{27259, 27248, 27309};
        this.classid = 117;
        this.prevclass = 55;
        this.X = new int[]{191046, 46066, 46087};
        this.Y = new int[]{-40640, -36396, -36372};
        this.Z = new int[]{-3042, -1685, -1685};
        registerNPCs();
    }
}
