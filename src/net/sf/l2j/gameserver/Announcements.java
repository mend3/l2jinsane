/**/
package net.sf.l2j.gameserver;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

import java.util.Iterator;

public class Announcements {
    public static boolean isSummoning = false;
    private static Announcements _instance;

    public static Announcements getInstance() {
        if (_instance == null) {
            _instance = new Announcements();
        }

        return _instance;
    }

    public static void announceToPlayers(String message) {
        Iterator var1 = World.getInstance().getPlayers().iterator();

        while (var1.hasNext()) {
            Player player = (Player) var1.next();
            player.sendMessage(message);
        }

    }

    public static void ArenaAnnounce(String text) {
        isSummoning = true;
        Iterator var1 = World.getInstance().getPlayers().iterator();

        while (var1.hasNext()) {
            Player player = (Player) var1.next();
            if (player != null && player.isOnline()) {
                new CreatureSay(0, 1, "", "[Tournament]: " + text);
            }

            if (Config.ARENA_MESSAGE_ENABLED && player != null && player.isOnline() && !player.isInsideZone(ZoneId.ARENA_EVENT) && !player.isDead() && !player.isInArenaEvent() && !player.isInStoreMode() && !player.isRooted() && player.getKarma() <= 0 && !player.isInOlympiadMode() && !player.isFestivalParticipant() && Config.TOURNAMENT_EVENT_SUMMON) {
                ThreadPool.schedule(new Runnable() {
                    public void run() {
                        Announcements.isSummoning = false;
                    }
                }, 31000L);
                SpawnLocation _position = new SpawnLocation(Config.Tournament_locx + Rnd.get(-100, 100), Config.Tournament_locy + Rnd.get(-100, 100), Config.Tournament_locz, 0);
                ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
                confirm.addString("=== [Tournament]: (4 x 4) / (9 x 9) ====");
                confirm.addZoneName(_position);
                confirm.addTime(30000);
                confirm.addRequesterId(player.getObjectId());
                player.sendPacket(confirm);
            }
        }

    }

    static class Restore implements Runnable {
        Restore(final Announcements param1) {
        }

        public void run() {
            Announcements.isSummoning = false;
        }
    }
}