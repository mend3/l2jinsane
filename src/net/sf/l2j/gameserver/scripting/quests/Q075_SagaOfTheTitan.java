package net.sf.l2j.gameserver.scripting.quests;

public class Q075_SagaOfTheTitan extends SagasSuperClass {
    public Q075_SagaOfTheTitan() {
        super(75, "Saga of the Titan");
        this.NPC = new int[]{
                31327, 31624, 31289, 31290, 31607, 31646, 31649, 31651, 31654, 31655,
                31658, 31290};
        this.Items = new int[]{
                7080, 7539, 7081, 7490, 7273, 7304, 7335, 7366, 7397, 7428,
                7098, 0};
        this.Mob = new int[]{27292, 27224, 27283};
        this.classid = 113;
        this.prevclass = 46;
        this.X = new int[]{119518, 181215, 181227};
        this.Y = new int[]{-28658, 36676, 36703};
        this.Z = new int[]{-3811, -4812, -4816};
        registerNPCs();
    }
}
