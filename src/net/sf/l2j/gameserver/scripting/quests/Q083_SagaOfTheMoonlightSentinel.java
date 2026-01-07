package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.scripting.quests.SagasScripts.SagasSuperClass;

public class Q083_SagaOfTheMoonlightSentinel extends SagasSuperClass {
    public Q083_SagaOfTheMoonlightSentinel() {
        super(83, "Saga of the Moonlight Sentinel");
        this.NPC = new int[]{
                30702, 31627, 31604, 31640, 31634, 31646, 31648, 31652, 31654, 31655,
                31658, 31641};
        this.Items = new int[]{
                7080, 7520, 7081, 7498, 7281, 7312, 7343, 7374, 7405, 7436,
                7106, 0};
        this.Mob = new int[]{27297, 27232, 27306};
        this.classid = 102;
        this.prevclass = 24;
        this.X = new int[]{161719, 181227, 181215};
        this.Y = new int[]{-92823, 36703, 36676};
        this.Z = new int[]{-1893, -4816, -4812};
        registerNPCs();
    }
}
