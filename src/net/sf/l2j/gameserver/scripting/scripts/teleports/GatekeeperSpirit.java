package net.sf.l2j.gameserver.scripting.scripts.teleports;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;

public class GatekeeperSpirit extends Quest {
    private static final int ENTER_GK = 31111;

    private static final int EXIT_GK = 31112;

    private static final int LILITH = 25283;

    private static final int ANAKIM = 25286;

    public GatekeeperSpirit() {
        super(-1, "teleports");
        addStartNpc(31111);
        addFirstTalkId(31111);
        addTalkId(31111);
        addKillId(25283, 25286);
    }

    public String onAdvEvent(String event, Npc npc, Player player) {
        if (event.equalsIgnoreCase("lilith_exit")) {
            addSpawn(31112, 184446, -10112, -5488, 0, false, 900000L, false);
        } else if (event.equalsIgnoreCase("anakim_exit")) {
            addSpawn(31112, 184466, -13106, -5488, 0, false, 900000L, false);
        }
        return super.onAdvEvent(event, npc, player);
    }

    public String onFirstTalk(Npc npc, Player player) {
        CabalType playerCabal = SevenSignsManager.getInstance().getPlayerCabal(player.getObjectId());
        CabalType sealAvariceOwner = SevenSignsManager.getInstance().getSealOwner(SealType.AVARICE);
        CabalType winningCabal = SevenSignsManager.getInstance().getCabalHighestScore();
        if (playerCabal == sealAvariceOwner && playerCabal == winningCabal)
            switch (sealAvariceOwner) {
                case DAWN:
                    return "dawn.htm";
                case DUSK:
                    return "dusk.htm";
            }
        npc.showChatWindow(player);
        return null;
    }

    public String onKill(Npc npc, Creature killer) {
        switch (npc.getNpcId()) {
            case 25283:
                startQuestTimer("lilith_exit", 10000L, null, null, false);
                break;
            case 25286:
                startQuestTimer("anakim_exit", 10000L, null, null, false);
                break;
        }
        return super.onKill(npc, killer);
    }
}
