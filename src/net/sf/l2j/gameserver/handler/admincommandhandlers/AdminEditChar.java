/**/
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.data.xml.PlayerData;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.actors.Sex;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.GMViewItemList;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

public class AdminEditChar implements IAdminCommandHandler {
    private static final int PAGE_LIMIT = 20;
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_changelvl", "admin_edit_character", "admin_current_player", "admin_setkarma", "admin_character_info", "admin_debug", "admin_show_characters", "admin_find_character", "admin_find_ip", "admin_find_account", "admin_find_dualbox", "admin_rec", "admin_settitle", "admin_setname", "admin_setsex", "admin_setcolor", "admin_settcolor", "admin_setclass", "admin_summon_info", "admin_unsummon", "admin_summon_setlvl", "admin_show_pet_inv", "admin_fullfood", "admin_party_info", "admin_clan_info", "admin_remove_clan_penalty"};

    private static void onLineChange(Player activeChar, Player player, int lvl) {
        player.setAccessLevel(lvl);
        if (lvl >= 0) {
            player.sendMessage("Your access level has been changed to " + lvl + ".");
        } else {
            player.logout(false);
        }

        String var10001 = player.getName();
        activeChar.sendMessage(var10001 + "'s access level is now set to " + lvl + ".");
    }

    private static void listCharacters(Player activeChar, int page) {
        List<Player> players = new ArrayList<>(World.getInstance().getPlayers());
        int max = MathUtil.countPagesNumber(players.size(), 20);
        players = players.subList((page - 1) * 20, Math.min(page * 20, players.size()));
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/charlist.htm");
        StringBuilder sb = new StringBuilder(players.size() * 200);

        for (int x = 0; x < max; ++x) {
            int pagenr = x + 1;
            if (page == pagenr) {
                StringUtil.append(sb, pagenr, "&nbsp;");
            } else {
                StringUtil.append(sb, "<a action=\"bypass -h admin_show_characters ", pagenr, "\">", pagenr, "</a>&nbsp;");
            }
        }

        html.replace("%pages%", sb.toString());
        sb.setLength(0);
        Iterator var9 = players.iterator();

        while (var9.hasNext()) {
            Player player = (Player) var9.next();
            StringUtil.append(sb, "<tr><td width=80><a action=\"bypass -h admin_character_info ", player.getName(), "\">", player.getName(), "</a></td><td width=110>", player.getTemplate().getClassName(), "</td><td width=40>", player.getLevel(), "</td></tr>");
        }

        html.replace("%players%", sb.toString());
        activeChar.sendPacket(html);
    }

    public static void showCharacterInfo(Player activeChar, Player player) {
        if (player == null) {
            WorldObject target = activeChar.getTarget();
            if (!(target instanceof Player)) {
                return;
            }

            player = (Player) target;
        } else {
            activeChar.setTarget(player);
        }

        gatherCharacterInfo(activeChar, player, "charinfo.htm");
    }

