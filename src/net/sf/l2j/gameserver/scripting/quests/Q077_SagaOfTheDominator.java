package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q077_SagaOfTheDominator extends SagasSuperClass {
    public Q077_SagaOfTheDominator() {
        super(77, "Saga of the Dominator");
        this.NPC = new int[]{
                31336, 31624, 31371, 31290, 31636, 31646, 31648, 31653, 31654, 31655,
                31656, 31290};
        this.Items = new int[]{
                7080, 7539, 7081, 7492, 7275, 7306, 7337, 7368, 7399, 7430,
                7100, 0};
        this.Mob = new int[]{27294, 27226, 27262};
        this.classid = 115;
        this.prevclass = 51;
        this.X = new int[]{164650, 47429, 47391};
        this.Y = new int[]{-74121, -56923, -56929};
        this.Z = new int[]{-2871, -2383, -2370};
        registerNPCs();
    }
}
