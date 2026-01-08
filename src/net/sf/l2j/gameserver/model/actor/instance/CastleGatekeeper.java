/**/
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;

import java.util.StringTokenizer;

public class CastleGatekeeper extends Folk {
    protected boolean _currentTask;
    private int _delay;

    public CastleGatekeeper(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken();
        if (actualCommand.equalsIgnoreCase("tele")) {
            if (!this._currentTask) {
                if (this.getCastle().getSiege().isInProgress()) {
                    if (this.getCastle().getSiege().getControlTowerCount() == 0) {
                        this._delay = 480000;
                    } else {
                        this._delay = 30000;
                    }
                } else {
                    this._delay = 0;
                }

                this._currentTask = true;
                ThreadPool.schedule(new oustAllPlayers(), this._delay);
            }

            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile("data/html/castleteleporter/MassGK-1.htm");
            html.replace("%delay%", this.getDelayInSeconds());
            player.sendPacket(html);
        } else {
            super.onBypassFeedback(player, command);
        }

    }

    public void showChatWindow(Player player) {
        String filename;
        if (!this._currentTask) {
            if (this.getCastle().getSiege().isInProgress() && this.getCastle().getSiege().getControlTowerCount() == 0) {
                filename = "data/html/castleteleporter/MassGK-2.htm";
            } else {
                filename = "data/html/castleteleporter/MassGK.htm";
            }
        } else {
            filename = "data/html/castleteleporter/MassGK-1.htm";
        }

        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", this.getObjectId());
        html.replace("%delay%", this.getDelayInSeconds());
        player.sendPacket(html);
    }

    private int getDelayInSeconds() {
        return this._delay > 0 ? this._delay / 1000 : 0;
    }

    protected class oustAllPlayers implements Runnable {
        public void run() {
            if (CastleGatekeeper.this.getCastle().getSiege().isInProgress()) {
                NpcSay cs = new NpcSay(CastleGatekeeper.this.getObjectId(), 1, CastleGatekeeper.this.getNpcId(), "The defenders of " + CastleGatekeeper.this.getCastle().getName() + " castle have been teleported to the inner castle.");
                int region = MapRegionData.getInstance().getMapRegion(CastleGatekeeper.this.getX(), CastleGatekeeper.this.getY());

                for (Player player : World.getInstance().getPlayers()) {
                    if (region == MapRegionData.getInstance().getMapRegion(player.getX(), player.getY())) {
                        player.sendPacket(cs);
                    }
                }
            }

            CastleGatekeeper.this.getCastle().oustAllPlayers();
            CastleGatekeeper.this._currentTask = false;
        }
    }
}
