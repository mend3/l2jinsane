package net.sf.l2j.gameserver.scripting.quests;

public class Q090_SagaOfTheStormScreamer extends SagasSuperClass {
    public Q090_SagaOfTheStormScreamer() {
        super(90, "Saga of the Storm Screamer");
        this.NPC = new int[]{
                30175, 31627, 31287, 31287, 31598, 31646, 31649, 31652, 31654, 31655,
                31659, 31287};
        this.Items = new int[]{
                7080, 7531, 7081, 7505, 7288, 7319, 7350, 7381, 7412, 7443,
                7084, 0};
        this.Mob = new int[]{27252, 27239, 27256};
        this.classid = 110;
        this.prevclass = 40;
        this.X = new int[]{161719, 124376, 124355};
        this.Y = new int[]{-92823, 82127, 82155};
        this.Z = new int[]{-1893, -2796, -2803};
        registerNPCs();
    }
}
