package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.StringTokenizer;

public class AdminEffects implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{
            "admin_hide", "admin_invul", "admin_undying", "admin_earthquake", "admin_earthquake_menu", "admin_gmspeed", "admin_gmspeed_menu", "admin_unpara_all", "admin_para_all", "admin_unpara",
            "admin_para", "admin_unpara_all_menu", "admin_para_all_menu", "admin_unpara_menu", "admin_para_menu", "admin_social", "admin_social_menu", "admin_effect", "admin_effect_menu", "admin_abnormal",
            "admin_abnormal_menu", "admin_jukebox", "admin_play_sound", "admin_atmosphere", "admin_atmosphere_menu"};

    private static boolean performAbnormal(int action, WorldObject target) {
        if (target instanceof Creature character) {
            if ((character.getAbnormalEffect() & action) == action) {
                character.stopAbnormalEffect(action);
            } else {
                character.startAbnormalEffect(action);
            }
            return true;
        }
        return false;
    }

    private static boolean performSocial(int action, WorldObject target) {
        if (target instanceof Creature character) {
            if (target instanceof net.sf.l2j.gameserver.model.actor.Summon || target instanceof net.sf.l2j.gameserver.model.actor.instance.Chest || (target instanceof net.sf.l2j.gameserver.model.actor.Npc && (action < 1 || action > 3)) || (target instanceof Player && (action < 2 || action > 16)))
                return false;
            character.broadcastPacket(new SocialAction(character, action));
            return true;
        }
        return false;
    }

    public void useAdminCommand(String command, Player activeChar) {
        StringTokenizer st = new StringTokenizer(command);
        st.nextToken();
        if (command.startsWith("admin_hide")) {
            if (!activeChar.getAppearance().getInvisible()) {
                activeChar.getAppearance().setInvisible();
                activeChar.decayMe();
                activeChar.broadcastUserInfo();
                activeChar.spawnMe();
            } else {
                activeChar.getAppearance().setVisible();
                activeChar.broadcastUserInfo();
            }
        } else if (command.equals("admin_invul")) {
            Player player = null;
            WorldObject object = activeChar.getTarget();
            if (object == null)
                player = activeChar;
            if (!(player instanceof Creature)) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return;
            }
            Creature target = player;
            target.setIsInvul(!target.isInvul());
            activeChar.sendMessage(target.getName() + target.getName());
        } else if (command.equals("admin_undying")) {
            Player player = null;
            WorldObject object = activeChar.getTarget();
            if (object == null)
                player = activeChar;
            if (!(player instanceof Creature)) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return;
            }
            Creature target = player;
            target.setIsMortal(!target.isMortal());
            activeChar.sendMessage(target.getName() + target.getName());
        } else if (command.startsWith("admin_earthquake")) {
            try {
                activeChar.broadcastPacket(new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())));
            } catch (Exception e) {
                activeChar.sendMessage("Use: //earthquake <intensity> <duration>");
            }
        } else if (command.startsWith("admin_atmosphere")) {
            try {
                ExRedSky exRedSky = null;
                String type = st.nextToken();
                String state = st.nextToken();
                L2GameServerPacket packet = null;
                if (type.equals("ssqinfo")) {
                    switch (state) {
                        case "dawn" -> {
                            SSQInfo sSQInfo = SSQInfo.DAWN_SKY_PACKET;
                        }
                        case "dusk" -> {
                            SSQInfo sSQInfo = SSQInfo.DUSK_SKY_PACKET;
                        }
                        case "red" -> {
                            SSQInfo sSQInfo = SSQInfo.RED_SKY_PACKET;
                        }
                        case "regular" -> {
                            SSQInfo sSQInfo = SSQInfo.REGULAR_SKY_PACKET;
                        }
                    }
                } else if (type.equals("sky")) {
                    switch (state) {
                        case "night" -> {
                            SunSet sunSet = SunSet.STATIC_PACKET;
                        }
                        case "day" -> {
                            SunRise sunRise = SunRise.STATIC_PACKET;
                        }
                        case "red" -> exRedSky = new ExRedSky(10);
                    }
                } else {
                    activeChar.sendMessage("Usage: //atmosphere <ssqinfo dawn|dusk|red|regular>");
                    activeChar.sendMessage("Usage: //atmosphere <sky day|night|red>");
                }
                if (exRedSky != null)
                    World.toAllOnlinePlayers(exRedSky);
            } catch (Exception ex) {
                activeChar.sendMessage("Usage: //atmosphere <ssqinfo dawn|dusk|red|regular>");
                activeChar.sendMessage("Usage: //atmosphere <sky day|night|red>");
            }
        } else if (command.startsWith("admin_jukebox")) {
            AdminHelpPage.showHelpPage(activeChar, "songs/songs.htm");
        } else if (command.startsWith("admin_play_sound")) {
            try {
                String sound = command.substring(17);
                PlaySound snd = sound.contains(".") ? new PlaySound(sound) : new PlaySound(1, sound);
                activeChar.broadcastPacket(snd);
                activeChar.sendMessage("Playing " + sound + ".");
            } catch (StringIndexOutOfBoundsException ignored) {
            }
        } else if (command.startsWith("admin_para_all")) {
            for (Player player : activeChar.getKnownType(Player.class)) {
                if (player.isGM())
                    continue;
                player.startAbnormalEffect(AbnormalEffect.HOLD_2);
                player.setIsParalyzed(true);
                player.startParalyze();
            }
        } else if (command.startsWith("admin_unpara_all")) {
            for (Player player : activeChar.getKnownType(Player.class)) {
                player.stopAbnormalEffect(AbnormalEffect.HOLD_2);
                player.setIsParalyzed(false);
            }
        } else if (command.startsWith("admin_para")) {
            WorldObject target = activeChar.getTarget();
            if (!(target instanceof Creature creature)) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return;
            }
            creature.startAbnormalEffect(AbnormalEffect.HOLD_2);
            creature.setIsParalyzed(true);
            creature.startParalyze();
        } else if (command.startsWith("admin_unpara")) {
            WorldObject target = activeChar.getTarget();
            if (!(target instanceof Creature creature)) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return;
            }
            creature.stopAbnormalEffect(AbnormalEffect.HOLD_2);
            creature.setIsParalyzed(false);
            creature.stopParalyze();
        } else if (command.startsWith("admin_gmspeed")) {
            try {
                activeChar.stopSkillEffects(7029);
                int val = Integer.parseInt(st.nextToken());
                if (val > 0 && val < 5)
                    activeChar.doCast(SkillTable.getInstance().getInfo(7029, val));
            } catch (Exception e) {
                activeChar.sendMessage("Use: //gmspeed value (0-4).");
            } finally {
                activeChar.updateEffectIcons();
            }
        } else if (command.startsWith("admin_social")) {
            try {
                int social = Integer.parseInt(st.nextToken());
                if (st.hasMoreTokens()) {
                    String targetOrRadius = st.nextToken();
                    if (targetOrRadius != null) {
                        Player player = World.getInstance().getPlayer(targetOrRadius);
                        if (player != null) {
                            if (performSocial(social, player)) {
                                activeChar.sendMessage(player.getName() + " was affected by your social request.");
                            } else {
                                activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
                            }
                        } else {
                            int radius = Integer.parseInt(targetOrRadius);
                            for (Creature object : activeChar.getKnownTypeInRadius(Creature.class, radius))
                                performSocial(social, object);
                            activeChar.sendMessage(radius + " units radius was affected by your social request.");
                        }
                    }
                } else {
                    Player player = null;
                    WorldObject obj = activeChar.getTarget();
                    if (obj == null)
                        player = activeChar;
                    if (performSocial(social, player)) {
                        activeChar.sendMessage(player.getName() + " was affected by your social request.");
                    } else {
                        activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
                    }
                }
            } catch (Exception e) {
                activeChar.sendMessage("Usage: //social <social_id> [player_name|radius]");
            }
        } else if (command.startsWith("admin_abnormal")) {
            try {
                int abnormal = Integer.decode("0x" + st.nextToken());
                if (st.hasMoreTokens()) {
                    String targetOrRadius = st.nextToken();
                    if (targetOrRadius != null) {
                        Player player = World.getInstance().getPlayer(targetOrRadius);
                        if (player != null) {
                            if (performAbnormal(abnormal, player)) {
                                activeChar.sendMessage(player.getName() + " was affected by your abnormal request.");
                            } else {
                                activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
                            }
                        } else {
                            int radius = Integer.parseInt(targetOrRadius);
                            for (Creature object : activeChar.getKnownTypeInRadius(Creature.class, radius))
                                performAbnormal(abnormal, object);
                            activeChar.sendMessage(radius + " units radius was affected by your abnormal request.");
                        }
                    }
                } else {
                    Player player = null;
                    WorldObject obj = activeChar.getTarget();
                    if (obj == null)
                        player = activeChar;
                    if (performAbnormal(abnormal, player)) {
                        activeChar.sendMessage(player.getName() + " was affected by your abnormal request.");
                    } else {
                        activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
                    }
                }
            } catch (Exception e) {
                activeChar.sendMessage("Usage: //abnormal <hex_abnormal_mask> [player|radius]");
            }
        } else if (command.startsWith("admin_effect")) {
            try {
                Player player = null;
                WorldObject obj = activeChar.getTarget();
                int level = 1, hittime = 1;
                int skill = Integer.parseInt(st.nextToken());
                if (st.hasMoreTokens())
                    level = Integer.parseInt(st.nextToken());
                if (st.hasMoreTokens())
                    hittime = Integer.parseInt(st.nextToken());
                if (obj == null)
                    player = activeChar;
                if (!(player instanceof Creature)) {
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                } else {
                    Creature target = player;
                    target.broadcastPacket(new MagicSkillUse(target, activeChar, skill, level, hittime, 0));
                    activeChar.sendMessage(player.getName() + " performs MSU " + player.getName() + "/" + skill + " by your request.");
                }
            } catch (Exception e) {
                activeChar.sendMessage("Usage: //effect skill [level | level hittime]");
            }
        }
        if (command.contains("menu")) {
            String filename = "effects_menu.htm";
            if (command.contains("abnormal")) {
                filename = "abnormal.htm";
            } else if (command.contains("social")) {
                filename = "social.htm";
            }
            AdminHelpPage.showHelpPage(activeChar, filename);
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
