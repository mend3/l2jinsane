package net.sf.l2j.gameserver.scripting.scripts.teleports;

import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;

public class HuntingGroundsTeleport extends Quest {
    private static final int[] PRIESTS = new int[]{
            31078, 31079, 31080, 31081, 31082, 31083, 31084, 31085, 31086, 31087,
            31088, 31089, 31090, 31091, 31168, 31169, 31692, 31693, 31694, 31695,
            31997, 31998};

    private static final int[] DAWN_NPCS = new int[]{
            31078, 31079, 31080, 31081, 31082, 31083, 31084, 31168, 31692, 31694,
            31997};

    public HuntingGroundsTeleport() {
        super(-1, "teleports");
        addStartNpc(PRIESTS);
        addTalkId(PRIESTS);
    }

    public String onTalk(Npc npc, Player player) {
        CabalType playerCabal = SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId());
        if (playerCabal == CabalType.NORMAL)
            return ArraysUtil.contains(DAWN_NPCS, npc.getNpcId()) ? "dawn_tele-no.htm" : "dusk_tele-no.htm";
        String htmltext = "";
        boolean check = (SevenSignsManager.getInstance().isSealValidationPeriod() && playerCabal == SevenSignsManager.getInstance().getSealOwner(SealType.GNOSIS) && SevenSignsManager.getInstance().getPlayerSeal(player.getObjectId()) == SealType.GNOSIS);
        htmltext = switch (npc.getNpcId()) {
            case 31078, 31085 -> check ? "low_gludin.htm" : "hg_gludin.htm";
            case 31079, 31086 -> check ? "low_gludio.htm" : "hg_gludio.htm";
            case 31080, 31087 -> check ? "low_dion.htm" : "hg_dion.htm";
            case 31081, 31088 -> check ? "low_giran.htm" : "hg_giran.htm";
            case 31082, 31089 -> check ? "low_heine.htm" : "hg_heine.htm";
            case 31083, 31090 -> check ? "low_oren.htm" : "hg_oren.htm";
            case 31084, 31091 -> check ? "low_aden.htm" : "hg_aden.htm";
            case 31168, 31169 -> check ? "low_hw.htm" : "hg_hw.htm";
            case 31692, 31693 -> check ? "low_goddard.htm" : "hg_goddard.htm";
            case 31694, 31695 -> check ? "low_rune.htm" : "hg_rune.htm";
            case 31997, 31998 -> check ? "low_schuttgart.htm" : "hg_schuttgart.htm";
            default -> htmltext;
        };
        return htmltext;
    }
}
