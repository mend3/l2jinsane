package enginemods.main.engine.npc;

import enginemods.main.engine.AbstractMods;
import enginemods.main.util.Util;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class NpcTeleporter extends AbstractMods {
    private static final int NPC = 60011;
    private static final Map<String, Location> TELEPORTS = new HashMap<>();

    public NpcTeleporter() {
        TELEPORTS.put("40", new Location(121980, -118800, -2574));
        TELEPORTS.put("60 TOP", new Location(174528, 52683, -4369));
        TELEPORTS.put("60 UNDER", new Location(170327, 53985, -4583));
        TELEPORTS.put("70", new Location(188191, -74959, -2738));
        this.registerMod(true);
        this.spawnGuards();
    }

    public static void getInstance() {
    }

    public void onModState() {
    }

    private void spawnGuards() {
        ThreadPool.schedule(() -> {
            for (Location loc : TELEPORTS.values()) {
                this.addSpawn(60010, loc, true, 0L);
                this.addSpawn(60010, loc, true, 0L);
            }

        }, 20000L);
    }

    public boolean onInteract(Player player, Creature npc) {
        if (!Util.areObjectType(Npc.class, npc)) {
            return false;
        } else if (((Npc) npc).getNpcId() != 60011) {
            return false;
        } else {
            HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
            hb.append("<html><body>");
            hb.append(Html.headHtml("TELEPORT MASTER"));
            hb.append("Puedo mostrarte las zonas donde");
            hb.append("los hombres se convierten en <font color=LEVEL>dioses!</font>");

            for (String tele : TELEPORTS.keySet()) {
                hb.append("<table width=280>");
                hb.append("<tr>");
                hb.append("<td align=center>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
                hb.append("<td><button value=\"", tele, "\" action=\"bypass -h Engine NpcTeleporter teleport ", tele, "\" width=216 height=32 back=L2UI_CH3.refinegrade3_21 fore=L2UI_CH3.refinegrade3_21></td>");
                hb.append("<td align=center>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
                hb.append("</tr>");
                hb.append("</table>");
            }

            hb.append("</body></html>");
            sendHtml(player, (Npc) npc, hb);
            return true;
        }
    }

    public void onEvent(Player player, Creature npc, String command) {
        if (((Npc) npc).getNpcId() == 60011) {
            StringTokenizer st = new StringTokenizer(command, " ");
            switch (st.nextToken()) {
                case "teleport":
                    String locName = st.nextToken();
                    if (TELEPORTS.containsKey(locName)) {
                        player.teleportTo(TELEPORTS.get(locName), 30);
                    }
                default:
            }
        }
    }

    private static class SingletonHolder {
        protected static final NpcTeleporter INSTANCE = new NpcTeleporter();
    }
}
