package net.sf.l2j.gameserver.scripting.quests;

public class Q086_SagaOfTheHierophant extends SagasSuperClass {
    public Q086_SagaOfTheHierophant() {
        super(86, "Saga of the Hierophant");
        this.NPC = new int[]{
                30191, 31626, 31588, 31280, 31591, 31646, 31648, 31652, 31654, 31655,
                31659, 31280};
        this.Items = new int[]{
                7080, 7523, 7081, 7501, 7284, 7315, 7346, 7377, 7408, 7439,
                7089, 0};
        this.Mob = new int[]{27269, 27235, 27275};
        this.classid = 98;
        this.prevclass = 17;
        this.X = new int[]{161719, 124355, 124376};
        this.Y = new int[]{-92823, 82155, 82127};
        this.Z = new int[]{-1893, -2803, -2796};
        registerNPCs();
    }
}
