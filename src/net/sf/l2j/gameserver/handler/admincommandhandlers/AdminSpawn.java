package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.manager.DayNightManager;
import net.sf.l2j.gameserver.data.manager.FenceManager;
import net.sf.l2j.gameserver.data.manager.RaidBossManager;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Fence;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminSpawn implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{
            "admin_list_spawns", "admin_show_spawns", "admin_spawn", "admin_spawn_index", "admin_unspawnall", "admin_respawnall", "admin_spawn_reload", "admin_npc_index", "admin_spawn_once", "admin_show_npcs",
            "admin_spawnnight", "admin_spawnday", "admin_spawnfence", "admin_deletefence", "admin_listfence"};

    private static void spawn(Player activeChar, String monsterId, int respawnTime, boolean permanent) {
        Player player = null;
        NpcTemplate template;
        WorldObject target = activeChar.getTarget();
        if (target == null)
            player = activeChar;
        if (monsterId.matches("[0-9]*")) {
            template = NpcData.getInstance().getTemplate(Integer.parseInt(monsterId));
        } else {
            monsterId = monsterId.replace('_', ' ');
            template = NpcData.getInstance().getTemplateByName(monsterId);
        }
        try {
            L2Spawn spawn = new L2Spawn(template);
            spawn.setLoc(player.getX(), player.getY(), player.getZ(), activeChar.getHeading());
            spawn.setRespawnDelay(respawnTime);
            if (template.isType("RaidBoss")) {
                if (RaidBossManager.getInstance().getBossSpawn(spawn.getNpcId()) != null) {
                    activeChar.sendMessage("You cannot spawn another instance of " + template.getName() + ".");
                    return;
                }
                spawn.setRespawnMinDelay(43200);
                spawn.setRespawnMaxDelay(129600);
                RaidBossManager.getInstance().addNewSpawn(spawn, 0L, 0.0D, 0.0D, permanent);
            } else {
                SpawnTable.getInstance().addSpawn(spawn, permanent);
                spawn.doSpawn(false);
                if (permanent)
                    spawn.setRespawnState(true);
            }
            if (!permanent)
                spawn.setRespawnState(false);
            activeChar.sendMessage("Spawned " + template.getName() + ".");
        } catch (Exception e) {
            activeChar.sendPacket(SystemMessageId.APPLICANT_INFORMATION_INCORRECT);
        }
    }

    private static void showMonsters(Player activeChar, int level, int from) {
        List<NpcTemplate> mobs = NpcData.getInstance().getTemplates(t -> (t.isType("Monster") && t.getLevel() == level));
        StringBuilder sb = new StringBuilder(200 + mobs.size() * 100);
        StringUtil.append(sb, "<html><title>Spawn Monster:</title><body><p> Level : ", level, "<br>Total Npc's : ", mobs.size(), "<br>");
        int i = from;
        for (int j = 0; i < mobs.size() && j < 50; i++, j++) {
            StringUtil.append(sb, "<a action=\"bypass -h admin_spawn ", mobs.get(i).getNpcId(), "\">", mobs.get(i).getName(), "</a><br1>");
        }
        if (i == mobs.size()) {
            sb.append("<br><center><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
        } else {
            StringUtil.append(sb, "<br><center><button value=\"Next\" action=\"bypass -h admin_spawn_index ", level, " ", i, "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
        }
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setHtml(sb.toString());
        activeChar.sendPacket(html);
    }

    private static void showNpcs(Player activeChar, String starting, int from) {
        List<NpcTemplate> mobs = NpcData.getInstance().getTemplates(t -> (t.isType("Folk") && t.getName().startsWith(starting)));
        StringBuilder sb = new StringBuilder(200 + mobs.size() * 100);
        StringUtil.append(sb, "<html><title>Spawn Monster:</title><body><p> There are ", mobs.size(), " Npcs whose name starts with ", starting, ":<br>");
        int i = from;
        for (int j = 0; i < mobs.size() && j < 50; i++, j++) {
            StringUtil.append(sb, "<a action=\"bypass -h admin_spawn ", mobs.get(i).getNpcId(), "\">", mobs.get(i).getName(), "</a><br1>");
        }
        if (i == mobs.size()) {
            sb.append("<br><center><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
        } else {
            StringUtil.append(sb, "<br><center><button value=\"Next\" action=\"bypass -h admin_npc_index ", starting, " ", i, "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
        }
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setHtml(sb.toString());
        activeChar.sendPacket(html);
    }

    private static void listFences(Player activeChar) {
        List<Fence> fences = FenceManager.getInstance().getFences();
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>Total Fences: ").append(fences.size()).append("<br><br>");
        for (Fence fence : fences)
            sb.append("<a action=\"bypass -h admin_deletefence ").append(fence.getObjectId()).append(" 1\">Fence: ").append(fence.getObjectId()).append(" [").append(fence.getX()).append(" ").append(fence.getY()).append(" ").append(fence.getZ()).append("]</a><br>");
        sb.append("</body></html>");
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setHtml(sb.toString());
        activeChar.sendPacket(html);
    }

    public void useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_list_spawns")) {
            int npcId = 0;
            try {
                String[] params = command.split(" ");
                Pattern pattern = Pattern.compile("[0-9]*");
                Matcher regexp = pattern.matcher(params[1]);
                if (regexp.matches()) {
                    npcId = Integer.parseInt(params[1]);
                } else {
                    params[1] = params[1].replace('_', ' ');
                    npcId = NpcData.getInstance().getTemplateByName(params[1]).getNpcId();
                }
            } catch (Exception e) {
                WorldObject target = activeChar.getTarget();
                if (target instanceof Npc)
                    npcId = ((Npc) target).getNpcId();
            }
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile("data/html/admin/listspawns.htm");
            StringBuilder sb = new StringBuilder();
            int index = 0;
            String name = "";
            for (L2Spawn spawn : SpawnTable.getInstance().getSpawns()) {
                if (npcId == spawn.getNpcId()) {
                    int x, y, z;
                    index++;
                    name = spawn.getTemplate().getName();
                    Npc _npc = spawn.getNpc();
                    if (_npc != null) {
                        x = _npc.getX();
                        y = _npc.getY();
                        z = _npc.getZ();
                    } else {
                        x = spawn.getLocX();
                        y = spawn.getLocY();
                        z = spawn.getLocZ();
                    }
                    StringUtil.append(sb, "<tr><td><a action=\"bypass -h admin_move_to ", x, " ", y, " ", z, "\">", index, " - (", x,
                            " ", y, " ", z, ")", "</a></td></tr>");
                }
            }
            if (index == 0) {
                html.replace("%npcid%", "?");
                html.replace("%list%", "<tr><td>The parameter you entered as npcId is invalid.</td></tr>");
            } else {
                html.replace("%npcid%", name + " (" + name + ")");
                html.replace("%list%", sb.toString());
            }
            activeChar.sendPacket(html);
        } else if (command.equals("admin_show_spawns")) {
            AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
        } else if (command.startsWith("admin_spawn_index")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            try {
                st.nextToken();
                int level = Integer.parseInt(st.nextToken());
                int from = 0;
                try {
                    from = Integer.parseInt(st.nextToken());
                } catch (NoSuchElementException ignored) {
                }
                showMonsters(activeChar, level, from);
            } catch (Exception e) {
                AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
            }
        } else if (command.equals("admin_show_npcs")) {
            AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
        } else if (command.startsWith("admin_npc_index")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            try {
                st.nextToken();
                String letter = st.nextToken();
                int from = 0;
                try {
                    from = Integer.parseInt(st.nextToken());
                } catch (NoSuchElementException ignored) {
                }
                showNpcs(activeChar, letter, from);
            } catch (Exception e) {
                AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
            }
        } else if (command.startsWith("admin_unspawnall")) {
            World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.NPC_SERVER_NOT_OPERATING));
            RaidBossManager.getInstance().cleanUp(false);
            DayNightManager.getInstance().cleanUp();
            World.getInstance().deleteVisibleNpcSpawns();
            AdminData.getInstance().broadcastMessageToGMs("NPCs' unspawn is now complete.");
        } else if (command.startsWith("admin_spawnday")) {
            DayNightManager.getInstance().spawnCreatures(false);
            AdminData.getInstance().broadcastMessageToGMs("Spawning day creatures spawns.");
        } else if (command.startsWith("admin_spawnnight")) {
            DayNightManager.getInstance().spawnCreatures(true);
            AdminData.getInstance().broadcastMessageToGMs("Spawning night creatures spawns.");
        } else if (command.startsWith("admin_respawnall") || command.startsWith("admin_spawn_reload")) {
            RaidBossManager.getInstance().cleanUp(false);
            DayNightManager.getInstance().cleanUp();
            World.getInstance().deleteVisibleNpcSpawns();
            NpcData.getInstance().reload();
            SpawnTable.getInstance().reload();
            RaidBossManager.getInstance().reload();
            SevenSignsManager.getInstance().spawnSevenSignsNPC();
            AdminData.getInstance().broadcastMessageToGMs("NPCs' respawn is now complete.");
        } else if (command.startsWith("admin_spawnfence")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            try {
                st.nextToken();
                int type = Integer.parseInt(st.nextToken());
                int sizeX = Integer.parseInt(st.nextToken()) / 100 * 100;
                int sizeY = Integer.parseInt(st.nextToken()) / 100 * 100;
                int height = 1;
                if (st.hasMoreTokens())
                    height = Math.min(Integer.parseInt(st.nextToken()), 3);
                FenceManager.getInstance().addFence(activeChar.getX(), activeChar.getY(), activeChar.getZ(), type, sizeX, sizeY, height);
                listFences(activeChar);
            } catch (Exception e) {
                activeChar.sendMessage("Usage: //spawnfence <type> <width> <length> [height]");
            }
        } else if (command.startsWith("admin_deletefence")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            try {
                WorldObject object = World.getInstance().getObject(Integer.parseInt(st.nextToken()));
                if (object instanceof Fence) {
                    FenceManager.getInstance().removeFence((Fence) object);
                    if (st.hasMoreTokens())
                        listFences(activeChar);
                } else {
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                }
            } catch (Exception e) {
                activeChar.sendMessage("Usage: //deletefence <objectId>");
            }
        } else if (command.startsWith("admin_listfence")) {
            listFences(activeChar);
        } else if (command.startsWith("admin_spawn")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            try {
                String cmd = st.nextToken();
                String id = st.nextToken();
                int respawnTime = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 60;
                spawn(activeChar, id, respawnTime, !cmd.equalsIgnoreCase("admin_spawn_once"));
            } catch (Exception e) {
                AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
            }
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
