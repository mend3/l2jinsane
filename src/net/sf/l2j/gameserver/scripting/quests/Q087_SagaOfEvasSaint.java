package net.sf.l2j.gameserver.scripting.quests;

public class Q087_SagaOfEvasSaint extends SagasSuperClass {
    public Q087_SagaOfEvasSaint() {
        super(87, "Saga of Eva's Saint");
        this.NPC = new int[]{
                30191, 31626, 31588, 31280, 31620, 31646, 31649, 31653, 31654, 31655,
                31657, 31280};
        this.Items = new int[]{
                7080, 7524, 7081, 7502, 7285, 7316, 7347, 7378, 7409, 7440,
                7088, 0};
        this.Mob = new int[]{27266, 27236, 27276};
        this.classid = 105;
        this.prevclass = 30;
        this.X = new int[]{164650, 46087, 46066};
        this.Y = new int[]{-74121, -36372, -36396};
        this.Z = new int[]{-2871, -1685, -1685};
        registerNPCs();
    }
}
