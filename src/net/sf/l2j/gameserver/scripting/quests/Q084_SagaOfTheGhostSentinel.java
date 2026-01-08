package net.sf.l2j.gameserver.scripting.quests;

public class Q084_SagaOfTheGhostSentinel extends SagasSuperClass {
    public Q084_SagaOfTheGhostSentinel() {
        super(84, "Saga of the Ghost Sentinel");
        this.NPC = new int[]{
                30702, 31587, 31604, 31640, 31635, 31646, 31649, 31652, 31654, 31655,
                31659, 31641};
        this.Items = new int[]{
                7080, 7521, 7081, 7499, 7282, 7313, 7344, 7375, 7406, 7437,
                7107, 0};
        this.Mob = new int[]{27298, 27233, 27307};
        this.classid = 109;
        this.prevclass = 37;
        this.X = new int[]{161719, 124376, 124376};
        this.Y = new int[]{-92823, 82127, 82127};
        this.Z = new int[]{-1893, -2796, -2796};
        registerNPCs();
    }
}
