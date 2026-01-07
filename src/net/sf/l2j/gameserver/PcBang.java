package net.sf.l2j.gameserver;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;

public class PcBang implements Runnable {
    private static final PcBang _instance = new PcBang();

    private PcBang() {
    }

    public static PcBang getInstance() {
        return _instance;
    }

    public void run() {
        int score = 0;
        for (Player activeChar : World.getInstance().getPlayers()) {
            if (activeChar.isOnline() && activeChar.getLevel() > Config.PCB_MIN_LEVEL && !activeChar.getClient().isDetached()) {
                score = Rnd.get(Config.PCB_POINT_MIN, Config.PCB_POINT_MAX);
                if (Rnd.get(100) <= Config.PCB_CHANCE_DUAL_POINT) {
                    score *= 2;
                    activeChar.addPcBangScore(score);
                    activeChar.sendMessage("Your PC Bang Point had doubled.");
                    activeChar.updatePcBangWnd(score, true, true);
                } else {
                    activeChar.addPcBangScore(score);
                    activeChar.sendMessage("You recevied PC Bang Point.");
                    activeChar.updatePcBangWnd(score, true, false);
                }
            }
        }
    }
}
