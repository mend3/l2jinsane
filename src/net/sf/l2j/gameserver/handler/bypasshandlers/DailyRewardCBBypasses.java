package net.sf.l2j.gameserver.handler.bypasshandlers;

import net.sf.l2j.gameserver.data.DailyRewardData;
import net.sf.l2j.gameserver.data.manager.DailyRewardManager;
import net.sf.l2j.gameserver.handler.IBypassHandler;
import net.sf.l2j.gameserver.model.DailyReward;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.StringTokenizer;

public class DailyRewardCBBypasses implements IBypassHandler {
    public boolean handleBypass(String bypass, Player activeChar) {
        StringTokenizer st = new StringTokenizer(bypass, " ");
        st.nextToken();
        if (bypass.startsWith("bp_getDailyReward")) {
            int day = Integer.parseInt(st.nextToken());
            DailyReward dr = DailyRewardData.getInstance().getDailyRewardByDay(day);
            DailyRewardManager.getInstance().tryToGetDailyReward(activeChar, dr);
            DailyRewardManager.getInstance().showBoard(activeChar, "index");
        }
        if (bypass.startsWith("bp_showDailyRewardsBoard"))
            DailyRewardManager.getInstance().showBoard(activeChar, "index");
        return false;
    }

    public String[] getBypassHandlersList() {
        return new String[]{"bp_getDailyReward", "bp_showDailyRewardsBoard"};
    }
}
