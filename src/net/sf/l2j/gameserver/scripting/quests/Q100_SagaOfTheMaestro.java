package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q100_SagaOfTheMaestro extends SagasSuperClass {
    public Q100_SagaOfTheMaestro() {
        super(100, "Saga of the Maestro");
        this.NPC = new int[]{
                31592, 31273, 31597, 31597, 31596, 31646, 31648, 31653, 31654, 31655,
                31656, 31597};
        this.Items = new int[]{
                7080, 7607, 7081, 7515, 7298, 7329, 7360, 7391, 7422, 7453,
                7108, 0};
        this.Mob = new int[]{27260, 27249, 27308};
        this.classid = 118;
        this.prevclass = 57;
        this.X = new int[]{164650, 47429, 47391};
        this.Y = new int[]{-74121, -56923, -56929};
        this.Z = new int[]{-2871, -2383, -2370};
        registerNPCs();
    }
}
