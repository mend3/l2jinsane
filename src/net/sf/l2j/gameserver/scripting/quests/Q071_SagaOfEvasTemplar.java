package net.sf.l2j.gameserver.scripting.quests;

public class Q071_SagaOfEvasTemplar extends SagasSuperClass {
    public Q071_SagaOfEvasTemplar() {
        super(71, "Saga of Eva's Templar");
        this.NPC = new int[]{
                30852, 31624, 31278, 30852, 31638, 31646, 31648, 31651, 31654, 31655,
                31658, 31281};
        this.Items = new int[]{
                7080, 7535, 7081, 7486, 7269, 7300, 7331, 7362, 7393, 7424,
                7094, 6482};
        this.Mob = new int[]{27287, 27220, 27279};
        this.classid = 99;
        this.prevclass = 20;
        this.X = new int[]{119518, 181215, 181227};
        this.Y = new int[]{-28658, 36676, 36703};
        this.Z = new int[]{-3811, -4812, -4816};
        registerNPCs();
    }
}
