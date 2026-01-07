package net.sf.l2j.gameserver.model.actor.instance;

import mods.newbies.NewbiesNpc;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.PlayerData;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.player.Experience;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class NewbieNpc extends Folk {
    public NewbieNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        if (player == null)
            return;
        if (!Config.ALLOW_CLASS_MASTERS)
            return;
        if (command.equalsIgnoreCase("change")) {
            String filename = "data/html/mods/NewbieNpc/changeclass.htm";
            if (Config.ALLOW_CLASS_MASTERS)
                filename = "data/html/mods/NewbieNpc/changeclass.htm";
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile(filename);
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        }
        if (command.startsWith("1stClass")) {
            ClassMaster.showHtmlMenu(player, getObjectId(), 1);
        } else if (command.startsWith("2ndClass")) {
            ClassMaster.showHtmlMenu(player, getObjectId(), 2);
        } else if (command.startsWith("3rdClass")) {
            ClassMaster.showHtmlMenu(player, getObjectId(), 3);
        } else if (command.startsWith("learn_skills")) {
            player.rewardSkills();
        } else if (command.startsWith("change_class")) {
            int val = Integer.parseInt(command.substring(13));
            if (ClassMaster.checkAndChangeClass(player, val)) {
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/classmaster/ok.htm");
                html.replace("%name%", PlayerData.getInstance().getClassNameById(val));
                player.sendPacket(html);
            }
        } else if (command.equalsIgnoreCase("LevelUp")) {
            player.addExpAndSp(Experience.LEVEL[Config.NEWBIE_LVL], 0);
        } else if (command.equalsIgnoreCase("items")) {
            ClassId currentClassId = player.getClassId();
            if (currentClassId.level() < 3) {
                player.sendMessage("First Complete Your Third Class!");
                return;
            }
            if (player.getReceiveNewbieArmor()) {
                player.sendMessage("you have already received the items!");
                return;
            }
            ClassId classes = player.getClassId();
            switch (classes) {
                case ADVENTURER:
                case SAGGITARIUS:
                case DUELIST:
                case TITAN:
                case GRAND_KHAVATARI:
                case PHOENIX_KNIGHT:
                case MOONLIGHT_SENTINEL:
                case FORTUNE_SEEKER:
                case MAESTRO:
                case DREADNOUGHT:
                case HELL_KNIGHT:
                case EVAS_TEMPLAR:
                case SWORD_MUSE:
                case WIND_RIDER:
                case SHILLIEN_TEMPLAR:
                case SPECTRAL_DANCER:
                case GHOST_HUNTER:
                case GHOST_SENTINEL:
                case SOULTAKER:
                case MYSTIC_MUSE:
                case ARCHMAGE:
                case ARCANA_LORD:
                case ELEMENTAL_MASTER:
                case CARDINAL:
                case STORM_SCREAMER:
                case SPECTRAL_MASTER:
                case SHILLIEN_SAINT:
                case DOMINATOR:
                case DOOMCRYER:
                    NewbiesNpc.giveItems(0, player);
                    break;
            }
            player.setReceiveNewbieArmor(true);
        } else if (command.equalsIgnoreCase("buffs")) {
            if (player.isMageClass() || player.getClassId() == ClassId.DOMINATOR || player.getClassId() == ClassId.DOOMCRYER) {
                for (int id : Config.NEWBIE_MAGE_BUFFS)
                    SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(player, player);
                player.setCurrentHp(player.getMaxHp());
                player.setCurrentCp(player.getMaxCp());
                player.setCurrentMp(player.getMaxMp());
            } else {
                for (int id : Config.NEWBIE_FIGHTER_BUFFS)
                    SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(player, player);
                player.setCurrentHp(player.getMaxHp());
                player.setCurrentCp(player.getMaxCp());
                player.setCurrentMp(player.getMaxMp());
            }
        } else if (command.equalsIgnoreCase("teleport")) {
            ClassId currentClassId = player.getClassId();
            if (currentClassId.level() < 3) {
                player.sendMessage("You Can't Leave! Your Character Isin't Complete!");
                return;
            }
            player.teleportTo(Config.SETX, Config.SETY, Config.SETZ, 0);
            player.sendPacket(new ExShowScreenMessage("seconds until auto respawn", 4000, 2, true));
        }
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + npcId;
        }
        return "data/html/mods/newbieNpc/" + filename + ".htm";
    }
}
