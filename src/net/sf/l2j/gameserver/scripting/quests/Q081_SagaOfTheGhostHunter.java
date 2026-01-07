package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q081_SagaOfTheGhostHunter extends SagasSuperClass {
    public Q081_SagaOfTheGhostHunter() {
        super(81, "Saga of the Ghost Hunter");
        this.NPC = new int[]{
                31603, 31624, 31286, 31615, 31617, 31646, 31649, 31653, 31654, 31655,
                31656, 31616};
        this.Items = new int[]{
                7080, 7518, 7081, 7496, 7279, 7310, 7341, 7372, 7403, 7434,
                7104, 0};
        this.Mob = new int[]{27301, 27230, 27304};
        this.classid = 108;
        this.prevclass = 36;
        this.X = new int[]{164650, 47391, 47429};
        this.Y = new int[]{-74121, -56929, -56923};
        this.Z = new int[]{-2871, -2370, -2383};
        registerNPCs();
    }
}