    private static void gatherCharacterInfo(Player activeChar, Player player, String filename) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/" + filename);
        html.replace("%name%", player.getName());
        html.replace("%level%", player.getLevel());
        html.replace("%clan%", player.getClan() != null ? "<a action=\"bypass -h admin_clan_info " + player.getName() + "\">" + player.getClan().getName() + "</a>" : "none");
        html.replace("%xp%", player.getExp());
        html.replace("%sp%", player.getSp());
        html.replace("%class%", player.getTemplate().getClassName());
        html.replace("%ordinal%", player.getClassId().ordinal());
        html.replace("%classid%", player.getClassId().toString());
        html.replace("%baseclass%", PlayerData.getInstance().getClassNameById(player.getBaseClass()));
        html.replace("%x%", player.getX());
        html.replace("%y%", player.getY());
        html.replace("%z%", player.getZ());
        html.replace("%currenthp%", (int) player.getCurrentHp());
        html.replace("%maxhp%", player.getMaxHp());
        html.replace("%karma%", player.getKarma());
        html.replace("%currentmp%", (int) player.getCurrentMp());
        html.replace("%maxmp%", player.getMaxMp());
        html.replace("%pvpflag%", player.getPvpFlag());
        html.replace("%currentcp%", (int) player.getCurrentCp());
        html.replace("%maxcp%", player.getMaxCp());
        html.replace("%pvpkills%", player.getPvpKills());
        html.replace("%pkkills%", player.getPkKills());
        html.replace("%currentload%", player.getCurrentLoad());
        html.replace("%maxload%", player.getMaxLoad());
        html.replace("%percent%", MathUtil.roundTo((float) player.getCurrentLoad() / (float) player.getMaxLoad() * 100.0F, 2));
        html.replace("%patk%", player.getPAtk(null));
        html.replace("%matk%", player.getMAtk(null, null));
        html.replace("%pdef%", player.getPDef(null));
        html.replace("%mdef%", player.getMDef(null, null));
        html.replace("%accuracy%", player.getAccuracy());
        html.replace("%evasion%", player.getEvasionRate(null));
        html.replace("%critical%", player.getCriticalHit(null, null));
        html.replace("%runspeed%", player.getMoveSpeed());
        html.replace("%patkspd%", player.getPAtkSpd());
        html.replace("%matkspd%", player.getMAtkSpd());
        html.replace("%account%", player.getAccountName());
        html.replace("%ip%", player.getClient().isDetached() ? "Disconnected" : player.getClient().getConnection().getInetAddress().getHostAddress());
        html.replace("%ai%", player.getAI().getDesire().getIntention().name());
        activeChar.sendPacket(html);
    }

    private static void setTargetKarma(Player activeChar, int newKarma) {
        WorldObject target = activeChar.getTarget();
        if (target instanceof Player player) {
            if (newKarma >= 0) {
                int oldKarma = player.getKarma();
                player.setKarma(newKarma);
                activeChar.sendMessage("You changed " + player.getName() + "'s karma from " + oldKarma + " to " + newKarma + ".");
            } else {
                activeChar.sendMessage("The karma value must be greater or equal to 0.");
            }

        }
    }

    private static void editCharacter(Player activeChar) {
        WorldObject target = activeChar.getTarget();
        if (target instanceof Player) {
            gatherCharacterInfo(activeChar, (Player) target, "charedit.htm");
        }
    }

    private static void findCharacter(Player activeChar, String characterToFind) {
        int charactersFound = 0;
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/charfind.htm");
        StringBuilder sb = new StringBuilder();
        Iterator var5 = World.getInstance().getPlayers().iterator();

        while (var5.hasNext()) {
            Player player = (Player) var5.next();
            String name = player.getName();
            if (name.toLowerCase().contains(characterToFind.toLowerCase())) {
                ++charactersFound;
                StringUtil.append(sb, "<tr><td width=80><a action=\"bypass -h admin_character_info ", name, "\">", name, "</a></td><td width=110>", player.getTemplate().getClassName(), "</td><td width=40>", player.getLevel(), "</td></tr>");
            }

            if (charactersFound > 20) {
                break;
            }
        }

        html.replace("%results%", sb.toString());
        sb.setLength(0);
        if (charactersFound == 0) {
            sb.append("s. Please try again.");
        } else if (charactersFound > 20) {
            html.replace("%number%", " more than 20.");
            sb.append("s.<br>Please refine your search to see all of the results.");
        } else if (charactersFound == 1) {
            sb.append(".");
        } else {
            sb.append("s.");
        }

        html.replace("%number%", charactersFound);
        html.replace("%end%", sb.toString());
        activeChar.sendPacket(html);
    }

    private static void findCharactersPerIp(Player activeChar, String IpAdress) throws IllegalArgumentException {
        boolean findDisconnected = false;
        if (IpAdress.equals("disconnected")) {
            findDisconnected = true;
        } else if (!IpAdress.matches("^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$")) {
            throw new IllegalArgumentException("Malformed IPv4 number");
        }

        int charactersFound = 0;
        String ip = "0.0.0.0";
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/ipfind.htm");
        StringBuilder sb = new StringBuilder(1000);
        Iterator var7 = World.getInstance().getPlayers().iterator();

        while (var7.hasNext()) {
            Player player = (Player) var7.next();
            GameClient client = player.getClient();
            if (client.isDetached()) {
                if (!findDisconnected) {
                    continue;
                }
            } else {
                if (findDisconnected) {
                    continue;
                }

                ip = client.getConnection().getInetAddress().getHostAddress();
                if (!ip.equals(IpAdress)) {
                    continue;
                }
            }

            String name = player.getName();
            ++charactersFound;
            StringUtil.append(sb, "<tr><td width=80><a action=\"bypass -h admin_character_info ", name, "\">", name, "</a></td><td width=110>", player.getTemplate().getClassName(), "</td><td width=40>", player.getLevel(), "</td></tr>");
            if (charactersFound > 20) {
                break;
            }
        }

        html.replace("%results%", sb.toString());
        String replyMSG2;
        if (charactersFound == 0) {
            replyMSG2 = ".";
        } else if (charactersFound > 20) {
            html.replace("%number%", " more than 20.");
            replyMSG2 = "s.";
        } else if (charactersFound == 1) {
            replyMSG2 = ".";
        } else {
            replyMSG2 = "s.";
        }

        html.replace("%ip%", IpAdress);
        html.replace("%number%", charactersFound);
        html.replace("%end%", replyMSG2);
        activeChar.sendPacket(html);
    }

    private static void findCharactersPerAccount(Player activeChar, String characterName) {
        Player player = World.getInstance().getPlayer(characterName);
        if (player == null) {
            activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
        } else {
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile("data/html/admin/accountinfo.htm");
            html.replace("%characters%", String.join("<br1>", player.getAccountChars().values()));
            html.replace("%account%", player.getAccountName());
            html.replace("%player%", characterName);
            activeChar.sendPacket(html);
        }
    }

    private static void findDualbox(Player activeChar, int multibox) {
        Map<String, List<Player>> ipMap = new HashMap<>();
        String ip = "0.0.0.0";
        final Map<String, Integer> dualboxIPs = new HashMap<>();
        Iterator var5 = World.getInstance().getPlayers().iterator();

        while (var5.hasNext()) {
            Player player = (Player) var5.next();
            GameClient client = player.getClient();
            if (client != null && !client.isDetached()) {
                ip = client.getConnection().getInetAddress().getHostAddress();
                if (ipMap.get(ip) == null) {
                    ipMap.put(ip, new ArrayList<>());
                }

                ipMap.get(ip).add(player);
                if (ipMap.get(ip).size() >= multibox) {
                    Integer count = dualboxIPs.get(ip);
                    if (count == null) {
                        dualboxIPs.put(ip, multibox);
                    } else {
                        Integer var9 = count;
                        count = count + 1;
                        dualboxIPs.put(ip, var9);
                    }
                }
            }
        }

        List<String> keys = new ArrayList<>(dualboxIPs.keySet());
        Collections.sort(keys, new Comparator<String>() {
            public int compare(String left, String right) {
                return dualboxIPs.get(left).compareTo(dualboxIPs.get(right));
            }

        });
        Collections.reverse(keys);
        StringBuilder sb = new StringBuilder();
        Iterator var12 = keys.iterator();

        while (var12.hasNext()) {
            String dualboxIP = (String) var12.next();
            StringUtil.append(sb, "<a action=\"bypass -h admin_find_ip ", dualboxIP, "\">", dualboxIP, " (", dualboxIPs.get(dualboxIP), ")</a><br1>");
        }

        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/dualbox.htm");
        html.replace("%multibox%", multibox);
        html.replace("%results%", sb.toString());
        html.replace("%strict%", "");
        activeChar.sendPacket(html);
    }

    private static void gatherSummonInfo(Summon target, Player activeChar) {
        String name = target.getName();
        String owner = target.getActingPlayer().getName();
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/petinfo.htm");
        html.replace("%name%", name == null ? "N/A" : name);
        html.replace("%level%", target.getLevel());
        html.replace("%exp%", target.getStat().getExp());
        html.replace("%owner%", " <a action=\"bypass -h admin_character_info " + owner + "\">" + owner + "</a>");
        html.replace("%class%", target.getClass().getSimpleName());
        html.replace("%ai%", target.hasAI() ? target.getAI().getDesire().getIntention().name() : "NULL");
        int var10002 = (int) target.getStatus().getCurrentHp();
        html.replace("%hp%", var10002 + "/" + target.getStat().getMaxHp());
        var10002 = (int) target.getStatus().getCurrentMp();
        html.replace("%mp%", var10002 + "/" + target.getStat().getMaxMp());
        html.replace("%karma%", target.getKarma());
        html.replace("%undead%", target.isUndead() ? "yes" : "no");
        if (target instanceof Pet pet) {
            html.replace("%inv%", " <a action=\"bypass admin_show_pet_inv " + target.getActingPlayer().getObjectId() + "\">view</a>");
            var10002 = pet.getCurrentFed();
            html.replace("%food%", var10002 + "/" + pet.getPetData().getMaxMeal());
            var10002 = pet.getInventory().getTotalWeight();
            html.replace("%load%", var10002 + "/" + pet.getMaxLoad());
        } else {
            html.replace("%inv%", "none");
            html.replace("%food%", "N/A");
            html.replace("%load%", "N/A");
        }

        activeChar.sendPacket(html);
    }

    public boolean useAdminCommand(String command, Player activeChar) {
        StringTokenizer st;
        int level;
        int classidval;
        String playerName;
        if (command.startsWith("admin_changelvl")) {
            try {
                st = new StringTokenizer(command, " ");
                st.nextToken();
                int paramCount = st.countTokens();
                if (paramCount == 1) {
                    level = Integer.parseInt(st.nextToken());
                    if (activeChar.getTarget() instanceof Player) {
                        onLineChange(activeChar, (Player) activeChar.getTarget(), level);
                    } else {
                        activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                    }
                } else if (paramCount == 2) {
                    playerName = st.nextToken();
                    classidval = Integer.parseInt(st.nextToken());
                    Player player = World.getInstance().getPlayer(playerName);
                    if (player != null) {
                        onLineChange(activeChar, player, classidval);
                    } else {
                        try {
                            Connection con = ConnectionPool.getConnection();

                            try {
                                PreparedStatement ps = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?");

                                try {
                                    ps.setInt(1, classidval);
                                    ps.setString(2, playerName);
                                    ps.execute();
                                    int count = ps.getUpdateCount();
                                    if (count == 0) {
                                        activeChar.sendMessage("Player can't be found or access level unaltered.");
                                    } else {
                                        activeChar.sendMessage("Player's access level is now set to " + classidval);
                                    }
                                } catch (Throwable var33) {
                                    if (ps != null) {
                                        try {
                                            ps.close();
                                        } catch (Throwable var13) {
                                            var33.addSuppressed(var13);
                                        }
                                    }

                                    throw var33;
                                }

                                if (ps != null) {
                                    ps.close();
                                }
                            } catch (Throwable var34) {
                                if (con != null) {
                                    try {
                                        con.close();
                                    } catch (Throwable var12) {
                                        var34.addSuppressed(var12);
                                    }
                                }

                                throw var34;
                            }

                            if (con != null) {
                                con.close();
                            }
                        } catch (Exception var35) {
                        }
                    }
                }
            } catch (Exception var36) {
                activeChar.sendMessage("Usage: //changelvl <target_new_level> | <player_name> <new_level>");
            }
        } else if (command.equals("admin_current_player")) {
            showCharacterInfo(activeChar, null);
        } else {
            Player player = null;
            if (!command.startsWith("admin_character_info") && !command.startsWith("admin_debug")) {
                if (command.startsWith("admin_show_characters")) {
                    try {
                        listCharacters(activeChar, Integer.parseInt(command.substring(22)));
                    } catch (Exception var31) {
                        activeChar.sendMessage("Usage: //show_characters <page_number>");
                    }
                } else if (command.startsWith("admin_find_character")) {
                    try {
                        findCharacter(activeChar, command.substring(21));
                    } catch (Exception var30) {
                        activeChar.sendMessage("Usage: //find_character <character_name>");
                        listCharacters(activeChar, 1);
                    }
                } else if (command.startsWith("admin_find_ip")) {
                    try {
                        findCharactersPerIp(activeChar, command.substring(14));
                    } catch (Exception var29) {
                        activeChar.sendMessage("Usage: //find_ip <www.xxx.yyy.zzz>");
                        listCharacters(activeChar, 1);
                    }
                } else if (command.startsWith("admin_find_account")) {
                    try {
                        findCharactersPerAccount(activeChar, command.substring(19));
                    } catch (Exception var28) {
                        activeChar.sendMessage("Usage: //find_account <player_name>");
                        listCharacters(activeChar, 1);
                    }
                } else if (command.startsWith("admin_find_dualbox")) {
                    int multibox = 2;

                    try {
                        multibox = Integer.parseInt(command.substring(19));
                        if (multibox < 1) {
                            activeChar.sendMessage("Usage: //find_dualbox [number > 0]");
                            return false;
                        }
                    } catch (Exception var27) {
                    }

                    findDualbox(activeChar, multibox);
                } else if (command.equals("admin_edit_character")) {
                    editCharacter(activeChar);
                } else if (command.startsWith("admin_setkarma")) {
                    try {
                        setTargetKarma(activeChar, Integer.parseInt(command.substring(15)));
                    } catch (Exception var26) {
                        activeChar.sendMessage("Usage: //setkarma <new_karma_value>");
                    }
                } else {
                    WorldObject target;
                    if (command.startsWith("admin_rec")) {
                        try {
                            target = activeChar.getTarget();
                            player = null;
                            if (!(target instanceof Player)) {
                                return false;
                            }

                            player = (Player) target;
                            player.setRecomHave(Integer.parseInt(command.substring(10)));
                            player.sendMessage("You have been recommended by a GM.");
                            player.broadcastUserInfo();
                        } catch (Exception var25) {
                            activeChar.sendMessage("Usage: //rec number");
                        }
                    } else if (command.startsWith("admin_setclass")) {
                        try {
                            target = activeChar.getTarget();
                            player = null;
                            if (!(target instanceof Player)) {
                                return false;
                            }

                            player = (Player) target;
                            boolean valid = false;
                            classidval = Integer.parseInt(command.substring(15));
                            ClassId[] var53 = ClassId.VALUES;
                            int var60 = var53.length;

                            for (int var66 = 0; var66 < var60; ++var66) {
                                ClassId classid = var53[var66];
                                if (classidval == classid.getId()) {
                                    valid = true;
                                    break;
                                }
                            }

                            if (valid && player.getClassId().getId() != classidval) {
                                player.setClassId(classidval);
                                if (!player.isSubClassActive()) {
                                    player.setBaseClass(classidval);
                                }

                                String newclass = player.getTemplate().getClassName();
                                player.refreshOverloaded();
                                player.store();
                                player.sendPacket(new HennaInfo(player));
                                player.broadcastUserInfo();
                                if (player != activeChar) {
                                    player.sendMessage("A GM changed your class to " + newclass + ".");
                                }

                                String var10001 = player.getName();
                                activeChar.sendMessage(var10001 + " is now a " + newclass + ".");
                            } else {
                                activeChar.sendMessage("Usage: //setclass <valid classid>");
                            }
                        } catch (Exception var41) {
                            AdminHelpPage.showHelpPage(activeChar, "charclasses.htm");
                        }
                    } else {
                        String newName;
                        Npc npc;
                        if (command.startsWith("admin_settitle")) {
                            try {
                                target = activeChar.getTarget();
                                newName = command.substring(15);
                                if (target instanceof Player) {
                                    player = (Player) target;
                                    player.setTitle(newName);
                                    player.sendMessage("Your title has been changed by a GM.");
                                    player.broadcastTitleInfo();
                                } else if (target instanceof Npc) {
                                    npc = (Npc) target;
                                    npc.setTitle(newName);
                                    npc.broadcastPacket(new NpcInfo(npc, null));
                                } else {
                                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                }
                            } catch (Exception var24) {
                                activeChar.sendMessage("Usage: //settitle title");
                            }
                        } else if (command.startsWith("admin_setname")) {
                            try {
                                target = activeChar.getTarget();
                                newName = command.substring(14);
                                if (target instanceof Player) {
                                    if (!StringUtil.isValidString(newName, "^[A-Za-z0-9]{3,16}$")) {
                                        activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
                                        return false;
                                    }

                                    if (NpcData.getInstance().getTemplateByName(newName) != null) {
                                        activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
                                        return false;
                                    }

                                    if (PlayerInfoTable.getInstance().getPlayerObjectId(newName) > 0) {
                                        activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
                                        return false;
                                    }

                                    player = (Player) target;
                                    player.setName(newName);
                                    PlayerInfoTable.getInstance().updatePlayerData(player, false);
                                    player.sendMessage("Your name has been changed by a GM.");
                                    player.broadcastUserInfo();
                                    player.store();
                                } else if (target instanceof Npc) {
                                    npc = (Npc) target;
                                    npc.setName(newName);
                                    npc.broadcastPacket(new NpcInfo(npc, null));
                                } else {
                                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                }
                            } catch (Exception var23) {
                                activeChar.sendMessage("Usage: //setname name");
                            }
                        } else if (command.startsWith("admin_setsex")) {
                            target = activeChar.getTarget();
                            player = null;
                            if (!(target instanceof Player)) {
                                return false;
                            }

                            player = (Player) target;
                            Sex sex = Sex.MALE;

                            try {
                                st = new StringTokenizer(command, " ");
                                st.nextToken();
                                sex = Enum.valueOf(Sex.class, st.nextToken().toUpperCase());
                            } catch (Exception var22) {
                            }

                            if (sex != player.getAppearance().getSex()) {
                                player.getAppearance().setSex(sex);
                                player.sendMessage("Your gender has been changed to " + sex + " by a GM.");
                                player.broadcastUserInfo();
                                player.decayMe();
                                player.spawnMe();
                            } else {
                                activeChar.sendMessage("The character sex is already defined as " + sex + ".");
                            }
                        } else if (command.startsWith("admin_setcolor")) {
                            try {
                                target = activeChar.getTarget();
                                player = null;
                                if (!(target instanceof Player)) {
                                    return false;
                                }

                                player = (Player) target;
                                player.getAppearance().setNameColor(Integer.decode("0x" + command.substring(15)));
                                player.sendMessage("Your name color has been changed by a GM.");
                                player.broadcastUserInfo();
                            } catch (Exception var21) {
                                activeChar.sendMessage("You need to specify a valid new color.");
                            }
                        } else if (command.startsWith("admin_settcolor")) {
                            try {
                                target = activeChar.getTarget();
                                player = null;
                                if (!(target instanceof Player)) {
                                    return false;
                                }

                                player = (Player) target;
                                player.getAppearance().setTitleColor(Integer.decode("0x" + command.substring(16)));
                                player.sendMessage("Your title color has been changed by a GM.");
                                player.broadcastUserInfo();
                            } catch (Exception var20) {
                                activeChar.sendMessage("You need to specify a valid new color.");
                            }
                        } else {
                            Summon summon;
                            if (command.startsWith("admin_summon_info")) {
                                target = activeChar.getTarget();
                                if (!(target instanceof Playable)) {
                                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                    return false;
                                }

                                player = target.getActingPlayer();
                                if (player != null) {
                                    summon = player.getSummon();
                                    if (summon != null) {
                                        gatherSummonInfo(summon, activeChar);
                                    } else {
                                        activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                    }
                                }
                            } else if (command.startsWith("admin_unsummon")) {
                                target = activeChar.getTarget();
                                if (!(target instanceof Playable)) {
                                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                    return false;
                                }

                                player = target.getActingPlayer();
                                if (player != null) {
                                    summon = player.getSummon();
                                    if (summon != null) {
                                        summon.unSummon(player);
                                    } else {
                                        activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                    }
                                }
                            } else {
                                Pet pet;
                                if (command.startsWith("admin_summon_setlvl")) {
                                    target = activeChar.getTarget();
                                    if (target instanceof Pet) {
                                        pet = (Pet) target;

                                        try {
                                            level = Integer.parseInt(command.substring(20));
                                            long oldExp = pet.getStat().getExp();
                                            long newExp = pet.getStat().getExpForLevel(level);
                                            if (oldExp > newExp) {
                                                pet.getStat().removeExp(oldExp - newExp);
                                            } else if (oldExp < newExp) {
                                                pet.getStat().addExp(newExp - oldExp);
                                            }
                                        } catch (Exception var19) {
                                        }
                                    } else {
                                        activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                    }
                                } else {
                                    if (command.startsWith("admin_show_pet_inv")) {
                                        try {
                                            target = World.getInstance().getPet(Integer.parseInt(command.substring(19)));
                                        } catch (Exception var18) {
                                            target = activeChar.getTarget();
                                        }

                                        if (target instanceof Pet) {
                                            activeChar.sendPacket(new GMViewItemList((Pet) target));
                                        } else {
                                            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                        }
                                    } else if (command.startsWith("admin_fullfood")) {
                                        target = activeChar.getTarget();
                                        if (target instanceof Pet) {
                                            pet = (Pet) target;
                                            pet.setCurrentFed(pet.getPetData().getMaxMeal());
                                        } else {
                                            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                        }
                                    } else if (command.startsWith("admin_party_info")) {
                                        try {
                                            target = World.getInstance().getPlayer(command.substring(17));
                                            if (target == null) {
                                                target = activeChar.getTarget();
                                            }
                                        } catch (Exception var17) {
                                            target = activeChar.getTarget();
                                        }

                                        if (!(target instanceof Player)) {
                                            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                                            return false;
                                        }

                                        player = (Player) target;
                                        Party party = player.getParty();
                                        if (party == null) {
                                            activeChar.sendMessage(player.getName() + " isn't in a party.");
                                            return false;
                                        }

                                        StringBuilder sb = new StringBuilder(400);
                                        Iterator var59 = party.getMembers().iterator();

                                        while (var59.hasNext()) {
                                            Player member = (Player) var59.next();
                                            if (!party.isLeader(member)) {
                                                StringUtil.append(sb, "<tr><td width=150><a action=\"bypass -h admin_character_info ", member.getName(), "\">", member.getName(), " (", member.getLevel(), ")</a></td><td width=120 align=right>", member.getClassId().toString(), "</td></tr>");
                                            } else {
                                                StringUtil.append(sb, "<tr><td width=150><a action=\"bypass -h admin_character_info ", member.getName(), "\"><font color=\"LEVEL\">", member.getName(), " (", member.getLevel(), ")</font></a></td><td width=120 align=right>", member.getClassId().toString(), "</td></tr>");
                                            }
                                        }

                                        NpcHtmlMessage html = new NpcHtmlMessage(0);
                                        html.setFile("data/html/admin/partyinfo.htm");
                                        html.replace("%party%", sb.toString());
                                        activeChar.sendPacket(html);
                                    } else if (command.startsWith("admin_clan_info")) {
                                        try {
                                            player = World.getInstance().getPlayer(command.substring(16));
                                            if (player == null) {
                                                activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
                                                return false;
                                            }

                                            Clan clan = player.getClan();
                                            if (clan == null) {
                                                activeChar.sendMessage("This player isn't in a clan.");
                                                return false;
                                            }

                                            NpcHtmlMessage html = new NpcHtmlMessage(0);
                                            html.setFile("data/html/admin/claninfo.htm");
                                            html.replace("%clan_name%", clan.getName());
                                            html.replace("%clan_leader%", clan.getLeaderName());
                                            html.replace("%clan_level%", clan.getLevel());
                                            html.replace("%clan_has_castle%", clan.hasCastle() ? CastleManager.getInstance().getCastleById(clan.getCastleId()).getName() : "No");
                                            html.replace("%clan_has_clanhall%", clan.hasClanHall() ? ClanHallManager.getInstance().getClanHall(clan.getClanHallId()).getName() : "No");
                                            html.replace("%clan_points%", clan.getReputationScore());
                                            html.replace("%clan_players_count%", clan.getMembersCount());
                                            html.replace("%clan_ally%", clan.getAllyId() > 0 ? clan.getAllyName() : "Not in ally");
                                            activeChar.sendPacket(html);
                                        } catch (Exception var16) {
                                            activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
                                        }
                                    } else if (command.startsWith("admin_remove_clan_penalty")) {
                                        try {
                                            st = new StringTokenizer(command, " ");
                                            if (st.countTokens() != 3) {
                                                activeChar.sendMessage("Usage: //remove_clan_penalty join|create charname");
                                                return false;
                                            }

                                            st.nextToken();
                                            boolean changeCreateExpiryTime = st.nextToken().equalsIgnoreCase("create");
                                            playerName = st.nextToken();
                                            player = World.getInstance().getPlayer(playerName);
                                            if (player == null) {
                                                try {
                                                    Connection con = ConnectionPool.getConnection();

                                                    try {
                                                        PreparedStatement ps = con.prepareStatement("UPDATE characters SET " + (changeCreateExpiryTime ? "clan_create_expiry_time" : "clan_join_expiry_time") + " WHERE char_name=? LIMIT 1");

                                                        try {
                                                            ps.setString(1, playerName);
                                                            ps.execute();
                                                        } catch (Throwable var37) {
                                                            if (ps != null) {
                                                                try {
                                                                    ps.close();
                                                                } catch (Throwable var15) {
                                                                    var37.addSuppressed(var15);
                                                                }
                                                            }

                                                            throw var37;
                                                        }

                                                        if (ps != null) {
                                                            ps.close();
                                                        }
                                                    } catch (Throwable var38) {
                                                        if (con != null) {
                                                            try {
                                                                con.close();
                                                            } catch (Throwable var14) {
                                                                var38.addSuppressed(var14);
                                                            }
                                                        }

                                                        throw var38;
                                                    }

                                                    if (con != null) {
                                                        con.close();
                                                    }
                                                } catch (Exception var39) {
                                                }
                                            } else if (changeCreateExpiryTime) {
                                                player.setClanCreateExpiryTime(0L);
                                            } else {
                                                player.setClanJoinExpiryTime(0L);
                                            }

                                            activeChar.sendMessage("Clan penalty is successfully removed for " + playerName + ".");
                                        } catch (Exception var40) {
                                            activeChar.sendMessage("Couldn't remove clan penalty.");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                try {
                    String name = command.substring(command.startsWith("admin_character_info") ? 21 : 12);
                    player = World.getInstance().getPlayer(name);
                    if (player == null) {
                        activeChar.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST);
                        return true;
                    }

                    showCharacterInfo(activeChar, player);
                } catch (Exception var32) {
                    showCharacterInfo(activeChar, null);
                }
            }
        }

        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}