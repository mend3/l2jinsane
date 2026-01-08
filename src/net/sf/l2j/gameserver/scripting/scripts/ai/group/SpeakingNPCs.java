package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;

public class SpeakingNPCs extends L2AttackableAIScript {
    private static final int[] NPC_IDS = new int[]{
            27016, 27021, 27022, 27219, 27220, 27221, 27222, 27223, 27224, 27225,
            27226, 27227, 27228, 27229, 27230, 27231, 27232, 27233, 27234, 27235,
            27236, 27237, 27238, 27239, 27240, 27241, 27242, 27243, 27244, 27245,
            27246, 27247, 27249};

    public SpeakingNPCs() {
        super("ai/group");
    }

    protected void registerNpcs() {
        addEventIds(NPC_IDS, ScriptEventType.ON_ATTACK, ScriptEventType.ON_KILL);
    }

    public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill) {
        if (npc.isScriptValue(1))
            return super.onAttack(npc, attacker, damage, skill);
        String message = switch (npc.getNpcId()) {
            case 27219, 27220, 27221, 27222, 27223, 27224, 27225, 27226, 27227, 27228, 27229, 27230, 27231, 27232,
                 27233, 27234, 27235, 27236, 27237, 27238, 27239, 27240, 27241, 27242, 27243, 27244, 27245, 27246,
                 27247, 27249 -> "You dare to disturb the order of the shrine! Die!";
            case 27016 -> "...How dare you challenge me!";
            case 27021 -> "I will taste your blood!";
            case 27022 -> "I shall put you in a never-ending nightmare!";
            default -> "";
        };
        npc.broadcastNpcSay(message);
        npc.setScriptValue(1);
        return super.onAttack(npc, attacker, damage, skill);
    }

    public String onKill(Npc npc, Creature killer) {
        String message = switch (npc.getNpcId()) {
            case 27219, 27220, 27221, 27222, 27223, 27224, 27225, 27226, 27227, 27228, 27229, 27230, 27231, 27232,
                 27233, 27234, 27235, 27236, 27237, 27238, 27239, 27240, 27241, 27242, 27243, 27244, 27245, 27246,
                 27247, 27249 -> "My spirit is releasing from this shell. I'm getting close to Halisha...";
            case 27016 -> "May Beleth's power be spread on the whole world...!";
            case 27021 -> "I have fulfilled my contract with Trader Creamees.";
            case 27022 -> "My soul belongs to Icarus...";
            default -> "";
        };
        npc.broadcastNpcSay(message);
        return super.onKill(npc, killer);
    }
}
