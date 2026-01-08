package net.sf.l2j.gameserver.scripting.quests;

public class Q082_SagaOfTheSagittarius extends SagasSuperClass {
    public Q082_SagaOfTheSagittarius() {
        super(82, "Saga of the Sagittarius");
        this.NPC = new int[]{
                30702, 31627, 31604, 31640, 31633, 31646, 31647, 31650, 31654, 31655,
                31657, 31641};
        this.Items = new int[]{
                7080, 7519, 7081, 7497, 7280, 7311, 7342, 7373, 7404, 7435,
                7105, 0};
        this.Mob = new int[]{27296, 27231, 27305};
        this.classid = 92;
        this.prevclass = 9;
        this.X = new int[]{191046, 46066, 46066};
        this.Y = new int[]{-40640, -36396, -36396};
        this.Z = new int[]{-3042, -1685, -1685};
        registerNPCs();
    }
}
