package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.xml.SkillTreeData;
import net.sf.l2j.gameserver.enums.skills.AcquireSkillType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.holder.skillnode.EnchantSkillNode;
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.skills.effects.EffectBuff;
import net.sf.l2j.gameserver.skills.effects.EffectDebuff;

import java.util.List;

public class Folk extends Npc {
    public Folk(int objectId, NpcTemplate template) {
        super(objectId, template);
        this.setIsMortal(false);
    }

    public void addEffect(L2Effect newEffect) {
        if (!(newEffect instanceof EffectDebuff) && !(newEffect instanceof EffectBuff)) {
            if (newEffect != null) {
                newEffect.stopEffectTask();
            }
        } else {
            super.addEffect(newEffect);
        }

    }

    public void showSkillList(Player player) {
        if (!this.getTemplate().canTeach(player.getClassId())) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile("data/html/trainer/" + this.getTemplate().getNpcId() + "-noskills.htm");
            player.sendPacket(html);
        } else {
            List<GeneralSkillNode> skills = player.getAvailableSkills();
            if (skills.isEmpty()) {
                int minlevel = player.getRequiredLevelForNextSkill();
                if (minlevel > 0) {
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(minlevel));
                } else {
                    player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
                }
            } else {
                player.sendPacket(new AcquireSkillList(AcquireSkillType.USUAL, skills));
            }

            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public void showEnchantSkillList(Player player) {
        if (!this.getTemplate().canTeach(player.getClassId())) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile("data/html/trainer/" + this.getTemplate().getNpcId() + "-noskills.htm");
            player.sendPacket(html);
        } else if (player.getClassId().level() < 3) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setHtml("<html><body> You must have 3rd class change quest completed.</body></html>");
            player.sendPacket(html);
        } else {
            List<EnchantSkillNode> skills = SkillTreeData.getInstance().getEnchantSkillsFor(player);
            if (skills.isEmpty()) {
                player.sendPacket(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
                if (player.getLevel() < 74) {
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(74));
                } else {
                    player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
                }
            } else {
                player.sendPacket(new ExEnchantSkillList(skills));
            }

            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public void giveBlessingSupport(Player player) {
        if (player != null) {
            this.setTarget(player);
            if (player.getLevel() <= 39 && player.getClassId().level() < 2) {
                this.doCast(SkillTable.FrequentSkill.BLESSING_OF_PROTECTION.getSkill());
            } else {
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                html.setHtml("<html><body>Newbie Guide:<br>I'm sorry, but you are not eligible to receive the protection blessing.<br1>It can only be bestowed on <font color=\"LEVEL\">characters below level 39 who have not made a seccond transfer.</font></body></html>");
                html.replace("%objectId%", this.getObjectId());
                player.sendPacket(html);
            }
        }
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("SkillList")) {
            this.showSkillList(player);
        } else if (command.startsWith("EnchantSkillList")) {
            this.showEnchantSkillList(player);
        } else if (command.startsWith("GiveBlessing")) {
            this.giveBlessingSupport(player);
        } else {
            super.onBypassFeedback(player, command);
        }

    }
}
