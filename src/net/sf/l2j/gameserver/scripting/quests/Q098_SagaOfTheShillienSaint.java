package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q098_SagaOfTheShillienSaint extends SagasSuperClass {
    public Q098_SagaOfTheShillienSaint() {
        super(98, "Saga of the Shillien Saint");
        this.NPC = new int[]{
                31581, 31626, 31588, 31287, 31621, 31646, 31647, 31651, 31654, 31655,
                31658, 31287};
        this.Items = new int[]{
                7080, 7525, 7081, 7513, 7296, 7327, 7358, 7389, 7420, 7451,
                7090, 0};
        this.Mob = new int[]{27270, 27247, 27277};
        this.classid = 112;
        this.prevclass = 43;
        this.X = new int[]{119518, 181215, 181227};
        this.Y = new int[]{-28658, 36676, 36703};
        this.Z = new int[]{-3811, -4812, -4816};
        registerNPCs();
    }
}
